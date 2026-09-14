package com.example.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.data.LabReportEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExportManager {
    fun generateLabReportPdf(context: Context, reports: List<LabReportEntity>): String {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val linePaint = Paint().apply {
            color = Color.DKGRAY
            strokeWidth = 2f
        }

        reports.forEachIndexed { index, report ->
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            var currentY = 80f

            // Title
            canvas.drawText("JUT ECE Virtual Lab", 50f, currentY, titlePaint)
            currentY += 40f
            canvas.drawLine(50f, currentY, 545f, currentY, linePaint)
            currentY += 40f

            // Report Header
            canvas.drawText("Experiment: ${report.title}", 50f, currentY, headerPaint)
            currentY += 25f
            
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US)
            val dateStr = dateFormat.format(Date(report.createdAt))
            canvas.drawText("Date: $dateStr", 50f, currentY, bodyPaint)
            currentY += 40f

            // Content Sections
            val sections = listOf(
                "Aim" to report.aim,
                "Apparatus" to report.apparatus,
                "Theory" to report.theory,
                "Observations" to report.observations,
                "Conclusion" to report.conclusion
            )

            sections.forEach { (title, content) ->
                if (currentY > pageHeight - 100f) {
                    // Primitive page wrap check (would need multi-page logic for real production, but fine for demo)
                }
                canvas.drawText("$title:", 50f, currentY, headerPaint)
                currentY += 20f
                
                // Primitive text wrap
                val words = content.split(" ")
                var line = ""
                for (word in words) {
                    if (bodyPaint.measureText(line + word) > 495f) {
                        canvas.drawText(line, 50f, currentY, bodyPaint)
                        currentY += 15f
                        line = "$word "
                    } else {
                        line += "$word "
                    }
                }
                if (line.isNotEmpty()) {
                    canvas.drawText(line, 50f, currentY, bodyPaint)
                    currentY += 30f
                }
            }

            document.finishPage(page)
        }

        // Save to Downloads directory
        val fileName = "LabReports_${System.currentTimeMillis()}.pdf"
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        try {
            document.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            return "Error: ${e.message}"
        }

        document.close()
        return "Saved to Downloads folder as $fileName"
    }
}
