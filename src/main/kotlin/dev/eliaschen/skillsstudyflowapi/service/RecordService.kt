package dev.eliaschen.skillsstudyflowapi.service

import dev.eliaschen.skillsstudyflowapi.repository.InMemoryRecordRepository
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
        return recordRepository.findAll()
    }

    fun getRecordById(id: String): Record? {
        return recordRepository.findById(id)
    }

    fun createRecord(record: Record): Record {
        recordRepository.save(record)
        return record
    }

    fun updateRecord(id: String, updatedRecord: Record): Record? {
        if (!recordRepository.existsById(id)) {
            return null
        }
        
        val updated = updatedRecord.copy(id = id)
        recordRepository.save(updated)
        return updated
    }

    fun deleteRecord(id: String): Boolean {
        return recordRepository.deleteById(id)
    }

    fun recordExists(id: String): Boolean {
        return recordRepository.existsById(id)
    }

    fun fileExists(filename: String): Boolean {
        return recordRepository.existsByFile(filename)
    }
}
