package team.hanseungil.nochu.infrastructure.s3.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import team.hanseungil.nochu.global.error.ErrorCode
import team.hanseungil.nochu.global.error.GlobalException

@Service
class S3DeleteService(
    private val s3Client: S3Client,
) {
    @Value("\${cloud.aws.s3.bucket}")
    private lateinit var bucket: String

    @Value("\${cloud.aws.region.static}")
    private lateinit var region: String

    fun delete(fileUrl: String) {
        try {
            val fileName = extractFileNameFromUrl(fileUrl)
            deleteFromS3(fileName)
        } catch (e: Exception) {
            throw GlobalException(ErrorCode.INVALID_INPUT_VALUE)
        }
    }

    private fun extractFileNameFromUrl(fileUrl: String): String {
        return fileUrl.substringAfter("${bucket}.s3.${region}.amazonaws.com/")
    }

    private fun deleteFromS3(fileName: String) {
        val deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(fileName)
            .build()

        s3Client.deleteObject(deleteObjectRequest)
    }
}
