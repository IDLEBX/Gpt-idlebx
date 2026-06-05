package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CosmoFile
import com.example.data.CosmoMessage
import com.example.data.CosmoRepository
import com.example.data.CosmoGeminiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

data class CosmoUiState(
    val showSplash: Boolean = true,
    val isEnglish: Boolean = false, // Default to Arabic as requested in detailed prompt
    val activeModels: Set<String> = setOf("Gemini"), // Can select multiple models!
    val activePresentationMode: String = "Text", // "Text", "Chart", "Voice"
    val isRecordingOrProcessing: Boolean = false,
    val selectedFileForPreview: CosmoFile? = null,
    
    // RAG Lightbeam animation trigger state
    val laserTriggerTimestamp: Long = 0,
    val matchingFileIds: List<Long> = emptyList(),
    
    // Speech synthesis state
    val isSpeaking: Boolean = false,
    val currentSpokenText: String = "",
    
    // UI Feedback
    val systemMessage: String? = null
)

class CosmoViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {
    
    private val repository = CosmoRepository(application)
    
    private val _uiState = MutableStateFlow(CosmoUiState())
    val uiState: StateFlow<CosmoUiState> = _uiState.asStateFlow()

    // Database Flows
    val filesList = repository.allFiles
    val messagesList = repository.allMessages

    // Local Text-To-Speech
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    init {
        // Initialize Database with awesome mock files
        viewModelScope.launch {
            repository.seedInitialFilesIfEmpty()
        }
        // Initialize Speech engine
        tts = TextToSpeech(application, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            tts?.language = Locale.US
        } else {
            Log.e("CosmoViewModel", "TextToSpeech init failed")
        }
    }

    fun dismissSplash() {
        _uiState.update { it.copy(showSplash = false) }
    }

    fun toggleLanguage() {
        _uiState.update { it.copy(isEnglish = !it.isEnglish) }
    }

    fun toggleModelSelection(modelName: String) {
        _uiState.update { state ->
            val updated = state.activeModels.toMutableSet()
            if (updated.contains(modelName)) {
                if (updated.size > 1) {
                    updated.remove(modelName)
                } else {
                    // Force keeping at least one active model
                    updated.clear()
                    updated.add(modelName)
                }
            } else {
                updated.add(modelName)
            }
            state.copy(activeModels = updated)
        }
    }

    fun setPresentationMode(mode: String) {
        _uiState.update { it.copy(activePresentationMode = mode) }
        if (mode != "Voice") {
            stopSpeaking()
        }
    }

    fun selectFileForPreview(file: CosmoFile?) {
        _uiState.update { it.copy(selectedFileForPreview = file) }
    }

    /**
     * Simulates client-side uploading of specific file formats.
     * Computes random cosmic orbit telemetry coordinates upon addition.
     */
    fun uploadFile(name: String, content: String, formatType: String) {
        viewModelScope.launch {
            val radius = (120..240).random().toFloat()
            val availableColors = listOf("#00F2FE", "#FF007F", "#9D4EDD", "#39FF14", "#FF007F", "#00FFCC")
            val randColor = availableColors.random()
            val symbol = when (formatType) {
                "PDF" -> "PDF"
                "DOCX" -> "DOCX"
                "TXT" -> "TXT"
                "Markdown" -> "MD"
                else -> "IMG"
            }
            
            val sizeKb = (2..50).random()
            val sizeLabel = "$sizeKb KB"

            val newFile = CosmoFile(
                name = name,
                content = content,
                fileType = formatType,
                sizeLabel = sizeLabel,
                orbitRadius = radius,
                colorHex = randColor,
                typeSymbol = symbol
            )

            repository.insertFile(newFile)
            _uiState.update { it.copy(systemMessage = "Uploaded '$name' as orbital planetary sphere!") }
        }
    }

    fun clearAllFiles() {
        viewModelScope.launch {
            repository.clearFiles()
            _uiState.update { it.copy(selectedFileForPreview = null, systemMessage = "All orbital file planets collapsed.") }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatHistory()
            stopSpeaking()
            _uiState.update { it.copy(systemMessage = "Chat cache purged successfully.") }
        }
    }

    fun clearSystemMessage() {
        _uiState.update { it.copy(systemMessage = null) }
    }

