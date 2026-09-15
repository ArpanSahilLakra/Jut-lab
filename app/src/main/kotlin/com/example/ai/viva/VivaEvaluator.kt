package com.example.ai.viva

import com.example.ai.core.*
import com.example.ai.repository.AiRepository
import java.util.Locale

/**
 * Parsed outcome of an AI Viva evaluation.
 */
data class VivaEvaluationResult(
    val status: VivaStatus,
    val feedback: String,
    val followUpQuestion: String?,
    val scoreIncrement: Int,
    val isHeuristicFallback: Boolean,
    val providerUsed: AiProviderType?
)

enum class VivaStatus {
    CORRECT,
    PARTIALLY_CORRECT,
    INCORRECT,
    ERROR_OCCURRED
}

/**
 * Production-hardened Viva Evaluation Engine.
 * Replaces fragile string matching with strict token regexes and guarantees that
 * AI failures are never scored as student errors.
 */
object VivaEvaluator {

    // Anchored regexes to prevent INCORRECT matching PARTIALLY CORRECT
    private val STATUS_REGEX = Regex("(?i)^\\s*STATUS:\\s*(CORRECT|PARTIALLY\\s*CORRECT|PARTIAL|INCORRECT)\\b", RegexOption.MULTILINE)
    private val FEEDBACK_REGEX = Regex("(?i)FEEDBACK:\\s*([\\s\\S]*?)(?=^\\s*FOLLOWUP:|$)", RegexOption.MULTILINE)
    private val FOLLOWUP_REGEX = Regex("(?i)FOLLOWUP:\\s*([\\s\\S]*?)$", RegexOption.MULTILINE)

    /**
     * Evaluates a student answer using the AI repository with fallback to heuristic evaluation.
     */
    suspend fun evaluateAnswer(
        question: String,
        studentAnswer: String,
        idealKey: String,
        repository: AiRepository,
        mode: AiRoutingMode
    ): VivaEvaluationResult {
        if (studentAnswer.isBlank()) {
            return VivaEvaluationResult(
                status = VivaStatus.INCORRECT,
                feedback = "No answer provided.",
                followUpQuestion = null,
                scoreIncrement = 0,
                isHeuristicFallback = false,
                providerUsed = null
            )
        }

        val prompt = """
            You are a strict and knowledgeable ECE viva examiner at JUT.
            Evaluate this student's response:
            Question: $question
            Ideal Answer Key: $idealKey
            Student Answer: $studentAnswer

            Your output MUST strictly follow this exact format:
            STATUS: <CORRECT | PARTIALLY CORRECT | INCORRECT>
            FEEDBACK: <1-2 sentences of specific technical feedback in Hinglish/English explaining what was right or missing>
            FOLLOWUP: <One brief follow-up viva question, or NONE if answer was complete>
        """.trimIndent()

        val request = AiRequest(
            prompt = prompt,
            systemPrompt = "You are an ECE viva examiner. Follow format strictly.",
            temperature = 0.2f
        )

        val aiResult = repository.generate(request, mode)
        return when (aiResult) {
            is AiResult.Success -> {
                parseAiEvaluation(aiResult.data.text, aiResult.providerType)
            }
            is AiResult.Failure -> {
                // If in OFFLINE mode or both AI unavailable, use clearly labeled HEURISTIC_FALLBACK
                val heuristicStatus = runHeuristicEvaluation(studentAnswer, idealKey)
                val score = when (heuristicStatus) {
                    VivaStatus.CORRECT -> 10
                    VivaStatus.PARTIALLY_CORRECT -> 5
                    VivaStatus.INCORRECT -> 0
                    VivaStatus.ERROR_OCCURRED -> 0
                }
                VivaEvaluationResult(
                    status = heuristicStatus,
                    feedback = "[HEURISTIC_FALLBACK] AI unavailable (${aiResult.message}). Evaluated using offline keyword heuristics.",
                    followUpQuestion = if (heuristicStatus == VivaStatus.PARTIALLY_CORRECT) "Can you explain the core equation or working principle in more detail?" else null,
                    scoreIncrement = score,
                    isHeuristicFallback = true,
                    providerUsed = null
                )
            }
        }
    }

