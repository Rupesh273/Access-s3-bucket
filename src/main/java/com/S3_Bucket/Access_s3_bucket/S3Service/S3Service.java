package com.S3_Bucket.Access_s3_bucket.S3Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.*;
import java.time.Duration;
import java.util.Objects;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    public S3Service (){
        this.s3Client =S3Client.builder().region(Region.of("ap-south-1")).
                credentialsProvider(InstanceProfileCredentialsProvider.create())
                .build();
       this.s3Presigner = S3Presigner.builder().region(Region.of("ap-south-1")).
                credentialsProvider(InstanceProfileCredentialsProvider.create()).
                build();
    }

    private File convertMultipartFileToFile(MultipartFile file) throws IOException {
        File convFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
    public String uploadImage(MultipartFile file, String bucketName, String keyName) throws IOException {
        File convertedFile = convertMultipartFileToFile(file);

        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(keyName)
                            .build(),
                    convertedFile.toPath());

            // Generate and return the URL
            return generatePresidedUrl(bucketName, keyName);

        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            return null;
        } finally {
            // Cleanup the temporary file
            if (convertedFile.exists()) {
                convertedFile.delete();
            }
        }
    }
    private String generatePresidedUrl(String bucketName, String keyName) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(60))  // URL valid for 60 minutes
                .getObjectRequest(builder -> builder.bucket(bucketName).key(keyName))
                .build();

        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedGetObjectRequest.url().toString();
    }
}
