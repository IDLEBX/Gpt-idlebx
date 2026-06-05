package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cosmo_files")
data class CosmoFile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val content: String,
    val fileType: String, // "PDF", "DOCX", "TXT", "Markdown", "Image"
    val timestamp: Long = System.currentTimeMillis(),
    val sizeLabel: String = "12 KB",
    val orbitRadius: Float,
    val colorHex: String,
    val typeSymbol: String
)

@Entity(tableName = "cosmo_messages")
data class CosmoMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "User", "Gemini", "GPT-4o", "Nano Banana", "Llama 3", "DeepSeek"
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val responseMode: String = "Text", // "Text", "Chart", "Voice"
    val chartDataJson: String? = null // simple structure to render mock custom bar/radar charts
)

@Dao
interface CosmoDao {
    @Query("SELECT * FROM cosmo_files ORDER BY timestamp DESC")
    fun getAllFiles(): Flow<List<CosmoFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: CosmoFile)

    @Query("DELETE FROM cosmo_files WHERE id = :id")
    suspend fun deleteFileById(id: Long)

    @Query("DELETE FROM cosmo_files")
    suspend fun clearAllFiles()

    @Query("SELECT * FROM cosmo_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<CosmoMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: CosmoMessage)

    @Query("DELETE FROM cosmo_messages")
    suspend fun clearChatHistory()
}

@Database(entities = [CosmoFile::class, CosmoMessage::class], version = 1, exportSchema = false)
abstract class CosmoDatabase : RoomDatabase() {
    abstract val dao: CosmoDao
}
