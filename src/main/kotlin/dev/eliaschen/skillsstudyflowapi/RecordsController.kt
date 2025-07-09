package dev.eliaschen.skillsstudyflowapi

import dev.eliaschen.skillsstudyflowapi.service.RecordService
import dev.eliaschen.skillsstudyflowapi.service.Record
import dev.eliaschen.skillsstudyflowapi.service.RecordCreateRequest
import dev.eliaschen.skillsstudyflowapi.service.RecordUpdateSchema
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.security.core.context.SecurityContextHolder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.slf4j.LoggerFactory

data class FileInfo(
    val filename: String,
    val uploadDate: String,
    val size: Long
)

data class RecordResponse(
    val id: String,
    val name: String,
    val file: String,
    val date: String,
    val note: List<dev.eliaschen.skillsstudyflowapi.service.RecordDataNoteSchema>,
    val tags: List<String>,
    val screenshots: List<String>
)

fun Record.toResponse(): RecordResponse {
    return RecordResponse(
        id = this.id,
        name = this.name,
        file = this.file,
        date = this.date,
        note = this.note,
        tags = this.tags,
        screenshots = this.screenshots
    )
}

@RestController
@RequestMapping("/records")
@Tag(name = "Records", description = "File upload and download API")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "apiKey")
class RecordsController(private val recordService: RecordService) {
    private val logger = LoggerFactory.getLogger(RecordsController::class.java)
    private val uploadDir: Path = Paths.get("records").toAbsolutePath()
    private val screenshotDir: Path = Paths.get("screenshots").toAbsolutePath()

    init {
        Files.createDirectories(uploadDir)
        Files.createDirectories(screenshotDir)
    }

