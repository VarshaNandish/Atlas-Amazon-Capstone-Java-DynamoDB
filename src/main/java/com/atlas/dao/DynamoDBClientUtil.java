package com.atlas.dao;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

public final class DynamoDBClientUtil {
    private static volatile DynamoDbClient client;

    private DynamoDBClientUtil() {}

    public static DynamoDbClient client() {
        if (client == null) {
            synchronized (DynamoDBClientUtil.class) {
                if (client == null) {
                    client = DynamoDbClient.builder()
                            .endpointOverride(URI.create("http://localhost:8000"))
                            .region(Region.AP_SOUTH_1)
                            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("dummy", "dummy")))
                            .build();
                }
            }
        }
        return client;
    }
}
