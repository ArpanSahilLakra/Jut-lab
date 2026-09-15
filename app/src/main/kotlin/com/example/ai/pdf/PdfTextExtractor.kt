package com.example.ai.pdf

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream

/**
 * Result of PDF text extraction.
 */
sealed class PdfExtractionResult {
    data class Success(
        val rawText: String,
        val cleanedText: String,
        val chunks: List<String>,
        val pageCount: Int,
        val isTruncatedForBudget: Boolean
    ) : PdfExtractionResult()

    data class ScannedOrImageOnly(
        val message: String
    ) : PdfExtractionResult()

    data class Error(
        val cause: Throwable,
        val message: String
    ) : PdfExtractionResult()
}

/**
 * Robust, Android-compatible PDF text extractor.
 * Decompresses FlateDecode content streams and extracts text glyph operators
 * without relying on brittle byte-level regexes.
 */
object PdfTextExtractor {

    // Context budgeting limits
    const val MAX_OFFLINE_CHARS = 4000
    const val MAX_ONLINE_CHARS = 32000
    const val CHUNK_SIZE_CHARS = 800

    /**
     * Extracts text from a PDF byte array.
     */
    fun extractText(pdfBytes: ByteArray, maxChars: Int = MAX_OFFLINE_CHARS): PdfExtractionResult {
        if (pdfBytes.isEmpty()) {
            return PdfExtractionResult.Error(IllegalArgumentException("PDF byte array is empty"), "Empty document")
        }

        // Check PDF header
        val header = String(pdfBytes.take(8).toByteArray(), Charsets.US_ASCII)
        if (!header.startsWith("%PDF-")) {
            return PdfExtractionResult.Error(IllegalArgumentException("Invalid PDF header: $header"), "Not a valid PDF file")
        }

        return try {
            val extractedText = parsePdfStreams(pdfBytes)
            val cleaned = cleanText(extractedText)

            if (cleaned.length < 30) {
                // Image-only / scanned document with no readable text streams
                return PdfExtractionResult.ScannedOrImageOnly(
                    "This document contains no readable text streams (it appears to be a scanned image-only PDF). " +
                    "In Offline mode, local OCR is not supported. Please switch to Online mode to analyze page images with Gemini Multimodal."
                )
            }

            val isTruncated = cleaned.length > maxChars
            val budgetedText = if (isTruncated) cleaned.take(maxChars) else cleaned
            val chunks = chunkText(budgetedText, CHUNK_SIZE_CHARS)

            PdfExtractionResult.Success(
                rawText = extractedText,
                cleanedText = budgetedText,
                chunks = chunks,
                pageCount = estimatePageCount(pdfBytes),
                isTruncatedForBudget = isTruncated
            )
        } catch (e: Throwable) {
            PdfExtractionResult.Error(e, "PDF text extraction failed: ${e.localizedMessage}")
        }
    }

    private fun estimatePageCount(bytes: ByteArray): Int {
        val s = String(bytes, Charsets.ISO_8859_1)
        val matches = Regex("/Type\\s*/Page\\b").findAll(s).count()
        return if (matches > 0) matches else 1
    }

    /**
     * Finds stream ... endstream blocks and decompresses them.
     */
    private fun parsePdfStreams(bytes: ByteArray): String {
        val outText = StringBuilder()
        val streamStartMarker = "stream".toByteArray(Charsets.US_ASCII)
        val streamEndMarker = "endstream".toByteArray(Charsets.US_ASCII)

        var index = 0
        while (index < bytes.size) {
            val startIdx = findBytes(bytes, streamStartMarker, index)
            if (startIdx == -1) break

            // Skip 'stream' and possible newline (\r\n or \n)
            var streamDataStart = startIdx + streamStartMarker.size
            if (streamDataStart < bytes.size && bytes[streamDataStart] == '\r'.code.toByte()) streamDataStart++
            if (streamDataStart < bytes.size && bytes[streamDataStart] == '\n'.code.toByte()) streamDataStart++

            val endIdx = findBytes(bytes, streamEndMarker, streamDataStart)
            if (endIdx == -1) break

            val streamBytes = bytes.copyOfRange(streamDataStart, endIdx)

            // Try decompressing with Inflater (FlateDecode)
            val decompressed = tryDecompressFlate(streamBytes)
            val contentString = if (decompressed != null) {
                String(decompressed, Charsets.ISO_8859_1)
            } else {
                String(streamBytes, Charsets.ISO_8859_1)
            }

            // Extract text from text blocks (BT ... ET)
            extractTextFromContentStream(contentString, outText)

            index = endIdx + streamEndMarker.size
        }

        return outText.toString()
    }

