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
    
    fun findAll(): List<RecordDataSchema> {
        return records.values.toList()
    }
    
    fun existsById(id: String): Boolean {
        return records.containsKey(id)
    }
    
    fun deleteById(id: String): Boolean {
        return records.remove(id) != null
    }
    
    fun existsByFile(filename: String): Boolean {
        return records.values.any { it.file == filename }
    }
    
    fun clear() {
        records.clear()
    }
    
    fun count(): Int {
        return records.size
    }
}
