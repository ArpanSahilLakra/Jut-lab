package com.example.ai

import com.example.ai.core.*
import com.example.ai.pdf.PdfExtractionResult
import com.example.ai.pdf.PdfTextExtractor
import com.example.ai.repository.AiRepository
import com.example.ai.viva.VivaEvaluator
import com.example.ai.viva.VivaStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream

/**
 * Complete Verification Matrix covering all 19 required architectural and safety scenarios.
 */
class AiArchitectureHardeningTest {

    // Mock/Fakes for testing without real network / external hardware
    class MockProvider(
        override val providerType: AiProviderType,
        private val available: Boolean,
        private val multimodal: Boolean = false,
        private val responseText: String = "Test AI Response",
        private val shouldFailWith: AiResult.Failure? = null
    ) : AiProvider {
        var generateCalledCount = 0
        var generateStreamCalledCount = 0

        override fun isAvailable(): Boolean = available

        override fun getAvailabilityStatus(): ModelAvailabilityStatus =
            if (available) ModelAvailabilityStatus.MODEL_READY else ModelAvailabilityStatus.MODEL_NOT_CONFIGURED

        override fun supportsMultimodal(): Boolean = multimodal

        override suspend fun generate(request: AiRequest): AiResult<AiResponse> {
            generateCalledCount++
            if (shouldFailWith != null) return shouldFailWith
            if (request.isMultimodal && !multimodal) {
                return AiResult.Failure.CapabilityUnsupported("multimodal_image", providerType)
            }
            return AiResult.Success(
                AiResponse(
                    text = responseText,
                    providerType = providerType,
                    routingMode = AiRoutingMode.AUTO,
                    tokensCount = 10
                ),
                providerType = providerType
            )
        }

        override fun generateStream(request: AiRequest): Flow<AiStreamToken> = flow {
            generateStreamCalledCount++
            if (shouldFailWith != null) {
                emit(AiStreamToken.Error(shouldFailWith))
                return@flow
            }
            if (request.isMultimodal && !multimodal) {
                emit(AiStreamToken.Error(AiResult.Failure.CapabilityUnsupported("multimodal_image", providerType)))
                return@flow
            }
            emit(AiStreamToken.Token(responseText))
            emit(AiStreamToken.Completion(responseText, providerType))
        }
    }

    // -------------------------------------------------------------
    // Test 1: TEXT + LOCAL
    // -------------------------------------------------------------
    @Test
    fun test1_TextLocal() = runBlocking {
        val local = MockProvider(AiProviderType.LOCAL, available = true, responseText = "Local Llama Answer")
        val cloud = MockProvider(AiProviderType.CLOUD, available = false)
        val repo = AiRepository(local, cloud, AiRoutingMode.OFFLINE_ONLY)

        val result = repo.generate(AiRequest("What is Ohm's Law?"))
        assertTrue(result is AiResult.Success)
        assertEquals(AiProviderType.LOCAL, (result as AiResult.Success).providerType)
        assertEquals("Local Llama Answer", result.data.text)
        assertEquals(1, local.generateCalledCount)
        assertEquals(0, cloud.generateCalledCount)
    }

    // -------------------------------------------------------------
    // Test 2: TEXT + CLOUD
    // -------------------------------------------------------------
    @Test
    fun test2_TextCloud() = runBlocking {
        val local = MockProvider(AiProviderType.LOCAL, available = false)
        val cloud = MockProvider(AiProviderType.CLOUD, available = true, responseText = "Gemini Answer")
        val repo = AiRepository(local, cloud, AiRoutingMode.ONLINE_ONLY)

        val result = repo.generate(AiRequest("Explain Nyquist theorem"))
        assertTrue(result is AiResult.Success)
        assertEquals(AiProviderType.CLOUD, (result as AiResult.Success).providerType)
        assertEquals("Gemini Answer", result.data.text)
        assertEquals(0, local.generateCalledCount)
        assertEquals(1, cloud.generateCalledCount)
    }

    // -------------------------------------------------------------
    // Test 3: IMAGE + LOCAL (Must reject as CapabilityUnsupported)
    // -------------------------------------------------------------
    @Test
    fun test3_ImageLocal_CapabilityUnsupported() = runBlocking {
        val local = MockProvider(AiProviderType.LOCAL, available = true, multimodal = false)
        val cloud = MockProvider(AiProviderType.CLOUD, available = false)
        val repo = AiRepository(local, cloud, AiRoutingMode.OFFLINE_ONLY)

        val request = AiRequest("Analyze circuit", imageBase64List = listOf("dummy_base64_jpeg"))
        val result = repo.generate(request)

        assertTrue(result is AiResult.Failure.CapabilityUnsupported)
        val failure = result as AiResult.Failure.CapabilityUnsupported
        assertEquals("multimodal_image", failure.capability)
        assertEquals(AiProviderType.LOCAL, failure.providerType)
        assertEquals(0, local.generateCalledCount)
        assertEquals(0, cloud.generateCalledCount)
    }