    /**
     * Attempts to decompress zlib/deflate stream data.
     */
    private fun tryDecompressFlate(data: ByteArray): ByteArray? {
        if (data.size < 2) return null
        return try {
            val inflater = Inflater(false)
            val out = ByteArrayOutputStream()
            val buffer = ByteArray(4096)
            inflater.setInput(data)
            while (!inflater.finished()) {
                val count = inflater.inflate(buffer)
                if (count == 0 && inflater.needsInput()) break
                out.write(buffer, 0, count)
            }
            inflater.end()
            val result = out.toByteArray()
            if (result.isNotEmpty()) result else null
        } catch (e: Exception) {
            // Try with nowrap = true (raw deflate without zlib header)
            try {
                val inflater = Inflater(true)
                val out = ByteArrayOutputStream()
                val buffer = ByteArray(4096)
                inflater.setInput(data)
                while (!inflater.finished()) {
                    val count = inflater.inflate(buffer)
                    if (count == 0 && inflater.needsInput()) break
                    out.write(buffer, 0, count)
                }
                inflater.end()
                val result = out.toByteArray()
                if (result.isNotEmpty()) result else null
            } catch (e2: Exception) {
                null
            }
        }
    }

    /**
     * Parses PDF text operators between BT and ET markers.
     */
    private fun extractTextFromContentStream(content: String, outText: StringBuilder) {
        val btIdx = content.indexOf("BT")
        if (btIdx == -1) return

        var pos = 0
        while (pos < content.length) {
            val bt = content.indexOf("BT", pos)
            if (bt == -1) break
            val et = content.indexOf("ET", bt)
            if (et == -1) break

            val textBlock = content.substring(bt + 2, et)
            parseTextBlock(textBlock, outText)
            pos = et + 2
        }
    }

    /**
     * Parses operators: Tj, TJ, ', " and hex strings <...>.
     */
    private fun parseTextBlock(block: String, outText: StringBuilder) {
        var i = 0
        while (i < block.length) {
            val ch = block[i]
            if (ch == '(') {
                // Literal string ( ... )
                val sb = StringBuilder()
                var depth = 1
                i++
                while (i < block.length && depth > 0) {
                    val c = block[i]
                    if (c == '\\' && i + 1 < block.length) {
                        val next = block[i + 1]
                        when (next) {
                            'n' -> sb.append('\n')
                            'r' -> sb.append('\r')
                            't' -> sb.append('\t')
                            'b' -> sb.append('\b')
                            'f' -> sb.append('\u000C')
                            '(', ')', '\\' -> sb.append(next)
                            in '0'..'7' -> {
                                // Octal escape
                                var oct = "$next"
                                if (i + 2 < block.length && block[i + 2] in '0'..'7') {
                                    oct += block[i + 2]
                                    if (i + 3 < block.length && block[i + 3] in '0'..'7') {
                                        oct += block[i + 3]
                                    }
                                }
                                sb.append(oct.toInt(8).toChar())
                                i += oct.length
                            }
                            else -> sb.append(next)
                        }
                        i += 2
                        continue
                    } else if (c == '(') {
                        depth++
                    } else if (c == ')') {
                        depth--
                        if (depth == 0) {
                            i++
                            break
                        }
                    }
                    sb.append(c)
                    i++
                }
                outText.append(sb.toString()).append(" ")
            } else if (ch == '<' && i + 1 < block.length && block[i + 1] != '<') {
                // Hex string <...>
                val closeIdx = block.indexOf('>', i)
                if (closeIdx != -1) {
                    val hex = block.substring(i + 1, closeIdx).replace("\\s".toRegex(), "")
                    val sb = StringBuilder()
                    var h = 0
                    while (h + 1 < hex.length) {
                        val byteVal = hex.substring(h, h + 2).toIntOrNull(16)
                        if (byteVal != null && byteVal in 32..126) {
                            sb.append(byteVal.toChar())
                        }
                        h += 2
                    }
                    if (sb.isNotEmpty()) {
                        outText.append(sb.toString()).append(" ")
                    }
                    i = closeIdx + 1
                } else {
                    i++
                }
            } else {
                i++
            }
        }
    }

    private fun findBytes(source: ByteArray, target: ByteArray, startIndex: Int): Int {
        if (target.isEmpty() || startIndex >= source.size) return -1
        outer@ for (i in startIndex..(source.size - target.size)) {
            for (j in target.indices) {
                if (source[i + j] != target[j]) continue@outer
            }
            return i
        }
        return -1
    }

    /**
     * Cleans whitespace, removes non-printable characters, and normalizes linebreaks.
     */
    fun cleanText(input: String): String {
        return input
            .replace("\\r\\n".toRegex(), "\n")
            .replace("\\r".toRegex(), "\n")
            .replace("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

    /**
     * Splits text into manageable chunks respecting sentence/word boundaries.
     */
    fun chunkText(text: String, chunkSize: Int = CHUNK_SIZE_CHARS): List<String> {
        if (text.length <= chunkSize) return listOf(text)

        val chunks = mutableListOf<String>()
        var start = 0
        while (start < text.length) {
            val end = (start + chunkSize).coerceAtMost(text.length)
            var breakPoint = end
            if (end < text.length) {
                val lastPeriod = text.lastIndexOf('.', end)
                val lastSpace = text.lastIndexOf(' ', end)
                breakPoint = if (lastPeriod > start + (chunkSize / 2)) {
                    lastPeriod + 1
                } else if (lastSpace > start + (chunkSize / 2)) {
                    lastSpace + 1
                } else {
                    end
                }
            }
            val chunk = text.substring(start, breakPoint).trim()
            if (chunk.isNotEmpty()) {
                chunks.add(chunk)
            }
            start = breakPoint
        }
        return chunks
    }
}
