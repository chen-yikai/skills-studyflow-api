package dev.eliaschen.skillsstudyflowapi.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "records")
data class RecordEntity(
    @Id
    val id: String,
    
    @Column(nullable = false)
    val name: String,
    
    @Column(nullable = false)
    val file: String,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @OneToMany(mappedBy = "record", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    val notes: MutableList<NoteEntity> = mutableListOf(),
    
    @OneToMany(mappedBy = "record", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    val tags: MutableList<TagEntity> = mutableListOf()
) {
    // No-arg constructor for JPA
    constructor() : this("", "", "", LocalDateTime.now(), LocalDateTime.now(), mutableListOf(), mutableListOf())
}

@Entity
@Table(name = "notes")
data class NoteEntity(
    @Id
    @GeneratedValue
    val id: Long? = null,
    
    @Column(nullable = false)
    val time: Int,
    
    @Column(nullable = false, columnDefinition = "TEXT")
    val data: String,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    val record: RecordEntity
) {
    // No-arg constructor for JPA
    constructor() : this(null, 0, "", RecordEntity())
}

@Entity
@Table(name = "tags")
data class TagEntity(
    @Id
    @GeneratedValue
    val id: Long? = null,
    
    @Column(nullable = false)
    val tag: String,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    val record: RecordEntity
) {
    // No-arg constructor for JPA
    constructor() : this(null, "", RecordEntity())
}
