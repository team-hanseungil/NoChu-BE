package team.hanseungil.nochu.global.s3.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import team.hanseungil.nochu.global.error.ErrorCode
import team.hanseungil.nochu.global.error.GlobalException
import java.util.*

@Service
class S3UploadService(
    private val s3Client: S3Client,
) {
    @Value("\${cloud.aws.s3.bucket}")
    private lateinit var bucket: String

    @Value("\${cloud.aws.region.static}")
    private lateinit var region: String

    fun upload(file: MultipartFile, directory: String): String {
        validateFile(file)

        val originalFilename = file.originalFilename 
            ?: throw GlobalException(ErrorCode.INVALID_INPUT_VALUE)

        val fileName = generateFileName(directory, originalFilename)

        return try {
            uploadToS3(file, fileName)
            buildFileUrl(fileName)
        } catch (e: Exception) {
            throw GlobalException(ErrorCode.INVALID_INPUT_VALUE)
        }
    }

    private fun validateFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw GlobalException(ErrorCode.INVALID_INPUT_VALUE)
        }
    }

    private fun generateFileName(directory: String, originalFilename: String): String {
        val extension = originalFilename.substringAfterLast(".", "")
        return "${directory}/${UUID.randomUUID()}.${extension}"
    }

    private fun uploadToS3(file: MultipartFile, fileName: String) {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(fileName)
            .contentType(file.contentType)
            .build()

        s3Client.putObject(
            putObjectRequest,
            RequestBody.fromInputStream(file.inputStream, file.size)
        )
    }

    private fun buildFileUrl(fileName: String): String {
        return "https://${bucket}.s3.${region}.amazonaws.com/${fileName}"
    }
}
