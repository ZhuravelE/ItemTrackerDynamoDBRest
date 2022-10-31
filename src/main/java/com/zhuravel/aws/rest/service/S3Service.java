package com.zhuravel.aws.rest.service;

import com.zhuravel.aws.rest.model.FileAttributeItem;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketConfiguration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Evgenii Zhuravel created on 27.10.2022
 */
@Service
public class S3Service {

    private static final String BUCKET = "zhuravel-test-bucket" /*+ System.currentTimeMillis()*/;
    private final Region region;

    private final DynamoDBService dynamoDBService;

    public S3Service(DynamoDBService dynamoDBService) {
        this.dynamoDBService = dynamoDBService;
        this.region = Region.EU_CENTRAL_1;
    }

    public String retrieveBucket(S3Client s3Client) {
        if (!isBucketExist(s3Client)) {
            createBucket(s3Client, BUCKET);
        }
        return BUCKET;
    }

    public void createBucket(S3Client s3Client, String bucketName) {
        try {
            s3Client.createBucket(CreateBucketRequest
                    .builder()
                    .bucket(bucketName)
                    .createBucketConfiguration(
                            CreateBucketConfiguration.builder()
                                    .locationConstraint(region.id())
                                    .build())
                    .build());

            s3Client.waiter().waitUntilBucketExists(HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build());

        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    public void putObject(MultipartFile file) {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        S3Client s3 = S3Client.builder().region(region).build();

        String bucket = retrieveBucket(s3);

        try {
            s3.putObject(PutObjectRequest
                            .builder()
                            .bucket(bucket)
                            .key(fileName)
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            GetUrlRequest getUrlRequest = GetUrlRequest.builder().bucket(bucket).key(fileName).build();
            String url = s3.utilities().getUrl(getUrlRequest).toExternalForm();

            FileAttributeItem item = new FileAttributeItem();
            item.setFilename(fileName);
            item.setSize(file.getSize());
            item.setUrl(url);

            dynamoDBService.putItem(item);
        } catch (IOException e) {
            e.printStackTrace();
        }

        s3.close();
    }

    public void cleanUp(S3Client s3Client, String bucketName, String keyName) {
        try {
            deleteObject(s3Client, bucketName, keyName);

            deleteBucket(s3Client, bucketName);
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    public List<FileAttributeItem> getFiles() {
        S3Client s3 = S3Client.builder().region(region).build();

        try {
            ListObjectsRequest listObjects = ListObjectsRequest
                    .builder()
                    .bucket(BUCKET)
                    .build();

            List<String> keys = s3.listObjects(listObjects).contents().stream()
                    .map(S3Object::key)
                    .collect(Collectors.toList());

            return dynamoDBService.getAllItems(keys);
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return null;
    }

    private void deleteObject(S3Client s3Client, String bucketName, String keyName) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName).key(keyName).build();
        s3Client.deleteObject(deleteObjectRequest);
    }

    private void deleteBucket(S3Client s3Client, String bucketName) {
        DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder().bucket(bucketName).build();
        s3Client.deleteBucket(deleteBucketRequest);
    }

    private boolean isBucketExist(S3Client s3Client) {
        HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                .bucket(BUCKET)
                .build();
        try {
            s3Client.headBucket(headBucketRequest);
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        }
    }

    public void delete(String fileName) {
        try {
            S3Client s3 = S3Client.builder().region(region).build();

            deleteObject(s3, BUCKET, fileName);

            dynamoDBService.deleteItem(fileName);
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }
}