    /**
     * Processes full RAG and multimodal parallel API calls for all selected models
     */
    fun submitMessage(prompt: String) {
        if (prompt.isBlank()) return

        _uiState.update { it.copy(isRecordingOrProcessing = true) }

        viewModelScope.launch {
            // 1. Log query from User in database
            val userMsg = CosmoMessage(sender = "User", messageText = prompt)
            repository.insertMessage(userMsg)

            // 2. Perform localized TF-IDF Vector RAG search
            val ragResult = repository.retrieveRelevantContext(prompt)

            // 3. Trigger Laser anims if match fits
            if (ragResult.matchedFileIds.isNotEmpty()) {
                _uiState.update {
                    it.copy(
                        laserTriggerTimestamp = System.currentTimeMillis(),
                        matchingFileIds = ragResult.matchedFileIds
                    )
                }
            }

            // 4. Fire parallel queries to active selected models
            val models = _uiState.value.activeModels.toList()
            models.forEach { model ->
                launch {
                    val aiResponse = CosmoGeminiService.generateContent(
                        modelName = model,
                        userPrompt = prompt,
                        ragContext = ragResult.snippet
                    )

                    // Inject custom mini JSON chart metrics if user asks about ratings, status or statistics
                    val chartJson = if (prompt.contains("نسبة") || prompt.contains("مخطط") || prompt.contains("شكل") || prompt.contains("chart") || prompt.contains("ratio") || prompt.contains("score") || prompt.contains("compare")) {
                        """{"metrics": [{"label": "Gemini", "value": 92}, {"label": "GPT-4o", "value": 94}, {"label": "DeepSeek", "value": 89}, {"label": "Llama 3", "value": 85}, {"label": "Claude", "value": 91}, {"label": "Perplexity", "value": 93}, {"label": "Banana", "value": 60}]}"""
                    } else {
                        // Regular metric representation
                        """{"metrics": [{"label": "RAG Accuracy", "value": 95}, {"label": "Speed", "value": 85}, {"label": "Resonance", "value": 78}]}"""
                    }

                    val aiMsg = CosmoMessage(
                        sender = model,
                        messageText = aiResponse,
                        responseMode = _uiState.value.activePresentationMode,
                        chartDataJson = chartJson
                    )
                    
                    repository.insertMessage(aiMsg)

                    // Speak response if current presentation mode is voice
                    if (_uiState.value.activePresentationMode == "Voice" && model == models.first()) {
                        speakText(aiResponse)
                    }
                }
            }

            _uiState.update { it.copy(isRecordingOrProcessing = false) }
        }
    }

