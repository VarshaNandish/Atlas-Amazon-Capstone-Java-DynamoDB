package com.atlas.dao;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class LogDao {
    private final DynamoDbClient client = DynamoDBClientUtil.client();
    private final String table = "StudentLogs";

    public void append(String logId, String studentId, String action, String courseId) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("logId", AttributeValue.builder().s(logId).build());
        item.put("studentId", AttributeValue.builder().s(studentId).build());
        item.put("action", AttributeValue.builder().s(action).build());
        if (courseId != null) item.put("courseId", AttributeValue.builder().s(courseId).build());
        item.put("timestamp", AttributeValue.builder().n(String.valueOf(Instant.now().toEpochMilli())).build());
        client.putItem(PutItemRequest.builder().tableName(table).item(item).build());
    }
}

