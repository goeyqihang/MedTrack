package com.qihang.medtrack.data.genai

import com.google.gson.annotations.SerializedName

// ── Request ──────────────────────────────────────────────────────────
data class GeminiRequest(
    @SerializedName("contents") val contents: List<GeminiContent>
)

data class GeminiContent(
    @SerializedName("parts") val parts: List<GeminiPart>
)

data class GeminiPart(
    @SerializedName("text") val text: String
)

// ── Response ─────────────────────────────────────────────────────────
data class GeminiResponse(
    @SerializedName("candidates") val candidates: List<GeminiCandidate>?
)

data class GeminiCandidate(
    @SerializedName("content") val content: GeminiContent?
)

/** Convenience: pull the generated text out of the first candidate. */
fun GeminiResponse.firstTextOrNull(): String? =
    candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
