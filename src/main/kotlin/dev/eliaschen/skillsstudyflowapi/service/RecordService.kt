package dev.eliaschen.skillsstudyflowapi.service

import dev.eliaschen.skillsstudyflowapi.repository.InMemoryRecordRepository
import dev.eliaschen.skillsstudyflowapi.util.SecurityUtils
import org.springframework.stereotype.Service

// DTOs for API - matching your specified schema
data class RecordDataNoteSchema(
    val time: String,
    val data: String
)

data class RecordDataSchema(
    val id: String,
    val name: String,
    val file: String,
    val date: String,
    val note: List<RecordDataNoteSchema>,
    val tags: List<String>,
    val screenshots: List<String>,
    val userId: String
)

// DTO for API requests without userId (userId is extracted from token)
data class RecordCreateRequest(
    val id: String,
    val name: String,
    val file: String,
    val date: String,
    val note: List<RecordDataNoteSchema>,
    val tags: List<String>,
    val screenshots: List<String>
)

// DTO for update requests without id requirement
data class RecordUpdateSchema(
    val name: String,
    val file: String,
    val date: String,
    val note: List<RecordDataNoteSchema>,
    val tags: List<String>,
    val screenshots: List<String>
)

// Legacy alias for backward compatibility
typealias Note = RecordDataNoteSchema
typealias Record = RecordDataSchema

@Service
class RecordService(private val recordRepository: InMemoryRecordRepository) {

    fun getAllRecords(): List<Record> {
        val userId = SecurityUtils.getCurrentUsername()
        return recordRepository.findAllByUserId(userId)
    }

    fun getRecordById(id: String): Record? {
        val userId = SecurityUtils.getCurrentUsername()
        return recordRepository.findByIdAndUserId(id, userId)
    }

    fun createRecord(record: Record): Record {
        val userId = SecurityUtils.getCurrentUsername()
        val recordWithUser = record.copy(userId = userId)
        recordRepository.save(recordWithUser)
        return recordWithUser
    }

    fun createRecordFromRequest(request: RecordCreateRequest): Record {
        val userId = SecurityUtils.getCurrentUsername()
        val record = RecordDataSchema(
            id = request.id,
            name = request.name,
            file = request.file,
            date = request.date,
            note = request.note,
            tags = request.tags,
            screenshots = request.screenshots,
            userId = userId
        )
        recordRepository.save(record)
        return record
    }

    fun updateRecord(id: String, updatedRecord: Record): Record? {
        val userId = SecurityUtils.getCurrentUsername()
        if (!recordRepository.existsByIdAndUserId(id, userId)) {
            return null
        }
        
        val updated = updatedRecord.copy(id = id, userId = userId)
        recordRepository.save(updated)
        return updated
    }

    fun updateRecordFromRequest(id: String, request: RecordUpdateSchema): Record? {
        val userId = SecurityUtils.getCurrentUsername()
        if (!recordRepository.existsByIdAndUserId(id, userId)) {
            return null
        }
        
        val updated = RecordDataSchema(
            id = id,
            name = request.name,
            file = request.file,
            date = request.date,
            note = request.note,
            tags = request.tags,
            screenshots = request.screenshots,
            userId = userId
        )
        recordRepository.save(updated)
        return updated
    }

    fun deleteRecord(id: String): Boolean {
        val userId = SecurityUtils.getCurrentUsername()
        return recordRepository.deleteByIdAndUserId(id, userId)
    }

    fun recordExists(id: String): Boolean {
        val userId = SecurityUtils.getCurrentUsername()
        return recordRepository.existsByIdAndUserId(id, userId)
    }

    fun fileExists(filename: String): Boolean {
        val userId = SecurityUtils.getCurrentUsername()
        return recordRepository.existsByFileAndUserId(filename, userId)
    }

    fun searchRecords(query: String): List<Record> {
        val userId = SecurityUtils.getCurrentUsername()
        if (query.isBlank()) {
            return emptyList()
        }
        
        val queryLower = query.lowercase().trim()
        
        return recordRepository.findAllByUserId(userId).filter { record ->
            // Search in name
            record.name.lowercase().contains(queryLower) ||
            // Search in note data
            record.note.any { note -> note.data.lowercase().contains(queryLower) }
        }
    }
}
