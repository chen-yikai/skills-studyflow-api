package dev.eliaschen.skillsstudyflowapi.repository

import dev.eliaschen.skillsstudyflowapi.entity.RecordEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface RecordRepository : JpaRepository<RecordEntity, String> {
    
    @Query("SELECT DISTINCT r FROM RecordEntity r LEFT JOIN FETCH r.notes")
    fun findAllWithNotes(): List<RecordEntity>
    
    @Query("SELECT DISTINCT r FROM RecordEntity r LEFT JOIN FETCH r.notes WHERE r.id = :id")
    fun findByIdWithNotes(id: String): RecordEntity?
    
    @Query("SELECT DISTINCT r FROM RecordEntity r LEFT JOIN FETCH r.tags WHERE r.id IN :ids")
    fun findTagsByRecordIds(ids: List<String>): List<RecordEntity>
    
    fun findByName(name: String): RecordEntity?
    
    fun existsByFile(file: String): Boolean
}