    @GetMapping
    @Operation(summary = "Get all records", description = "Returns a list of all records")
    @ApiResponse(responseCode = "200", description = "Records retrieved successfully")
    fun getRecords(): ResponseEntity<List<RecordResponse>> {
        val records = recordService.getAllRecords()
        logger.info("Getting all records. Count: ${records.size}")
        return ResponseEntity.ok(records.map { it.toResponse() })
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search records", 
        description = "Search records by name or note data content. Returns records that match the query in either name or note data."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Search completed successfully"),
            ApiResponse(responseCode = "400", description = "Invalid query parameter")
        ]
    )
    fun searchRecords(
        @Parameter(description = "Search query to match against record name or note data")
        @RequestParam("q", required = true) query: String
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Searching records with query: '$query'")
        
        if (query.isBlank()) {
            logger.warn("Search failed: Query parameter is blank")
            return ResponseEntity.badRequest().body(
                mapOf("message" to "Query parameter cannot be blank")
            )
        }
        
        val results = recordService.searchRecords(query)
        logger.info("Search completed. Found ${results.size} records matching '$query'")
        
        return ResponseEntity.ok(
            mapOf(
                "query" to query,
                "count" to results.size,
                "results" to results.map { it.toResponse() }
            )
        )
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single record by ID", description = "Returns a specific record by its ID")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Record retrieved successfully"),
            ApiResponse(responseCode = "404", description = "Record not found")
        ]
    )
    fun getRecordById(@Parameter(description = "ID of the record to retrieve") @PathVariable id: String): ResponseEntity<Any> {
        logger.info("Getting record with ID: $id")
        
        val record = recordService.getRecordById(id)
        return if (record != null) {
            logger.info("Record found: ${record.name}")
            ResponseEntity.ok(record.toResponse())
        } else {
            logger.warn("Record with ID '$id' not found")
            ResponseEntity.status(404).body(
                mapOf("message" to "Record with ID '$id' not found")
            )
        }
    }

    @PostMapping
    @Operation(
        summary = "Create a new record",
        description = "Creates a new record with the provided data. The file field can reference any filename, whether uploaded or not."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Record created successfully"),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request data - duplicate record or other validation errors"
            )
        ]
    )
    fun createRecord(@RequestBody request: RecordCreateRequest): ResponseEntity<Map<String, String>> {
        logger.info("Creating new record with ID: ${request.id}")

        // Check if record already exists
        if (recordService.recordExists(request.id)) {
            logger.warn("Validation failed: Record with ID '${request.id}' already exists")
            return ResponseEntity.badRequest().body(
                mapOf("message" to "Record with ID '${request.id}' already exists")
            )
        }


        try {
            val createdRecord = recordService.createRecordFromRequest(request)
            logger.info("Record created successfully: ${createdRecord.id}")
            return ResponseEntity.status(201).body(mapOf("message" to "Record created successfully", "id" to createdRecord.id))
        } catch (ex: Exception) {
            logger.error("Error creating record: ${ex.message}")
            return ResponseEntity.badRequest().body(mapOf("message" to "Error creating record: ${ex.message}"))
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing record", description = "Updates the details of an existing record.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Record updated successfully"),
            ApiResponse(
                responseCode = "404", description = "Record not found"
            )
        ]
    )
    fun updateRecord(@PathVariable id: String, @RequestBody updateData: RecordUpdateSchema): ResponseEntity<Map<String, String>> {
    logger.info("Updating record with ID: $id")

    try {
        val result = recordService.updateRecordFromRequest(id, updateData)

        if (result != null) {
            logger.info("Record with ID '$id' updated successfully")
            return ResponseEntity.ok(mapOf("message" to "Record updated successfully", "id" to id))
        } else {
            logger.warn("Update failed: Record with ID '$id' not found")
            return ResponseEntity.status(404).body(
                mapOf("message" to "Record not found")
            )
        }
    } catch (ex: Exception) {
        logger.error("Error updating record: ${ex.message}")
        return ResponseEntity.badRequest().body(mapOf("message" to "Error updating record: ${ex.message}"))
    }
}

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a record", description = "Deletes a record by its ID")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Record deleted successfully"),
            ApiResponse(responseCode = "404", description = "Record not found")
        ]
    )
    fun deleteRecord(@PathVariable id: String): ResponseEntity<Map<String, String>> {
        logger.info("Deleting record with ID: $id")

        try {
            val deleted = recordService.deleteRecord(id)
            if (deleted) {
                logger.info("Record with ID '$id' deleted successfully")
                return ResponseEntity.ok(mapOf("message" to "Record deleted successfully", "id" to id))
            } else {
                logger.warn("Delete failed: Record with ID '$id' not found")
                return ResponseEntity.status(404).body(
                    mapOf("message" to "Record not found")
                )
            }
        } catch (ex: Exception) {
            logger.error("Error deleting record: ${ex.message}")
            return ResponseEntity.badRequest().body(mapOf("message" to "Error deleting record: ${ex.message}"))
        }
    }

    @GetMapping("/files")
    @Operation(summary = "List all uploaded files", description = "Returns a list of all files with their upload dates")
    fun listFiles(): ResponseEntity<List<FileInfo>> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())

        val fileList = Files.list(uploadDir)
            .filter { Files.isRegularFile(it) }
            .map { filePath ->
                val attrs = Files.readAttributes(filePath, BasicFileAttributes::class.java)
                FileInfo(
                    filename = filePath.fileName.toString(),
                    uploadDate = formatter.format(attrs.creationTime().toInstant()),
                    size = attrs.size()
                )
            }
            .toList()
        return ResponseEntity.ok().body(fileList)
    }

    @GetMapping("/files/{filename}")
    @Operation(summary = "Download a file")
    fun downloadFile(@Parameter(description = "Name of the file to download") @PathVariable filename: String): ResponseEntity<Resource> {
        val filePath = uploadDir.resolve(filename)
        val resource: Resource = UrlResource(filePath.toUri())
        return if (resource.exists()) {
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${filename}\"")
                .body(resource)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/screenshots/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "Upload a screenshot", description = "Uploads a screenshot to the server")
    fun uploadScreenshot(
        @Parameter(description = "Screenshot to upload", content = [Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)])
        @RequestParam("screenshot") screenshot: MultipartFile
    ): ResponseEntity<Map<String, String>> {
        logger.info("Screenshot upload endpoint called")
        
        // Log authentication info
        val authentication = SecurityContextHolder.getContext().authentication
        logger.info("Authentication present: ${authentication != null}")
        logger.info("Authentication authenticated: ${authentication?.isAuthenticated}")
        logger.info("Authentication principal: ${authentication?.principal}")
        
        logger.info("Upload request received for screenshot: ${screenshot.originalFilename}, Size: ${screenshot.size} bytes")

        if (screenshot.isEmpty) {
            logger.warn("Upload failed: Screenshot is empty")
            return ResponseEntity.badRequest().body(mapOf("message" to "Screenshot can't be empty!"))
        }

        try {
            val fileName = screenshot.originalFilename ?: ""
            val filePath = screenshotDir.resolve(fileName)
            logger.info("Copying screenshot to: $filePath")
            Files.createDirectories(screenshotDir)
            Files.copy(screenshot.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)
            logger.info("Screenshot uploaded successfully: $fileName")
            return ResponseEntity.ok(mapOf("message" to "Screenshot uploaded successfully", "filename" to fileName))
        } catch (ex: Exception) {
            logger.error("Error uploading screenshot: ${ex.message}")
            return ResponseEntity.status(500).body(mapOf("message" to (ex.message ?: "Unknown error")))
        }
    }

    @DeleteMapping("/files/{filename}")
    @Operation(summary = "Delete a file", description = "Deletes a file from the server")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "File deleted successfully"),
            ApiResponse(responseCode = "404", description = "File not found")
        ]
    )
    fun deleteFile(@Parameter(description = "Name of the file to delete") @PathVariable filename: String): ResponseEntity<Map<String, String>> {
        logger.info("Deleting file: $filename")

        val filePath = uploadDir.resolve(filename)
        if (!Files.exists(filePath)) {
            logger.warn("Delete failed: File '$filename' not found")
            return ResponseEntity.status(404).body(
                mapOf("message" to "File not found")
            )
        }

        try {
            Files.delete(filePath)
            logger.info("File '$filename' deleted successfully")
            return ResponseEntity.ok(mapOf("message" to "File deleted successfully", "filename" to filename))
        } catch (ex: Exception) {
            logger.error("Error deleting file '$filename': ${ex.message}")
            return ResponseEntity.status(500).body(
                mapOf("message" to "Error deleting file: ${ex.message}")
            )
        }
    }

    @GetMapping("/screenshots")
    @Operation(summary = "List all uploaded screenshots", description = "Returns a list of all screenshots with their upload dates")
    fun listScreenshots(): ResponseEntity<List<FileInfo>> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())

        val screenshotList = Files.list(screenshotDir)
            .filter { Files.isRegularFile(it) }
            .map { filePath ->
                val attrs = Files.readAttributes(filePath, BasicFileAttributes::class.java)
                FileInfo(
                    filename = filePath.fileName.toString(),
                    uploadDate = formatter.format(attrs.creationTime().toInstant()),
                    size = attrs.size()
                )
            }
            .toList()
        return ResponseEntity.ok().body(screenshotList)
    }

    @GetMapping("/screenshots/{filename}")
    @Operation(summary = "Download a screenshot")
    fun downloadScreenshot(@Parameter(description = "Name of the screenshot to download") @PathVariable filename: String): ResponseEntity<Resource> {
        val filePath = screenshotDir.resolve(filename)
        val resource: Resource = UrlResource(filePath.toUri())
        return if (resource.exists()) {
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${filename}\"")
                .body(resource)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/screenshots/{filename}")
    @Operation(summary = "Delete a screenshot", description = "Deletes a screenshot from the server")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Screenshot deleted successfully"),
            ApiResponse(responseCode = "404", description = "Screenshot not found")
        ]
    )
    fun deleteScreenshot(@Parameter(description = "Name of the screenshot to delete") @PathVariable filename: String): ResponseEntity<Map<String, String>> {
        logger.info("Deleting screenshot: $filename")

        val filePath = screenshotDir.resolve(filename)
        if (!Files.exists(filePath)) {
            logger.warn("Delete failed: Screenshot '$filename' not found")
            return ResponseEntity.status(404).body(
                mapOf("message" to "Screenshot not found")
            )
        }

        try {
            Files.delete(filePath)
            logger.info("Screenshot '$filename' deleted successfully")
            return ResponseEntity.ok(mapOf("message" to "Screenshot deleted successfully", "filename" to filename))
        } catch (ex: Exception) {
            logger.error("Error deleting screenshot '$filename': ${ex.message}")
            return ResponseEntity.status(500).body(
                mapOf("message" to "Error deleting screenshot: ${ex.message}")
            )
        }
    }

    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "Upload a file", description = "Uploads a file to the server")
    fun uploadRecord(
        @Parameter(
            description = "File to upload",
            content = [Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)]
        )
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<Map<String, String>> {
        logger.info("Upload request received. File: ${file.originalFilename}, Size: ${file.size} bytes")

        if (file.isEmpty) {
            logger.warn("Upload failed: File is empty")
            return ResponseEntity.badRequest().body(mapOf("message" to "File can't be empty!"))
        }
        try {
            val fileName = "${file.originalFilename}"
            val filePath = uploadDir.resolve(fileName)
            logger.info("Copying file to: $filePath")
            Files.copy(file.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)
            logger.info("File uploaded successfully: $fileName")
            return ResponseEntity.ok().body(mapOf("message" to "successfully upload file"))
        } catch (ex: Exception) {
            return ResponseEntity.status(500).body(mapOf("message" to (ex.message ?: "Unknown error")))
        }
    }

}
