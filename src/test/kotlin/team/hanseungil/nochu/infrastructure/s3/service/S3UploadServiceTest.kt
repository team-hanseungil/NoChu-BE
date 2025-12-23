package team.hanseungil.nochu.infrastructure.s3.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.S3Utilities
import software.amazon.awssdk.services.s3.model.GetUrlRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import java.util.function.Consumer
import java.net.URI
import team.hanseungil.nochu.global.error.ErrorCode
import team.hanseungil.nochu.global.error.GlobalException

class S3UploadServiceTest : BehaviorSpec({
    Given("유효한 이미지 파일이 주어졌을 때") {
        val s3Client = mockk<S3Client>(relaxed = true)
        val uploadService = S3UploadService(s3Client)
        val bucketField = S3UploadService::class.java.getDeclaredField("bucket")
        bucketField.isAccessible = true
        bucketField.set(uploadService, "test-bucket")

        val file = mockk<MultipartFile>()
        val fileBytes = "test content".toByteArray()

        every { file.isEmpty } returns false
        every { file.originalFilename } returns "test.jpg"
        every { file.bytes } returns fileBytes
        every { file.contentType } returns "image/jpeg"
        every { s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) } returns mockk<PutObjectResponse>(relaxed = true)
        val s3Utilities = mockk<S3Utilities>()
        every { s3Client.utilities() } returns s3Utilities
        every { s3Utilities.getUrl(any<GetUrlRequest>()) } returns URI.create("https://test-bucket.s3.amazonaws.com/test-key").toURL()
        every { s3Utilities.getUrl(any<Consumer<GetUrlRequest.Builder>>()) } returns
            URI.create("https://test-bucket.s3.amazonaws.com/test-key").toURL()

        When("파일을 업로드하면") {
            lateinit var result: String

            beforeTest {
                result = uploadService.execute(file)
            }

            Then("S3 업로드 결과를 반환한다") {
                result shouldBe "https://test-bucket.s3.amazonaws.com/test-key"
            }

            Then("S3Client의 putObject가 호출된다") {
                verify(atLeast = 1) { s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) }
            }
        }
    }

    Given("빈 파일이 주어졌을 때") {
        val s3Client = mockk<S3Client>(relaxed = true)
        val uploadService = S3UploadService(s3Client)

        val file = mockk<MultipartFile>()
        every { file.isEmpty } returns true

        When("파일을 업로드하면") {
            Then("FILE_EMPTY 예외가 발생한다") {
                val exception = shouldThrow<GlobalException> {
                    uploadService.execute(file)
                }
                exception.errorCode shouldBe ErrorCode.FILE_EMPTY
            }
        }
    }

    Given("파일명이 없는 파일이 주어졌을 때") {
        val s3Client = mockk<S3Client>(relaxed = true)
        val uploadService = S3UploadService(s3Client)

        val file = mockk<MultipartFile>()
        every { file.isEmpty } returns false
        every { file.originalFilename } returns null

        When("파일을 업로드하면") {
            Then("FILE_NOT_FOUND 예외가 발생한다") {
                val exception = shouldThrow<GlobalException> {
                    uploadService.execute(file)
                }
                exception.errorCode shouldBe ErrorCode.FILE_NOT_FOUND
            }
        }
    }

    Given("허용되지 않은 확장자 파일이 주어졌을 때") {
        val s3Client = mockk<S3Client>(relaxed = true)
        val uploadService = S3UploadService(s3Client)

        val file = mockk<MultipartFile>()
        every { file.isEmpty } returns false
        every { file.originalFilename } returns "test.pdf"

        When("파일을 업로드하면") {
            Then("FILE_EXTENSION_NOT_ALLOWED 예외가 발생한다") {
                val exception = shouldThrow<GlobalException> {
                    uploadService.execute(file)
                }
                exception.errorCode shouldBe ErrorCode.FILE_EXTENSION_NOT_ALLOWED
            }
        }
    }
})
