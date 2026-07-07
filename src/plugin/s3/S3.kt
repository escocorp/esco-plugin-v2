package plugin.s3

import plugin.PVars
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import java.net.URI

class S3(val baseUrl: String, val accessKey: String, val secretKey: String) {
    val client:  S3Client
    init {
        client = S3Client.builder()
            .endpointOverride(URI.create(baseUrl))
            .region(Region.EU_WEST_1)
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        accessKey,
                        secretKey
                    )
                )
            )
            .forcePathStyle(true)
            .build()
    }

    fun putObject(bucket: String, name: String, bytes: ByteArray): PutObjectResponse {
        return client.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key("${PVars.gamemode.simpleName}/${name}")
                .build(),
            RequestBody.fromBytes(bytes)
        )
    }
}