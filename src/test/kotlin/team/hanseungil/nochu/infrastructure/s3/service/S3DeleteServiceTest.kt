package team.hanseungil.nochu.infrastructure.s3.service

import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.*
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest

class S3DeleteServiceTest : BehaviorSpec({
    Given("S3 파일 URL이 주어졌을 때") {
        val s3Client = mockk<S3Client>()
        val deleteService = S3DeleteService(s3Client)
        val bucketField = S3DeleteService::class.java.getDeclaredField("bucket")
        val regionField = S3DeleteService::class.java.getDeclaredField("region")
        bucketField.isAccessible = true
        regionField.isAccessible = true
        bucketField.set(deleteService, "test-bucket")
        regionField.set(deleteService, "ap-northeast-2")

        val fileUrl = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/images/test-file.jpg"
        
        every { s3Client.deleteObject(any<DeleteObjectRequest>()) } returns mockk()

        When("파일을 삭제하면") {
            deleteService.delete(fileUrl)

            Then("S3Client의 deleteObject가 호출된다") {
                verify(exactly = 1) {
                    s3Client.deleteObject(match<DeleteObjectRequest> { request ->
                        request.bucket() == "test-bucket" &&
                        request.key() == "images/test-file.jpg"
                    })
                }
            }
        }
    }

    Given("여러 경로가 포함된 파일 URL이 주어졌을 때") {
        val s3Client = mockk<S3Client>()
        val deleteService = S3DeleteService(s3Client)
        val bucketField = S3DeleteService::class.java.getDeclaredField("bucket")
        val regionField = S3DeleteService::class.java.getDeclaredField("region")
        bucketField.isAccessible = true
        regionField.isAccessible = true
        bucketField.set(deleteService, "test-bucket")
        regionField.set(deleteService, "ap-northeast-2")

        val fileUrl = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/images/emotions/2024/test.jpg"
        
        every { s3Client.deleteObject(any<DeleteObjectRequest>()) } returns mockk()

        When("파일을 삭제하면") {
            deleteService.delete(fileUrl)

            Then("올바른 경로로 S3Client가 호출된다") {
                verify(exactly = 1) {
                    s3Client.deleteObject(match<DeleteObjectRequest> { request ->
                        request.key() == "images/emotions/2024/test.jpg"
                    })
                }
            }
        }
    }
})
