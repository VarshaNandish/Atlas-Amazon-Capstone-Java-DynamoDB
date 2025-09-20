package com.atlas.repository;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;
import java.util.Objects;

public final class DynamoDBClientUtil {
    private static volatile DynamoDbClient client;
    private DynamoDBClientUtil() {}

    public static DynamoDbClient client() {
        if (client == null) {
            synchronized (DynamoDBClientUtil.class) {
                if (client == null) {
                    // read endpoint from env var so tests inside containers can use service name
                    String endpoint = System.getenv("DYNAMODB_ENDPOINT");
                    if (endpoint == null || endpoint.isBlank()) {
                        endpoint = "http://localhost:8000"; // default for local dev
                    }
                    client = DynamoDbClient.builder()
                            .endpointOverride(URI.create(endpoint))
                            .region(Region.AP_SOUTH_1)
                            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("dummy", "dummy")))
                            .build();
                }
            }
        }
        return client;
    }
}
