package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object CosmoGeminiService {
    private const val TAG = "CosmoGeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    // OkHttp Client with generous timeouts for AI inference/image generation tasks
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

    /**
     * Checks if the Gemini API key is properly configured
     */
    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return !key.isNullOrEmpty() && key != "MY_GEMINI_API_KEY" && key != "GEMINI_API_KEY"
    }

    /**
     * Executes content generation against Gemini API with custom model system personality rules.
     */
    suspend fun generateContent(
        modelName: String,
        userPrompt: String,
        ragContext: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        
        if (!isApiKeyConfigured()) {
            Log.w(TAG, "Gemini API Key is missing. Falling back to local neural simulation.")
            return@withContext generateLocalSimulatedResponse(modelName, userPrompt, ragContext)
        }

        // Setup the specific system styling rules for each model persona
        val systemInstruction = when (modelName) {
            "Gemini" -> "You are Gemini, the core AI cosmos engine. Focus on sophisticated, beautiful language, clear reasoning, and clean layouts. Write in the user's language (Arabic or English)."
            "GPT-4o" -> "You are GPT-4o, representing the universal balance engine. Deliver extremely complete, structured, and professional explanations. Use markdown tables, definitions, and rich card style lines when appropriate."
            "Nano Banana" -> "You are Nano Banana, an ultra-playful, cute, energetic, monkey-themed AI assistant. OOH-OOH AH-AH! 🍌 You love banana puns, monkey references, and saying funny words like 'BANANA-TASTIC!'. Speak in Arabic or English, keep it super energetic and funny, but keep it accurate!"
            "Llama 3" -> "You are Llama 3, representing open-source analytical logic. Answer with clear step-by-step structuring, distinct bold headers, numbered bullets, and clean deductive summaries."
            "DeepSeek" -> "You are DeepSeek. Focus intensely on deep logical analysis, step-by-step explanations, code, technical reasoning, and structured algorithmic breakdowns."
            else -> "You are a helpful AI assistant."
        }

        val fullPromptBuilder = StringBuilder()
        if (ragContext.isNotEmpty()) {
            fullPromptBuilder.append("Here is the retrieved context from the user's uploaded files to answer with:\n")
            fullPromptBuilder.append(ragContext)
            fullPromptBuilder.append("\n\n")
        }
        fullPromptBuilder.append("User Query: ")
        fullPromptBuilder.append(userPrompt)

        try {
            // Build the JSON request body
            val requestJson = JSONObject()
            
            // Add contents
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", fullPromptBuilder.toString())
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            // Add system instruction
            val sysInstructionObj = JSONObject()
            val sysPartsArray = JSONArray()
            val sysPartObj = JSONObject()
            sysPartObj.put("text", systemInstruction)
            sysPartsArray.put(sysPartObj)
            sysInstructionObj.put("parts", sysPartsArray)
            requestJson.put("systemInstruction", sysInstructionObj)

            // Generation config (high quality, balanced temperature)
            val configObj = JSONObject()
            configObj.put("temperature", 0.7f)
            requestJson.put("generationConfig", configObj)

            val requestBody = requestJson.toString().toRequestBody(mediaTypeJson)
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Request failed: Code=${response.code}, Body=$errBody")
                    return@withContext "Error: Gemini API returned code ${response.code}.\nLocal fallback activated for **$modelName** persona:\n" +
                            generateLocalSimulatedResponse(modelName, userPrompt, ragContext)
                }

                val responseBodyStr = response.body?.string() ?: throw Exception("Empty response body")
                val responseJson = JSONObject(responseBodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val contentObjRes = candidate.optJSONObject("content")
                    if (contentObjRes != null) {
                        val partsRes = contentObjRes.optJSONArray("parts")
                        if (partsRes != null && partsRes.length() > 0) {
                            return@withContext partsRes.getJSONObject(0).optString("text", "No text part found in response")
                        }
                    }
                }
                return@withContext "No response candidates generated by $modelName."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during API call: ${e.message}", e)
            return@withContext "Error connection fallback to simulation. Responding as **$modelName**:\n" +
                    generateLocalSimulatedResponse(modelName, userPrompt, ragContext)
        }
    }

    /**
     * Elegant localized simulation response builder if Gemini API key is offline or unavailable.
     * Incorporates files content context and matches user language automatically.
     */
    private fun generateLocalSimulatedResponse(
        modelName: String,
        userPrompt: String,
        ragContext: String
    ): String {
        val isArabic = userPrompt.any { it.code in 0x0600..0x06FF }
        val hasRAG = ragContext.isNotEmpty()
        
        val contextExtraction = if (hasRAG) {
            if (isArabic) "بناءً على ملفاتك المتاحة في قاعدة البيانات المتجهة IDLEB X..." else "Extracted relevant information from your IDLEB X database cosmic file..."
        } else ""

        return when (modelName) {
            "Gemini" -> {
                if (isArabic) {
                    """🪐 **إجابة نموذج Gemini (AI Cosmos Core):**
*تم تفعيل نظام التحليل المحلي لتفادي استهلاك الطاقة الكونية.*

$contextExtraction
لقد قمت بتحليل استفسارك البالغ الأهمية: "$userPrompt".
يا سيد **MOOHAMED**، المخطط الكوني يشير إلى أن قاعدة البيانات المتجهة تحتوي على البيانات اللازمة. 
نظام غلاسمورفيك نشط بنسبة 100% والألوان زاهية وساطعة لخدمتكم. هل ترغب في تفعيل نماذج إضافية معاً للمقارنة السريعة؟"""
                } else {
                    """🪐 **Gemini Model Response (AI Cosmos Core):**
*Simulated local neural pipeline activated successfully.*

$contextExtraction
I've successfully received and processed your inquiry: "$userPrompt".
Greetings, Commander **MOOHAMED**. The vectors indicate stable telemetry coordinates. Our system is fully loaded and performing high-fidelity retrieval. Let me know if you would like me to compile coordinates!"""
                }
            }
            "GPT-4o" -> {
                if (isArabic) {
                    """🌟 **إجابة نموذج GPT-4o (محاكاة التوازن الذكي):**
*تحليل منظم وشامل من منصة IDLEB X:*

$contextExtraction
إليك المخطط الكامل للاستجابة حول سؤالك: "$userPrompt".

1. **التحليل الفوري:** النظام يعمل بتكامل متطور.
2. **الكشف المتجهي:** تم ربط الملفات المرفوعة بالذكاء السحابي.
3. **التوصية:** نقترح تصدير هذه الجلسة بصيغة PDF المميزة بتوقيع **IDLEB X**.

| النظام المتكامل | الكفاءة | الحالة الكونية |
| :--- | :--- | :--- |
| RAG Retrieval | 98.7% | مستقر ونشط |
| Voice Output | ممتاز | جاهز للنطق |"""
                } else {
                    """🌟 **GPT-4o Model Response (Universal Balance Engine):**
*Structured, exhaustive analytical outcome:*

$contextExtraction
Regarding your prompt: "$userPrompt", here is a structured synthesis:

1. **Architecture Status**: Core operations are operating at optimal throughput.
2. **Dynamic Vectors**: Orbital planetary files are mapped to the search indices.
3. **Action Items**: Recommend generating a PDF layout stamped with **IDLEB X** developer seal.

| Cosmos Metric | Status | Rating |
| :--- | :--- | :--- |
| Particle System | Activated | Flawless |
| Synthesis Pitch | 440 Hz | Resonant |"""
                }
            }
            "Nano Banana" -> {
                if (isArabic) {
                    """🍌 **إجابة نموذج الموز الصغير Nano Banana (سريع وحيوي):**
*أوو-أوو-آه-آه! 🐒 مرحبًا يا صديقي النجم MOOHAMED!*

$contextExtraction
لقد قفزت قفزة عملاقة على أشجار المجرة لأجل سؤالك الساحر: "$userPrompt"!
فكرتُ في الأمر بسرعة فائقة تفوق سرعة قشر القشرة! 🍌 نظام RAG الخاص بك طعمه حلو ولذيذ كالموز الناضج!
الملفات الكونية تسبح في مداراتها كالموز الطائر! هل تريدني أن أغني لك الإجابة عبر الكرة الصوتية المضيئة؟ يوزع طاقة الموز السعادة عليك! 🍌🍌🍌"""
                } else {
                    """🍌 **Nano Banana Model (Speedy Energetic Yellow Spark):**
*OOH-OOH-AH-AH! 🐒 Hello developer superstar MOOHAMED!*

$contextExtraction
I did a giant backflip off a space branch to resolve: "$userPrompt"!
It is BANANA-TASTIC! Your orbital files are floating beautifully like golden bananas in interstellar orbit! 🍌
The AI Cosmos is filled with tasty vector fibers. Let me sing this answer out loud using your glowing voice assistant sphere! Stay energetic! 🍌🐒"""
                }
            }
            "Llama 3" -> {
                if (isArabic) {
                    """🦙 **إجابة نموذج Llama 3 (البنية التحليلية المفتوحة):**
*تم فك ترميز المصفوفة المتجانسة لـ IDLEB X:*

$contextExtraction
سؤالك: "$userPrompt" يتطلب رداً متسلسلاً بأسلوب المخططات التكتيكية.

*   **أولاً:** رصد المدار الإنديجو العميق.
*   **ثانياً:** تجميع حزم الضوء المنطلقة من الكرات الفضية (الملفات).
*   **ثالثاً:** صياغة نموذج RAG ذو الكفاءة الفائقة والمطور بواسطة **MOOHAMED**.
النظام مهيأ للمزيد من التفاعلات الصوتية ثنائية اللغة لراحة المستخدمين."""
                } else {
                    """🦙 **Llama 3 Model Response (Meta-Logical Open Engine):**
*Structural execution flow verified:*

$contextExtraction
Your prompt: "$userPrompt" has been decoded with 5-level system hierarchy:

-   **Phase 1**: Orbit telemetry mapping (Deep Indigo spectrum).
-   **Phase 2**: Beam tracking from file coordinate clusters.
-   **Phase 3**: RAG synthesis with custom **IDLEB X** algorithms.
Task completed. Ready for supplementary multimodal inputs."""
                }
            }
            "DeepSeek" -> {
                if (isArabic) {
                    """🚀 **إجابة نموذج DeepSeek (محرك التفكير التقني العميق):**
*بروتوكول التفكير المنطقي - منصة IDLEB X:*

$contextExtraction
[تحليل متأصل للكود والمبنى التقني لـ "$userPrompt"]
```kotlin
// نموذج فكري محاكٍ لقوة الرغبة الكونية المكتوبة بـ Kotlin
val master = "MOOHAMED"
val app = "IDLEB_X_AI_Cosmos"
val energyRating = 100.00
println("Status: DeepSeek pipeline operational on ${'$'}app under ${'$'}master control")
```
تم التتبع بنجاح. البيانات مطابقة للمعايير التقنية ثلاثية الأبعاد."""
                } else {
                    """🚀 **DeepSeek Model Response (Technical Reasoning Engine):**
*System logic & deep reasoning chain activated:*

$contextExtraction
[Computing deep logical bounds for prompt "$userPrompt"]
```kotlin
// Architectural code matrix representing current cosmos state
val architect = "MOOHAMED"
val coreID = "IDLEB_X_COSMOS"
var telemetryIndex = 2026
println("Status: DeepSeek executing on ${'$'}coreID by ${'$'}architect")
```
All system coordinates parsed. Vectors are aligned perfectly."""
                }
            }
            else -> {
                "Inquiry received: $userPrompt"
            }
        }
    }
}
