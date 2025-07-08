package dev.eliaschen.skillsstudyflowapi.service

import dev.eliaschen.skillsstudyflowapi.entity.RecordEntity
import dev.eliaschen.skillsstudyflowapi.entity.NoteEntity
import dev.eliaschen.skillsstudyflowapi.entity.TagEntity
import dev.eliaschen.skillsstudyflowapi.repository.RecordRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

// DTOs for API - matching your specified schema
data class RecordDataNoteSchema(
    val time: Int,
    val data: String
)

data class RecordDataSchema(
    val id: String,
    val name: String,
    val file: String,
    val note: List<RecordDataNoteSchema>,
    val tags: List<String>
)

// Legacy alias for backward compatibility
typealias Note = RecordDataNoteSchema
typealias Record = RecordDataSchema

@Service
@Transactional
class RecordService(private val recordRepository: RecordRepository) {

    fun getAllRecords(): List<Record> {
        val recordsWithNotes = recordRepository.findAllWithNotes()
        if (recordsWithNotes.isEmpty()) return emptyList()
        
        val recordIds = recordsWithNotes.map { it.id }
        val recordsWithTags = recordRepository.findTagsByRecordIds(recordIds)
        val tagsMap = recordsWithTags.associate { it.id to it.tags }
        
        return recordsWithNotes.map { record ->
            val tags = tagsMap[record.id] ?: emptyList()
            // Add tags to the record temporarily for DTO conversion
            record.tags.clear()
            record.tags.addAll(tags)
            record.toDto()
        }
    }

    fun getRecordById(id: String): Record? {
        val recordWithNotes = recordRepository.findByIdWithNotes(id) ?: return null
        val recordsWithTags = recordRepository.findTagsByRecordIds(listOf(id))
        val tags = recordsWithTags.firstOrNull()?.tags ?: emptyList()
        
        // Add tags to the record temporarily for DTO conversion
        recordWithNotes.tags.clear()
        recordWithNotes.tags.addAll(tags)
        return recordWithNotes.toDto()
    }

    fun createRecord(record: Record): Record {
        val entity = RecordEntity(
            id = record.id,
            name = record.name,
            file = record.file,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        // Add notes
        record.note.forEach { note ->
            val noteEntity = NoteEntity(
                time = note.time,
                data = note.data,
                record = entity
            )
            entity.notes.add(noteEntity)
        }
        
        // Add tags
        record.tags.forEach { tag ->
            val tagEntity = TagEntity(
                tag = tag,
                record = entity
            )
            entity.tags.add(tagEntity)
        }
        
        val savedEntity = recordRepository.save(entity)
        return savedEntity.toDto()
    }

    fun updateRecord(id: String, updatedRecord: Record): Record? {
        val existingEntity = recordRepository.findByIdWithNotes(id) ?: return null
        
        val updatedEntity = RecordEntity(
            id = existingEntity.id,
            name = updatedRecord.name,
            file = updatedRecord.file,
            createdAt = existingEntity.createdAt,
            updatedAt = LocalDateTime.now()
        )
        
        // Clear existing notes and add new ones
        updatedEntity.notes.clear()
        updatedRecord.note.forEach { note ->
            val noteEntity = NoteEntity(
                time = note.time,
                data = note.data,
                record = updatedEntity
            )
            updatedEntity.notes.add(noteEntity)
        }
        
        // Clear existing tags and add new ones
        updatedEntity.tags.clear()
        updatedRecord.tags.forEach { tag ->
            val tagEntity = TagEntity(
                tag = tag,
                record = updatedEntity
            )
            updatedEntity.tags.add(tagEntity)
        }
        
        val savedEntity = recordRepository.save(updatedEntity)
        return savedEntity.toDto()
    }

    fun deleteRecord(id: String): Boolean {
        return if (recordRepository.existsById(id)) {
            recordRepository.deleteById(id)
            true
        } else {
            false
        }
    }

    fun recordExists(id: String): Boolean {
        return recordRepository.existsById(id)
    }

    fun fileExists(filename: String): Boolean {
        return recordRepository.existsByFile(filename)
    }

    // Extension function to convert entity to DTO
    private fun RecordEntity.toDto(): Record {
        return Record(
            id = this.id,
            name = this.name,
            file = this.file,
            note = this.notes.map { RecordDataNoteSchema(it.time, it.data) },
            tags = this.tags.map { it.tag }
        )
    }
}
