package dev.eliaschen.skillsstudyflowapi.repository

import dev.eliaschen.skillsstudyflowapi.service.RecordDataSchema
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class InMemoryRecordRepository {
    
    private val records = ConcurrentHashMap<String, RecordDataSchema>()
    
    fun save(record: RecordDataSchema): RecordDataSchema {
        records[record.id] = record
        return record
    }
    
    fun findById(id: String): RecordDataSchema? {
        return records[id]
    }
    
    fun findByIdAndUserId(id: String, userId: String): RecordDataSchema? {
        return records[id]?.takeIf { it.userId == userId }
    }
    
    fun findAll(): List<RecordDataSchema> {
        return records.values.toList()
    }
    
    fun findAllByUserId(userId: String): List<RecordDataSchema> {
        return records.values.filter { it.userId == userId }
    }
    
    fun existsById(id: String): Boolean {
        return records.containsKey(id)
    }
    
    fun existsByIdAndUserId(id: String, userId: String): Boolean {
        return records[id]?.userId == userId
    }
    
    fun deleteById(id: String): Boolean {
        return records.remove(id) != null
    }
    
    fun deleteByIdAndUserId(id: String, userId: String): Boolean {
        val record = records[id]
        return if (record?.userId == userId) {
            records.remove(id) != null
        } else {
            false
        }
    }
    
    fun existsByFile(filename: String): Boolean {
        return records.values.any { it.file == filename }
    }
    
    fun existsByFileAndUserId(filename: String, userId: String): Boolean {
        return records.values.any { it.file == filename && it.userId == userId }
    }
    
    fun clear() {
        records.clear()
    }
    
    fun count(): Int {
        return records.size
    }
}
