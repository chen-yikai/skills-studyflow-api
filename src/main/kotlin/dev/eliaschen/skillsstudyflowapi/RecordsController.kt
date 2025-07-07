package dev.eliaschen.skillsstudyflowapi

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.bind.annotation.ExceptionHandler
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

data class Note(
    val time: Int,
    val data: String
)

data class Record(
    val id: String,
    val name: String,
    val file: String,
    val note: List<Note>
)

@RestController
@RequestMapping("/records")
@Tag(name = "Records", description = "File upload and download API")
class RecordsController {
    private val logger = LoggerFactory.getLogger(RecordsController::class.java)
    private val uploadDir: Path = Paths.get("records").toAbsolutePath()
    private val records = mutableListOf<Record>()

    init {
        Files.createDirectories(uploadDir)
    }

    @GetMapping
    @Operation(summary = "Get all records", description = "Returns a list of all records")
    @ApiResponse(responseCode = "200", description = "Records retrieved successfully")
    fun getRecords(): ResponseEntity<List<Record>> {
        logger.info("Getting all records. Count: ${records.size}")
        return ResponseEntity.ok(records)
    }

    @PostMapping
    @Operation(
        summary = "Create a new record", 
        description = "Creates a new record with the provided data. Note: record ID must match the file name, and the file must be uploaded first."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Record created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid request data - ID/file mismatch, file not found, or duplicate record")
    ])
    fun createRecord(@RequestBody record: Record): ResponseEntity<Map<String, String>> {
        logger.info("Creating new record with ID: ${record.id}")
        
        // Validate that record ID matches the file name
        if (record.id != record.file) {
            logger.warn("Validation failed: Record ID '${record.id}' does not match file name '${record.file}'")
            return ResponseEntity.badRequest().body(
                mapOf("message" to "Record ID must match the file name. ID: '${record.id}', File: '${record.file}'")
            )
        }
        
        // Check if the uploaded file actually exists
        val filePath = uploadDir.resolve(record.file)
        if (!Files.exists(filePath)) {
            logger.warn("Validation failed: File '${record.file}' not found in uploads directory")
            return ResponseEntity.badRequest().body(
                mapOf("message" to "File '${record.file}' must be uploaded before creating a record. Please upload the file first.")
            )
        }
        
        // Check if record with this ID already exists
        if (records.any { it.id == record.id }) {
            logger.warn("Validation failed: Record with ID '${record.id}' already exists")
            return ResponseEntity.badRequest().body(
                mapOf("message" to "Record with ID '${record.id}' already exists")
            )
        }
        
        try {
            records.add(record)
            logger.info("Record created successfully: ${record.id}")
            return ResponseEntity.status(201).body(mapOf("message" to "Record created successfully", "id" to record.id))
        } catch (ex: Exception) {
            logger.error("Error creating record: ${ex.message}")
            return ResponseEntity.badRequest().body(mapOf("message" to "Error creating record: ${ex.message}"))
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