    /**
     * Robust parser for AI evaluation outputs.
     */
    fun parseAiEvaluation(aiText: String, provider: AiProviderType): VivaEvaluationResult {
        val cleanText = aiText.trim()

        // 1. Parse Status safely
        val statusMatch = STATUS_REGEX.find(cleanText)
        val rawStatus = statusMatch?.groupValues?.get(1)?.uppercase(Locale.ROOT)?.trim()

        val status = when (rawStatus) {
            "CORRECT" -> VivaStatus.CORRECT
            "PARTIALLY CORRECT", "PARTIAL" -> VivaStatus.PARTIALLY_CORRECT
            "INCORRECT" -> VivaStatus.INCORRECT
            else -> {
                // Fallback: search for exact word matches if AI omitted 'STATUS:' tag
                if (cleanText.contains("PARTIALLY CORRECT", ignoreCase = true) || cleanText.contains("PARTIAL", ignoreCase = true)) {
                    VivaStatus.PARTIALLY_CORRECT
                } else if (cleanText.contains("INCORRECT", ignoreCase = true)) {
                    VivaStatus.INCORRECT
                } else if (cleanText.contains("CORRECT", ignoreCase = true)) {
                    VivaStatus.CORRECT
                } else {
                    VivaStatus.PARTIALLY_CORRECT
                }
            }
        }

        // 2. Parse Feedback
        val feedbackMatch = FEEDBACK_REGEX.find(cleanText)
        val feedback = feedbackMatch?.groupValues?.get(1)?.trim()
            ?: cleanText.lines().firstOrNull { it.isNotBlank() && !it.startsWith("STATUS:") }
            ?: "Answer evaluated."

        // 3. Parse Follow-up
        val followUpMatch = FOLLOWUP_REGEX.find(cleanText)
        val rawFollowUp = followUpMatch?.groupValues?.get(1)?.trim()
        val followUp = if (!rawFollowUp.isNullOrBlank() && !rawFollowUp.equals("NONE", ignoreCase = true)) {
            rawFollowUp
        } else {
            null
        }

        val score = when (status) {
            VivaStatus.CORRECT -> 10
            VivaStatus.PARTIALLY_CORRECT -> 5
            VivaStatus.INCORRECT -> 0
            VivaStatus.ERROR_OCCURRED -> 0
        }

        return VivaEvaluationResult(
            status = status,
            feedback = feedback,
            followUpQuestion = followUp,
            scoreIncrement = score,
            isHeuristicFallback = false,
            providerUsed = provider
        )
    }

    /**
     * Heuristic keyword-matching evaluation.
     * Explicitly marked as fallback.
     */
    fun runHeuristicEvaluation(answer: String, key: String): VivaStatus {
        val ansLower = answer.lowercase(Locale.ROOT)
        val keyLower = key.lowercase(Locale.ROOT)

        val keywords = keyLower.split("\\s+".toRegex())
            .filter { it.length > 3 }
            .filter { !listOf("with", "that", "this", "from", "when", "which", "where").contains(it) }

        if (keywords.isEmpty()) {
            return if (ansLower.length > 10) VivaStatus.PARTIALLY_CORRECT else VivaStatus.INCORRECT
        }

        var matchCount = 0
        for (word in keywords) {
            if (ansLower.contains(word)) {
                matchCount++
            }
        }

        val ratio = matchCount.toFloat() / keywords.size.toFloat()
        return when {
            ratio >= 0.40f -> VivaStatus.CORRECT
            ratio >= 0.15f -> VivaStatus.PARTIALLY_CORRECT
            else -> VivaStatus.INCORRECT
        }
    }
}
