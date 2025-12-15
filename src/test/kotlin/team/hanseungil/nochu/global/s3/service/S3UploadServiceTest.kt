package team.hanseungil.nochu.global.s3.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.*
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import team.hanseungil.nochu.global.error.ErrorCode
import team.hanseungil.nochu.global.error.GlobalException
import java.io.ByteArrayInputStream

class S3UploadServiceTest : BehaviorSpec({
    Given("파일과 디렉토리가 주어졌을 때") {
        val s3Client = mockk<S3Client>()
        val uploadService = S3UploadService(s3Client)
        val bucketField = S3UploadService::class.java.getDeclaredField("bucket")
        val regionField = S3UploadService::class.java.getDeclaredField("region")
        bucketField.isAccessible = true
        regionField.isAccessible = true
        bucketField.set(uploadService, "test-bucket")
        regionField.set(uploadService, "ap-northeast-2")

        val file = mockk<MultipartFile>()
        val directory = "images"
        val fileContent = ByteArrayInputStream("test content".toByteArray())

        every { file.isEmpty } returns false
        every { file.originalFilename } returns "test.jpg"
        every { file.contentType } returns "image/jpeg"
        every { file.inputStream } returns fileContent
        every { file.size } returns 100L
        every { s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) } returns mockk()

        When("파일을 업로드하면") {
            val result = uploadService.upload(file, directory)

            Then("S3 URL을 반환한다") {
                result shouldContain "https://test-bucket.s3.ap-northeast-2.amazonaws.com"
                result shouldContain directory
                result shouldContain ".jpg"
            }

            Then("S3Client가 호출된다") {
                verify(exactly = 1) { s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) }
            }
        }
    }

    Given("빈 파일이 주어졌을 때") {
        val s3Client = mockk<S3Client>()
        val uploadService = S3UploadService(s3Client)
        
        val file = mockk<MultipartFile>()
        every { file.isEmpty } returns true

        When("파일을 업로드하면") {
            Then("INVALID_INPUT_VALUE 예외가 발생한다") {
                val exception = shouldThrow<GlobalException> {
                    uploadService.upload(file, "images")
                }
                exception.errorCode shouldBe ErrorCode.INVALID_INPUT_VALUE
            }
        }
    }

    Given("파일명이 없는 파일이 주어졌을 때") {
        val s3Client = mockk<S3Client>()
        val uploadService = S3UploadService(s3Client)
        
        val file = mockk<MultipartFile>()
        every { file.isEmpty } returns false
        every { file.originalFilename } returns null

        When("파일을 업로드하면") {
            Then("INVALID_INPUT_VALUE 예외가 발생한다") {
                val exception = shouldThrow<GlobalException> {
                    uploadService.upload(file, "images")
                }
                exception.errorCode shouldBe ErrorCode.INVALID_INPUT_VALUE
            }
        }
    }
})
