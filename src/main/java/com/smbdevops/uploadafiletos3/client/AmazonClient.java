package com.smbdevops.uploadafiletos3.client;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.smbdevops.uploadafiletos3.configuration.AmazonConfiguration;
import org.springframework.stereotype.Service;

@Service
public class AmazonClient {
    private AmazonS3 s3client;

    private final AmazonConfiguration configuration;

    public AmazonClient(final AmazonConfiguration configuration) {
        this.configuration = configuration;
        this.initializeAmazon();
    }

    private void initializeAmazon() {
        final AWSCredentials credentials = new BasicAWSCredentials(
                this.configuration.getAccessKey(),
                this.configuration.getSecretKey()
        );
        this.s3client = AmazonS3Client
                .builder()
                .withRegion(null != this.configuration.getRegion() ? this.configuration.getRegion() : "us-west-2")
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

    public final AmazonS3 getS3client() {
        return this.s3client;
    }

    public final String getBucketName() {
        return this.configuration.getBucketName();
    }

    public final String getAccessKeyId() {
        return this.configuration.getAccessKey();
    }

    public final String getRegion() {
        return this.configuration.getRegion();
    }
}