    /**
     * TextToSpeech Synthesis
     */
    fun speakText(text: String) {
        if (!isTtsInitialized) return
        
        // Strip out some markdown formatting for cleaner speech synthesis
        val cleanText = text.replace(Regex("[*#_`~|\\\\]"), "")
            .take(300) // Keep standard speech preview succinct
            
        _uiState.update { it.copy(isSpeaking = true, currentSpokenText = cleanText) }
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "CosmoSpeech")
    }

    fun stopSpeaking() {
        tts?.stop()
        _uiState.update { it.copy(isSpeaking = false, currentSpokenText = "") }
    }

    /**
     * Exports the latest answers compiled with "IDLEB X" signatures as a beautiful PDF document!
     */
    fun exportToPdf(context: Context, messages: List<CosmoMessage>) {
        if (messages.isEmpty()) {
            Toast.makeText(context, "No chat messages to export!", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val pdfDocument = PdfDocument()
            val paint = Paint()
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 14f
                isAntiAlias = true
            }
            val titlePaint = Paint().apply {
                color = Color.rgb(15, 0, 40)
                textSize = 20f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val signaturePaint = Paint().apply {
                color = Color.rgb(255, 0, 127)
                textSize = 12f
                isFakeBoldText = true
                isAntiAlias = true
            }

            var pageNumber = 1
            var yPosition = 50f
            
            // Map dimensions for standard A4
            var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas: Canvas = page.canvas

            // Header banner
            canvas.drawRect(0f, 0f, 595f, 40f, Paint().apply { color = Color.rgb(10, 5, 25) })
            canvas.drawText("IDLEB X – AI COSMOS JOURNAL", 20f, 26f, Paint().apply {
                color = Color.WHITE
                textSize = 14f
                isFakeBoldText = true
            })

            yPosition = 80f
            canvas.drawText("AI Cosmos Analytical Log Report", 20f, yPosition, titlePaint)
            yPosition += 20f
            canvas.drawText("Compiled on behalf of: MOOHAMED - IDLEB X", 20f, yPosition, Paint().apply {
                color = Color.GRAY
                textSize = 12f
            })
            yPosition += 30f

            messages.forEach { msg ->
                if (yPosition > 750f) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = 50f
                }

                val prefix = if (msg.sender == "User") "👤 MOOHAMED (User):" else "🪐 ${msg.sender} (AI Model):"
                canvas.drawText(prefix, 20f, yPosition, Paint().apply {
                    color = if (msg.sender == "User") Color.rgb(0, 100, 200) else Color.rgb(100, 0, 200)
                    textSize = 12f
                    isFakeBoldText = true
                })
                yPosition += 18f

                // Word wrap text logically
                val words = msg.messageText.split(" ")
                var lineBuilder = StringBuilder()
                for (word in words) {
                    val tempLine = lineBuilder.toString() + word + " "
                    val textWidth = textPaint.measureText(tempLine)
                    if (textWidth > 550f) {
                        canvas.drawText(lineBuilder.toString(), 30f, yPosition, textPaint)
                        yPosition += 16f
                        lineBuilder = StringBuilder(word).append(" ")
                        if (yPosition > 780f) {
                            pdfDocument.finishPage(page)
                            pageNumber++
                            pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                            page = pdfDocument.startPage(pageInfo)
                            canvas = page.canvas
                            yPosition = 50f
                        }
                    } else {
                        lineBuilder.append(word).append(" ")
                    }
                }
                if (lineBuilder.isNotEmpty()) {
                    canvas.drawText(lineBuilder.toString(), 30f, yPosition, textPaint)
                    yPosition += 25f
                }
            }

            // Signature at back
            if (yPosition > 720f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 50f
            }
            yPosition += 20f
            canvas.drawLine(20f, yPosition, 575f, yPosition, Paint().apply { color = Color.LTGRAY })
            yPosition += 25f
            canvas.drawText("SYSTEM SECURE SIGNATURE DECRYPTED:", 20f, yPosition, Paint().apply {
                color = Color.GRAY
                textSize = 11f
            })
            yPosition += 18f
            canvas.drawText("⚡ MOOHAMED - IDLEB X ⚡", 20f, yPosition, signaturePaint)
            yPosition += 15f
            canvas.drawText("Secure Multiversal AI Interface Core v2026. All Rights Merged.", 20f, yPosition, Paint().apply {
                color = Color.GRAY
                textSize = 10f
            })

            pdfDocument.finishPage(page)

            // Save PDF
            val outDir = File(context.cacheDir, "documents")
            if (!outDir.exists()) outDir.mkdirs()
            val file = File(outDir, "IDLEB_X_AI_Cosmos_Report.pdf")
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            // Share PDF
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share AI Cosmos Report via IDLEB X"))
            _uiState.update { it.copy(systemMessage = "Report exported as signature PDF Document!") }
        } catch (e: Exception) {
            Log.e("CosmoViewModel", "PDF Export error", e)
            Toast.makeText(context, "PDF Failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Generates a stunning graphics PNG infocard from latest reply and shares it!
     */
    fun shareAsImage(context: Context, replySender: String, textReply: String) {
        try {
            val width = 600
            val height = 400
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Paint elegant galactic background gradient
            val bgPaint = Paint().apply {
                shader = android.graphics.LinearGradient(
                    0f, 0f, 0f, 400f,
                    Color.rgb(3, 3, 16),
                    Color.rgb(11, 0, 31),
                    android.graphics.Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, 600f, 400f, bgPaint)

            // Dynamic grid star dots
            val starPaint = Paint().apply { color = Color.WHITE; alpha = 100 }
            for (i in 0..12) {
                canvas.drawCircle((20..580).random().toFloat(), (20..380).random().toFloat(), (1..3).random().toFloat(), starPaint)
            }

            // Thin glowing border
            val borderPaint = Paint().apply {
                color = Color.rgb(0, 242, 254) // Cyan glow
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }
            canvas.drawRect(6f, 6f, 594f, 394f, borderPaint)

            // Title
            canvas.drawText("🪐 IDLEB X – COSMIC INTELLEGENT INFOCARD", 25f, 45f, Paint().apply {
                color = Color.WHITE
                textSize = 16f
                isFakeBoldText = true
                isAntiAlias = true
            })

            canvas.drawText("GENERATED CORE RESPONSE BY $replySender", 25f, 75f, Paint().apply {
                color = Color.rgb(157, 78, 221) // electric purple
                textSize = 12f
                isFakeBoldText = true
                isAntiAlias = true
            })

            // Content wrap
            val contentPaint = Paint().apply {
                color = Color.rgb(220, 220, 250)
                textSize = 13f
                isAntiAlias = true
            }
            val quote = textReply.replace(Regex("[*#_`~|\\\\]"), "")
            val words = quote.split(" ")
            var lineBuilder = StringBuilder()
            var yPosition = 120f
            for (word in words) {
                val tempLine = lineBuilder.toString() + word + " "
                val tw = contentPaint.measureText(tempLine)
                if (tw > 540f) {
                    canvas.drawText(lineBuilder.toString(), 25f, yPosition, contentPaint)
                    yPosition += 18f
                    lineBuilder = StringBuilder(word).append(" ")
                    if (yPosition > 330f) {
                        canvas.drawText("...", 25f, yPosition, contentPaint)
                        break
                    }
                } else {
                    lineBuilder.append(word).append(" ")
                }
            }
            if (lineBuilder.isNotEmpty() && yPosition <= 330f) {
                canvas.drawText(lineBuilder.toString(), 25f, yPosition, contentPaint)
            }

            // Stamp Signature
            val footerPaint = Paint().apply {
                color = Color.rgb(255, 0, 127) // Magenta Neon
                textSize = 12f
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawText("DEVELOPED BY: MOOHAMED (IDLEB X)", 25f, 370f, footerPaint)

            // Save Bitmap
            val cacheDir = File(context.cacheDir, "images")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val file = File(cacheDir, "IDLEB_X_Cosmos_Infocard.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Cosmic Infocard via IDLEB X"))
            _uiState.update { it.copy(systemMessage = "Infocard exported as high-fidelity PNG image!") }
        } catch (e: Exception) {
            Log.e("CosmoViewModel", "Image share error", e)
            Toast.makeText(context, "Image share failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.shutdown()
    }
}
