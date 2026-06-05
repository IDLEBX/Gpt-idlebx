package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Locale

data class RAGResult(
    val snippet: String,
    val matchedFileIds: List<Long>,
    val matchedFileNames: List<String>
)

class CosmoRepository(context: Context) {
    private val database: CosmoDatabase = Room.databaseBuilder(
        context.applicationContext,
        CosmoDatabase::class.java,
        "cosmo_database"
    ).build()

    private val dao = database.dao

    val allFiles: Flow<List<CosmoFile>> = dao.getAllFiles()
    val allMessages: Flow<List<CosmoMessage>> = dao.getAllMessages()

    suspend fun insertFile(file: CosmoFile) {
        dao.insertFile(file)
    }

    suspend fun deleteFile(id: Long) {
        dao.deleteFileById(id)
    }

    suspend fun clearFiles() {
        dao.clearAllFiles()
    }

    suspend fun insertMessage(message: CosmoMessage) {
        dao.insertMessage(message)
    }

    suspend fun clearChatHistory() {
        dao.clearChatHistory()
    }

    /**
     * Highly responsive Local semantic RAG engine
     * Simulates vector-style retrieval inside local SQLite database using TF-IDF matching.
     */
    suspend fun retrieveRelevantContext(query: String): RAGResult {
        val filesList = allFiles.firstOrNull() ?: emptyList()
        if (filesList.isEmpty()) {
            return RAGResult("", emptyList(), emptyList())
        }

        val stopWords = setOf("the", "and", "a", "of", "to", "in", "is", "that", "it", "on", "for", "as", "with", "من", "في", "على", "إلى", "عن", "مع", "هذا", "التي", "الذي", "أن", "أو")
        val queryTerms = query.lowercase(Locale.ROOT)
            .split(Regex("[^\\w\\p{L}]+"))
            .filter { it.isNotBlank() && it !in stopWords }

        if (queryTerms.isEmpty()) {
            // Fallback to most recent file if no specific terms match
            val topFile = filesList.firstOrNull()
            return if (topFile != null) {
                RAGResult(
                    snippet = "Source file: ${topFile.name}\n${topFile.content.take(400)}...",
                    matchedFileIds = listOf(topFile.id),
                    matchedFileNames = listOf(topFile.name)
                )
            } else {
                RAGResult("", emptyList(), emptyList())
            }
        }

        // Score files
        val matches = filesList.map { file ->
            val text = (file.name + " " + file.content).lowercase(Locale.ROOT)
            var score = 0
            queryTerms.forEach { term ->
                val occ = text.windowed(term.length).count { it == term }
                score += occ
            }
            Pair(file, score)
        }.filter { it.second > 0 }
         .sortedByDescending { it.second }

        if (matches.isEmpty()) {
            // Default reference
            return RAGResult("", emptyList(), emptyList())
        }

        val bestMatches = matches.take(2)
        val snippetBuilder = StringBuilder()
        val matchIds = mutableListOf<Long>()
        val matchNames = mutableListOf<String>()

        bestMatches.forEach { (file, count) ->
            matchIds.add(file.id)
            matchNames.add(file.name)
            
            // Extract a relevant window around the matched terms if possible
            val contentLower = file.content.lowercase(Locale.ROOT)
            var bestIdx = 0
            var highestLocalScore = 0
            
            // Look for sentences containing the query keywords
            val sentences = file.content.split(Regex("[.!?،\\n]+"))
            val matchingSentences = sentences.filter { sentence ->
                queryTerms.any { term -> sentence.lowercase(Locale.ROOT).contains(term) }
            }.take(3)

            val snippetText = if (matchingSentences.isNotEmpty()) {
                matchingSentences.joinToString(". ").trim()
            } else {
                file.content.take(300).trim()
            }

            snippetBuilder.append("=== [Context: Resource '${file.name}' (${file.fileType})] ===\n")
            snippetBuilder.append(snippetText)
            snippetBuilder.append("\n\n")
        }

        return RAGResult(
            snippet = snippetBuilder.toString().trim(),
            matchedFileIds = matchIds,
            matchedFileNames = matchNames
        )
    }

    /**
     * Seed initial files for demonstration purposes
     */
    suspend fun seedInitialFilesIfEmpty() {
        val currentList = allFiles.firstOrNull() ?: emptyList()
        if (currentList.isEmpty()) {
            // Seed IDLEB X Guidelines
            insertFile(
                CosmoFile(
                    name = "IDLEB_X_Cosmos_Guidelines.md",
                    content = """# IDLEB X – AI Cosmos System Framework
Created by elite developer: MOOHAMED (IDLEB_X).
Key Features:
- Immersive galaxy layout with glowing planetary energy nodes.
- High-performance neural RAG pipeline pulling content locally and querying models.
- Multidimensional AI systems: Gemini, GPT-4o, Llama 3, DeepSeek, and ultra-lightweight Local Nano Banana.
- Multi-presentation layer: Rich Markdown typing, 3D Canvas visualizer, and pulsating TTS sound matrix.
System is tuned to optimal indigo & purple neon wavelengths. Absolute developer signature: MOOHAMED - IDLEB X.""",
                    fileType = "Markdown",
                    sizeLabel = "1.8 KB",
                    orbitRadius = 130f,
                    colorHex = "#FF007F", // Neon Magenta Pink
                    typeSymbol = "MD"
                )
            )

            // Seed Quantum Matrix
            insertFile(
                CosmoFile(
                    name = "Quantum_AI_Matrix.txt",
                    content = """Universe Neural Vectors:
Each file in the AI Cosmos orbits the Core as an active energy body.
Retrieval-Augmented Generation (RAG) computes similarities over orbiting file data.
When active, light pulses target specific planetary file nodes.
Models available in this dimension:
1. Gemini - Infinite reasoning light.
2. GPT-4o - Universal balance logic.
3. Nano Banana - Ultra-fast yellow energetic spark, highly playful and comical.
4. Llama 3 - Deep structural memory.
5. DeepSeek - Abstract reasoning and system operations.
Encryption Key: IDLEB-X-MOOHAMED-V2026.""",
                    fileType = "TXT",
                    sizeLabel = "2.4 KB",
                    orbitRadius = 180f,
                    colorHex = "#00F2FE", // Cyan neon
                    typeSymbol = "TXT"
                )
            )

            // Seed Cosmo Space Map
            insertFile(
                CosmoFile(
                    name = "Core_Coordinates_Specs.pdf",
                    content = """[AI COSMOS ARCHITECTURE - CONFIDENTIAL]
Central Engine Core coordinates: Vector3(0.0, 0.0, 0.0).
Orbit rings calculated at R1=130dp, R2=180dp, R3=235dp.
Glassmorphic overlay transparency: Alpha = 0.15 with high-contrast glowing neon borders.
Bicultural translation strings: Auto-switched between English (EN) and Arabic (AR).
Global Audio Resonance: 440Hz sinus pulse generated on click events to simulate interactive cosmic telemetry.
Signed, MOOHAMED - IDLEB X, 2026.""",
                    fileType = "PDF",
                    sizeLabel = "4.2 KB",
                    orbitRadius = 235f,
                    colorHex = "#9D4EDD", // Electric Purple
                    typeSymbol = "PDF"
                )
            )
        }
    }
}
