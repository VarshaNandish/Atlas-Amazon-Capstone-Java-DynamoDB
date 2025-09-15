package com.atlas.repository;

import com.atlas.dao.StudentDao;
import com.atlas.model.Student;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class DynamoStudentDao implements StudentDao {
    private final DynamoDbClient client = DynamoDBClientUtil.client();
    private final String table = "Students";

    @Override
    public void save(Student s) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().s(s.getId()).build());
        item.put("name", AttributeValue.builder().s(s.getName()).build());
        item.put("email", AttributeValue.builder().s(s.getEmail()).build());
        item.put("passwordHash", AttributeValue.builder().s(s.getPasswordHash()).build());
        item.put("enrolledCourseIds", AttributeValue.builder().l(
                s.getEnrolledCourseIds().stream().map(id -> AttributeValue.builder().s(id).build()).collect(Collectors.toList())).build());
        item.put("waitlistedCourseIds", AttributeValue.builder().l(
                s.getWaitlistedCourseIds().stream().map(id -> AttributeValue.builder().s(id).build()).collect(Collectors.toList())).build());
        client.putItem(PutItemRequest.builder().tableName(table).item(item).build());
    }

    @Override
    public Student getById(String id) {
        GetItemResponse resp = client.getItem(GetItemRequest.builder()
                .tableName(table)
                .key(Map.of("id", AttributeValue.builder().s(id).build()))
                .build());
        Map<String, AttributeValue> it = resp.item();
        if (it == null || it.isEmpty()) return null;
        String name = it.get("name").s();
        String email = it.get("email").s();
        String hash = it.get("passwordHash").s();
        Student s = new Student(id, name, email, hash);
        if (it.containsKey("enrolledCourseIds")) s.getEnrolledCourseIds().addAll(it.get("enrolledCourseIds").l().stream().map(AttributeValue::s).collect(Collectors.toList()));
        if (it.containsKey("waitlistedCourseIds")) s.getWaitlistedCourseIds().addAll(it.get("waitlistedCourseIds").l().stream().map(AttributeValue::s).collect(Collectors.toList()));
        return s;
    }

    @Override
    public Student findByEmail(String email) {
        ScanResponse resp = client.scan(ScanRequest.builder().tableName(table)
                .filterExpression("email = :e")
                .expressionAttributeValues(Map.of(":e", AttributeValue.builder().s(email).build()))
                .build());
        if (resp.count() == 0) return null;
        String id = resp.items().get(0).get("id").s();
        return getById(id);
    }

    @Override
    public List<Student> listAll() {
        ScanResponse resp = client.scan(ScanRequest.builder().tableName(table).build());
        return resp.items().stream().map(i -> getById(i.get("id").s())).collect(Collectors.toList());
    }
}