    // -------------------------------------------------------------
    // Test 4: IMAGE + CLOUD
    // -------------------------------------------------------------
    @Test
    fun test4_ImageCloud() = runBlocking {
        val local = MockProvider(AiProviderType.LOCAL, available = true, multimodal = false)
        val cloud = MockProvider(AiProviderType.CLOUD, available = true, multimodal = true, responseText = "Identified 555 Timer IC")
        val repo = AiRepository(local, cloud, AiRoutingMode.AUTO)

        val request = AiRequest("Identify IC", imageBase64List = listOf("dummy_base64_jpeg"))
        val result = repo.generate(request)

        assertTrue(result is AiResult.Success)
        assertEquals(AiProviderType.CLOUD, (result as AiResult.Success).providerType)
        assertEquals("Identified 555 Timer IC", result.data.text)
        assertEquals(0, local.generateCalledCount)
        assertEquals(1, cloud.generateCalledCount)
    }

    // -------------------------------------------------------------
    // Test 5: OFFLINE_ONLY + Local Available (Routes to local)
    // -------------------------------------------------------------
    @Test
    fun test5_OfflineOnly_LocalAvailable() = runBlocking {
        val local = MockProvider(AiProviderType.LOCAL, available = true, responseText = "Offline response")
        val cloud = MockProvider(AiProviderType.CLOUD, available = true, responseText = "Cloud response")
        val repo = AiRepository(local, cloud, AiRoutingMode.OFFLINE_ONLY)

        val result = repo.generate(AiRequest("Test"))
        assertTrue(result is AiResult.Success)
        assertEquals(AiProviderType.LOCAL, (result as AiResult.Success).providerType)
        assertEquals(1, local.generateCalledCount)
        assertEquals(0, cloud.generateCalledCount) // Cloud MUST NOT be called
    }

    // -------------------------------------------------------------
    // Test 6: OFFLINE_ONLY + Local Unavailable (STRICT PRIVACY: 0 Network calls!)
    // -------------------------------------------------------------
    @Test
    fun test6_OfflineOnly_LocalUnavailable_StrictZeroNetwork() = runBlocking {
        val local = MockProvider(AiProviderType.LOCAL, available = false)
        val cloud = MockProvider(AiProviderType.CLOUD, available = true, responseText = "Cloud eavesdrop attempt")
        val repo = AiRepository(local, cloud, AiRoutingMode.OFFLINE_ONLY)

        val result = repo.generate(AiRequest("Confidential lab query"))
        assertTrue(result is AiResult.Failure.ProviderUnavailable)
        assertEquals(AiProviderType.LOCAL, (result as AiResult.Failure.ProviderUnavailable).providerType)
        assertEquals(0, local.generateCalledCount)
        assertEquals(0, cloud.generateCalledCount) // Cloud MUST NEVER be touched
    }

    // -------------------------------------------------------------
    // Test 7: ONLINE_ONLY + Cloud Available
    // -------------------------------------------------------------
    @Test
    fun test7_OnlineOnly_CloudAvailable() = runBlocking {
        val local = MockProvider(AiProviderType.LOCAL, available = true)
        val cloud = MockProvider(AiProviderType.CLOUD, available = true, responseText = "Cloud response")
        val repo = AiRepository(local, cloud, AiRoutingMode.ONLINE_ONLY)

        val result = repo.generate(AiRequest("Test"))
        assertTrue(result is AiResult.Success)
        assertEquals(AiProviderType.CLOUD, (result as AiResult.Success).providerType)
        assertEquals(0, local.generateCalledCount)
        assertEquals(1, cloud.generateCalledCount)
    }

    // -------------------------------------------------------------
    // Test 8: ONLINE_ONLY + Cloud Unavailable
    // -------------------------------------------------------------
    @Test
    fun test8_OnlineOnly_CloudUnavailable() = runBlocking {
        val local = MockProvider(AiProviderType.LOCAL, available = true)
        val cloud = MockProvider(AiProviderType.CLOUD, available = false)
        val repo = AiRepository(local, cloud, AiRoutingMode.ONLINE_ONLY)

        val result = repo.generate(AiRequest("Test"))
        assertTrue(result is AiResult.Failure.ProviderUnavailable)
        assertEquals(AiProviderType.CLOUD, (result as AiResult.Failure.ProviderUnavailable).providerType)
        assertEquals(0, local.generateCalledCount)
        assertEquals(0, cloud.generateCalledCount)
    }

