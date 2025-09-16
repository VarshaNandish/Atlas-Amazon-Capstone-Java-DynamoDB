package com.atlas.repository;

import com.atlas.dao.LogDao;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class DynamoLogDao implements LogDao {
    private final DynamoDbClient client = DynamoDBClientUtil.client();
    private final String table = "StudentLogs";

    @Override
    public void append(String logId, String studentId, String action, String courseId) {
        // store ISO-8601 timestamp only under "timestamp"
        String iso = Instant.now().toString(); // e.g. 2025-09-16T12:34:56.789Z

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("logId", AttributeValue.builder().s(logId).build());
        item.put("studentId", AttributeValue.builder().s(studentId).build());
        item.put("action", AttributeValue.builder().s(action).build());
        if (courseId != null) item.put("courseId", AttributeValue.builder().s(courseId).build());

        // only ISO string under 'timestamp'
        item.put("timestamp", AttributeValue.builder().s(iso).build());

        client.putItem(PutItemRequest.builder().tableName(table).item(item).build());
    }
}
