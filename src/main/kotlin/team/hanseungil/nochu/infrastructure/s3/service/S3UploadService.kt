package team.hanseungil.nochu.infrastructure.s3.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.util.StringUtils
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import team.hanseungil.nochu.global.error.ErrorCode
import team.hanseungil.nochu.global.error.GlobalException
import java.util.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class S3UploadService(
    private val s3Client: S3Client,
) {
    @Value("\${cloud.aws.s3.bucket}")
    private lateinit var bucket: String
    fun execute(file: MultipartFile): String {
        validateFile(file)

        val originalFilename =
            file.originalFilename
                ?: throw GlobalException(ErrorCode.FILE_NOT_FOUND)
        val fileExtension =
            StringUtils.getFilenameExtension(originalFilename)
                ?: throw GlobalException(ErrorCode.FILE_EXTENSION_NOT_FOUND)
        val storedFilename = generateStoredFilename(fileExtension)
        return s3Client.putObject(PutObjectRequest.builder()
            .bucket(bucket)
            .key(storedFilename)
            .build(), RequestBody.fromBytes(file.bytes)).toString()
    }

    private fun validateFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw GlobalException(ErrorCode.FILE_EMPTY)
        }

        val originalFilename = file.originalFilename
            ?: throw GlobalException(ErrorCode.FILE_NOT_FOUND)

        val extension = StringUtils
            .getFilenameExtension(originalFilename)
            ?.lowercase()
            ?: throw GlobalException(ErrorCode.FILE_EXTENSION_NOT_FOUND)

        val allowedExtensions = setOf("jpg", "jpeg", "png")
        if (extension !in allowedExtensions) {
            throw GlobalException(ErrorCode.FILE_EXTENSION_NOT_ALLOWED)
        }
    }

    private fun generateStoredFilename(fileExtension: String): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val uuid = UUID.randomUUID().toString().replace("-", "")
        return "file/${timestamp}_$uuid.$fileExtension"
    }
}