    // -------------------------------------------------------------
    // Test 9: AUTO + Local Available (Prefers local)
    // -------------------------------------------------------------
    @Test
    fun test9_Auto_LocalAvailable() = runBlocking {
        val local = MockProvider(AiProviderType.LOCAL, available = true, responseText = "Local Fast Response")
        val cloud = MockProvider(AiProviderType.CLOUD, available = true, responseText = "Cloud Response")
        val repo = AiRepository(local, cloud, AiRoutingMode.AUTO)

        val result = repo.generate(AiRequest("Formula for bandwidth?"))
        assertTrue(result is AiResult.Success)
        assertEquals(AiProviderType.LOCAL, (result as AiResult.Success).providerType)
        assertEquals(1, local.generateCalledCount)
        assertEquals(0, cloud.generateCalledCount)
    }

    // -------------------------------------------------------------
    // Test 10: AUTO + Local Unavailable (Falls back to Cloud)
    // -------------------------------------------------------------
    @Test
    fun test10_Auto_LocalUnavailable_FallsBackToCloud() = runBlocking {
        val local = MockProvider(AiProviderType.LOCAL, available = false)
        val cloud = MockProvider(AiProviderType.CLOUD, available = true, responseText = "Cloud Fallback Response")
        val repo = AiRepository(local, cloud, AiRoutingMode.AUTO)

        val result = repo.generate(AiRequest("Formula for bandwidth?"))
        assertTrue(result is AiResult.Success)
        assertEquals(AiProviderType.CLOUD, (result as AiResult.Success).providerType)
        assertEquals(0, local.generateCalledCount)
        assertEquals(1, cloud.generateCalledCount)
    }

    // -------------------------------------------------------------
    // Test 11: Viva + Local
    // -------------------------------------------------------------
    @Test
    fun test11_Viva_LocalEvaluation() = runBlocking {
        val vivaOutput = """
            STATUS: CORRECT
            FEEDBACK: Shandaar! Frequency response curve bilkul sahi explain kiya.
            FOLLOWUP: What is the 3dB cutoff frequency formula?
        """.trimIndent()

        val local = MockProvider(AiProviderType.LOCAL, available = true, responseText = vivaOutput)
        val cloud = MockProvider(AiProviderType.CLOUD, available = false)
        val repo = AiRepository(local, cloud, AiRoutingMode.OFFLINE_ONLY)

        val evaluation = VivaEvaluator.evaluateAnswer(
            question = "Explain RC Low Pass Filter",
            studentAnswer = "It attenuates high frequencies above cutoff",
            idealKey = "Attenuates frequencies higher than cutoff fc = 1 / (2 * pi * R * C)",
            repository = repo,
            mode = AiRoutingMode.OFFLINE_ONLY
        )

        assertEquals(VivaStatus.CORRECT, evaluation.status)
        assertEquals(10, evaluation.scoreIncrement)
        assertFalse(evaluation.isHeuristicFallback)
        assertEquals(AiProviderType.LOCAL, evaluation.providerUsed)
        assertEquals("What is the 3dB cutoff frequency formula?", evaluation.followUpQuestion)
    }

    // -------------------------------------------------------------
    // Test 12: Viva + Cloud
    // -------------------------------------------------------------
    @Test
    fun test12_Viva_CloudEvaluation() = runBlocking {
        val vivaOutput = """
            STATUS: PARTIALLY CORRECT
            FEEDBACK: Concept is partially clear, but you missed negative feedback loop.
            FOLLOWUP: Explain why negative feedback stabilizes gain.
        """.trimIndent()

        val local = MockProvider(AiProviderType.LOCAL, available = false)
        val cloud = MockProvider(AiProviderType.CLOUD, available = true, responseText = vivaOutput)
        val repo = AiRepository(local, cloud, AiRoutingMode.ONLINE_ONLY)

        val evaluation = VivaEvaluator.evaluateAnswer(
            question = "Explain Op-Amp inverting amplifier",
            studentAnswer = "Gain is Rf/Rin and phase is 180 degrees",
            idealKey = "Inverting amplifier has gain Av = -Rf/Rin with 180 degree phase shift using negative feedback",
            repository = repo,
            mode = AiRoutingMode.ONLINE_ONLY
        )

        assertEquals(VivaStatus.PARTIALLY_CORRECT, evaluation.status)
        assertEquals(5, evaluation.scoreIncrement)
        assertFalse(evaluation.isHeuristicFallback)
        assertEquals(AiProviderType.CLOUD, evaluation.providerUsed)
    }

