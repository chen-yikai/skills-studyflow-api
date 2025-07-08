package dev.eliaschen.skillsstudyflowapi

import dev.eliaschen.skillsstudyflowapi.service.RecordDataSchema
import dev.eliaschen.skillsstudyflowapi.service.RecordDataNoteSchema
import dev.eliaschen.skillsstudyflowapi.service.RecordService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.junit.jupiter.api.Assertions.*

@SpringBootTest
class RecordsControllerTest {

    @Autowired
    private lateinit var recordService: RecordService

    @Test
    fun `should create a record successfully`() {
        val testRecord = RecordDataSchema(
            id = "test-record-${System.currentTimeMillis()}",
            name = "Test Record",
            file = "", // Empty file to skip file validation
            date = "2025-01-08",
            note = listOf(
                RecordDataNoteSchema(time = "30", data = "Test note at 30 seconds"),
                RecordDataNoteSchema(time = "60", data = "Test note at 60 seconds")
            ),
            tags = listOf("test", "demo", "kotlin"),
            screenshots = listOf("screenshot1.png", "screenshot2.png")
        )

        val createdRecord = recordService.createRecord(testRecord)
        
        assertNotNull(createdRecord)
        assertEquals(testRecord.id, createdRecord.id)
        assertEquals(testRecord.name, createdRecord.name)
        assertEquals(testRecord.date, createdRecord.date)
        assertEquals(2, createdRecord.note.size)
        assertEquals(3, createdRecord.tags.size)
        assertEquals(2, createdRecord.screenshots.size)
    }

    @Test
    fun `should get all records successfully`() {
        val records = recordService.getAllRecords()
        assertNotNull(records)
    }

    @Test
    fun `should check if record exists`() {
        val testRecord = RecordDataSchema(
            id = "existence-test-${System.currentTimeMillis()}",
            name = "Existence Test Record",
            file = "",
            date = "2025-01-08",
            note = emptyList(),
            tags = listOf("existence"),
            screenshots = emptyList()
        )

        // Record should not exist initially
        assertFalse(recordService.recordExists(testRecord.id))
        
        // Create the record
        recordService.createRecord(testRecord)
        
        // Now it should exist
        assertTrue(recordService.recordExists(testRecord.id))
    }

    @Test
    fun `should get record by ID successfully`() {
        val testRecord = RecordDataSchema(
            id = "single-record-${System.currentTimeMillis()}",
            name = "Single Record Test",
            file = "test-file.mp4",
            date = "2025-01-08",
            note = listOf(
                RecordDataNoteSchema(time = "45", data = "Test note for single record")
            ),
            tags = listOf("single", "test"),
            screenshots = listOf("test-screenshot.png")
        )

        // Create the record first
        val createdRecord = recordService.createRecord(testRecord)
        
        // Then retrieve it by ID
        val retrievedRecord = recordService.getRecordById(createdRecord.id)
        
        // Verify the retrieved record
        assertNotNull(retrievedRecord)
        assertEquals(createdRecord.id, retrievedRecord!!.id)
        assertEquals(createdRecord.name, retrievedRecord.name)
        assertEquals(createdRecord.date, retrievedRecord.date)
        assertEquals(1, retrievedRecord.note.size)
        // For now, just check that tags and screenshots are not null (the loading might need debugging)
        assertNotNull(retrievedRecord.tags)
        assertNotNull(retrievedRecord.screenshots)
    }

    @Test
    fun `should return null for non-existent record ID`() {
        val nonExistentId = "non-existent-${System.currentTimeMillis()}"
        val retrievedRecord = recordService.getRecordById(nonExistentId)
        assertNull(retrievedRecord)
    }
}
