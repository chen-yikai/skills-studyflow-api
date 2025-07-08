package dev.eliaschen.skillsstudyflowapi

import dev.eliaschen.skillsstudyflowapi.service.RecordDataSchema
import dev.eliaschen.skillsstudyflowapi.service.RecordDataNoteSchema
import dev.eliaschen.skillsstudyflowapi.service.RecordService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.junit.jupiter.api.Assertions.*

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
class RecordsControllerTest {

    @Autowired
    private lateinit var recordService: RecordService

    @Test
    fun `should create a record successfully`() {
        val testRecord = RecordDataSchema(
            id = "test-record-${System.currentTimeMillis()}",
            name = "Test Record",
            file = "", // Empty file to skip file validation
            note = listOf(
                RecordDataNoteSchema(time = 30, data = "Test note at 30 seconds"),
                RecordDataNoteSchema(time = 60, data = "Test note at 60 seconds")
            ),
            tags = listOf("test", "demo", "kotlin")
        )

        val createdRecord = recordService.createRecord(testRecord)
        
        assertNotNull(createdRecord)
        assertEquals(testRecord.id, createdRecord.id)
        assertEquals(testRecord.name, createdRecord.name)
        assertEquals(2, createdRecord.note.size)
        assertEquals(3, createdRecord.tags.size)
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
            note = emptyList(),
            tags = listOf("existence")
        )

        // Record should not exist initially
        assertFalse(recordService.recordExists(testRecord.id))
        
        // Create the record
        recordService.createRecord(testRecord)
        
        // Now it should exist
        assertTrue(recordService.recordExists(testRecord.id))
    }
}