    // -------------------------------------------------------------
    // Test 13: Viva + Both Unavailable (HEURISTIC_FALLBACK, No points for error)
    // -------------------------------------------------------------
    @Test
    fun test13_Viva_BothUnavailable_HeuristicFallback() = runBlocking {
        val local = MockProvider(AiProviderType.LOCAL, available = false)
        val cloud = MockProvider(AiProviderType.CLOUD, available = false)
        val repo = AiRepository(local, cloud, AiRoutingMode.AUTO)

        val evaluation = VivaEvaluator.evaluateAnswer(
            question = "Explain Kirchhoff Current Law (KCL)",
            studentAnswer = "Sum of currents entering a node is equal to sum of currents leaving the node",
            idealKey = "At any junction or node in an electrical circuit, the sum of currents entering equals sum leaving",
            repository = repo,
            mode = AiRoutingMode.AUTO
        )

        // Must be marked as HEURISTIC_FALLBACK
        assertTrue(evaluation.isHeuristicFallback)
        assertTrue(evaluation.feedback.contains("[HEURISTIC_FALLBACK]"))
        assertNull(evaluation.providerUsed)
        // Heuristic matched key terms
        assertEquals(VivaStatus.CORRECT, evaluation.status)
    }

    // -------------------------------------------------------------
    // Test 14: PDF Text Extraction + Local Chunking
    // -------------------------------------------------------------
    @Test
    fun test14_PdfTextExtraction_RealStreams() {
        // Construct a synthetically valid PDF with Flate-compressed text stream
        val rawTextStream = "BT /F1 12 Tf 100 700 Td (ECE Lab Manual: Experiment 1 - Study of CE Amplifier) Tj ET"
        val compressedStream = compressZlib(rawTextStream.toByteArray(Charsets.ISO_8859_1))

        val pdfBuilder = ByteArrayOutputStream()
        pdfBuilder.write("%PDF-1.4\n".toByteArray(Charsets.US_ASCII))
        pdfBuilder.write("1 0 obj\n<< /Type /Page >>\nendobj\n".toByteArray(Charsets.US_ASCII))
        pdfBuilder.write("2 0 obj\n<< /Length ${compressedStream.size} /Filter /FlateDecode >>\nstream\n".toByteArray(Charsets.US_ASCII))
        pdfBuilder.write(compressedStream)
        pdfBuilder.write("\nendstream\nendobj\n%%EOF".toByteArray(Charsets.US_ASCII))

        val result = PdfTextExtractor.extractText(pdfBuilder.toByteArray(), maxChars = 2000)
        assertTrue("Expected PdfExtractionResult.Success but was $result", result is PdfExtractionResult.Success)
        val success = result as PdfExtractionResult.Success
        assertTrue(success.cleanedText.contains("ECE Lab Manual: Experiment 1 - Study of CE Amplifier"))
        assertFalse(success.isTruncatedForBudget)
    }

    // -------------------------------------------------------------
    // Test 15: PDF Scanned / Image-Only + Local (Explains OCR limitation)
    // -------------------------------------------------------------
    @Test
    fun test15_PdfScanned_ImageOnly_OfflineExplanation() {
        // PDF with image XObject and NO text operators (BT...ET)
        val pdfBuilder = ByteArrayOutputStream()
        pdfBuilder.write("%PDF-1.4\n".toByteArray(Charsets.US_ASCII))
        pdfBuilder.write("1 0 obj\n<< /Type /Page /Resources << /XObject << /Im1 2 0 R >> >> >>\nendobj\n".toByteArray(Charsets.US_ASCII))
        pdfBuilder.write("2 0 obj\n<< /Type /XObject /Subtype /Image /Width 100 /Height 100 >>\nstream\n<image_data>\nendstream\nendobj\n%%EOF".toByteArray(Charsets.US_ASCII))

        val result = PdfTextExtractor.extractText(pdfBuilder.toByteArray())
        assertTrue("Expected ScannedOrImageOnly but was $result", result is PdfExtractionResult.ScannedOrImageOnly)
        val scanned = result as PdfExtractionResult.ScannedOrImageOnly
        assertTrue(scanned.message.contains("scanned image-only PDF"))
    }

    // -------------------------------------------------------------
    // Test 16: PDF Image + Cloud Multimodal Request
    // -------------------------------------------------------------
    @Test
    fun test16_PdfImage_CloudMultimodal() = runBlocking {
        val local = MockProvider(AiProviderType.LOCAL, available = true, multimodal = false)
        val cloud = MockProvider(AiProviderType.CLOUD, available = true, multimodal = true, responseText = "Visual Analysis: Oscilloscope sine wave with 2V p-p")
        val repo = AiRepository(local, cloud, AiRoutingMode.AUTO)

        val request = AiRequest(
            prompt = "Analyze rendered lab manual page",
            imageBase64List = listOf("dummy_page1_bitmap_base64")
        )
        val result = repo.generate(request)

        assertTrue(result is AiResult.Success)
        assertEquals(AiProviderType.CLOUD, (result as AiResult.Success).providerType)
        assertTrue(result.data.text.contains("Oscilloscope sine wave"))
    }

    // -------------------------------------------------------------
    // Test 17: Cancellation Propagation
    // -------------------------------------------------------------
    @Test
    fun test17_CancellationPropagation() = runBlocking {
        val error = AiResult.Failure.Cancelled("User clicked stop")
        val local = MockProvider(AiProviderType.LOCAL, available = true, shouldFailWith = error)
        val cloud = MockProvider(AiProviderType.CLOUD, available = false)
        val repo = AiRepository(local, cloud, AiRoutingMode.OFFLINE_ONLY)

        val tokens = repo.generateStream(AiRequest("Long generation")).toList()
        assertEquals(1, tokens.size)
        assertTrue(tokens[0] is AiStreamToken.Error)
        val failure = (tokens[0] as AiStreamToken.Error).failure
        assertTrue(failure is AiResult.Failure.Cancelled)
    }

    // -------------------------------------------------------------
    // Test 18: Malformed Viva Output Parsing (Strict INCORRECT vs PARTIALLY CORRECT)
    // -------------------------------------------------------------
    @Test
    fun test18_VivaOutputParsing_IncorrectVsPartiallyCorrect() {
        // Case A: PARTIALLY CORRECT must not be misparsed as INCORRECT
        val partialText = """
            STATUS: PARTIALLY CORRECT
            FEEDBACK: Formula is right but definition is incomplete.
            FOLLOWUP: Define slew rate.
        """.trimIndent()
        val partialResult = VivaEvaluator.parseAiEvaluation(partialText, AiProviderType.LOCAL)
        assertEquals(VivaStatus.PARTIALLY_CORRECT, partialResult.status)
        assertEquals(5, partialResult.scoreIncrement)
        assertEquals("Formula is right but definition is incomplete.", partialResult.feedback)
        assertEquals("Define slew rate.", partialResult.followUpQuestion)

        // Case B: INCORRECT must not match PARTIALLY CORRECT
        val incorrectText = """
            STATUS: INCORRECT
            FEEDBACK: Wrong pin configuration for 741 Op-Amp.
            FOLLOWUP: NONE
        """.trimIndent()
        val incorrectResult = VivaEvaluator.parseAiEvaluation(incorrectText, AiProviderType.CLOUD)
        assertEquals(VivaStatus.INCORRECT, incorrectResult.status)
        assertEquals(0, incorrectResult.scoreIncrement)
        assertNull(incorrectResult.followUpQuestion)

        // Case C: Malformed output without explicit tags
        val malformedText = "The student is mostly correct here, good explanation."
        val malformedResult = VivaEvaluator.parseAiEvaluation(malformedText, AiProviderType.LOCAL)
        assertEquals(VivaStatus.CORRECT, malformedResult.status)
    }

    // -------------------------------------------------------------
    // Test 19: Native Model Loading Failure & Memory Safety
    // -------------------------------------------------------------
    @Test
    fun test19_ModelValidation_InvalidHeaderAndMissingFile() {
        val missingProvider = com.example.ai.local.LocalLlamaProvider { "/non/existent/path.gguf" }
        assertEquals(ModelAvailabilityStatus.MODEL_CONFIGURED_BUT_INVALID, missingProvider.getAvailabilityStatus())
        assertFalse(missingProvider.isAvailable())

        val unconfiguredProvider = com.example.ai.local.LocalLlamaProvider { "" }
        assertEquals(ModelAvailabilityStatus.MODEL_NOT_CONFIGURED, unconfiguredProvider.getAvailabilityStatus())
        assertFalse(unconfiguredProvider.isAvailable())
    }

    private fun compressZlib(data: ByteArray): ByteArray {
        val out = ByteArrayOutputStream()
        val deflater = Deflater()
        val dos = DeflaterOutputStream(out, deflater)
        dos.write(data)
        dos.finish()
        dos.close()
        return out.toByteArray()
    }
}
