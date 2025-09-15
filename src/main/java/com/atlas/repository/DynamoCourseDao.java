package com.atlas.repository;

import com.atlas.dao.CourseDao;
import com.atlas.model.Course;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class DynamoCourseDao implements CourseDao {
    private final DynamoDbClient client = DynamoDBClientUtil.client();
    private final String table = "Courses";

    @Override
    public Course getById(String courseId) {
        GetItemResponse r = client.getItem(GetItemRequest.builder()
                .tableName(table)
                .key(Map.of("courseId", AttributeValue.builder().s(courseId).build()))
                .build());
        Map<String, AttributeValue> it = r.item();
        if (it == null || it.isEmpty()) return null;
        Course c = new Course(courseId, it.get("courseName").s(), Integer.parseInt(it.get("maxSeats").n()));
        c.setCurrentEnrolledCount(Integer.parseInt(it.get("currentEnrolledCount").n()));
        c.setStartDate(it.get("startDate").s());
        c.setEndDate(it.get("endDate").s());
        c.setLatestEnrollmentBy(it.get("latestEnrollmentBy").s());
        if (it.containsKey("enrolledIds")) c.getEnrolledIds().addAll(it.get("enrolledIds").l().stream().map(AttributeValue::s).collect(Collectors.toList()));
        if (it.containsKey("waitlistIds")) c.getWaitlistIds().addAll(it.get("waitlistIds").l().stream().map(AttributeValue::s).collect(Collectors.toList()));
        return c;
    }

    @Override
    public List<Course> listAll() {
        ScanResponse resp = client.scan(ScanRequest.builder().tableName(table).build());
        return resp.items().stream().map(it -> getById(it.get("courseId").s())).collect(Collectors.toList());
    }

    @Override
    public boolean enrollStudentAtomic(String courseId, String studentId, int maxSeats) {
        Map<String, AttributeValue> key = Map.of("courseId", AttributeValue.builder().s(courseId).build());
        String update = "SET currentEnrolledCount = currentEnrolledCount + :inc, enrolledIds = list_append(if_not_exists(enrolledIds, :empty), :new)";
        Map<String, AttributeValue> vals = new HashMap<>();
        vals.put(":inc", AttributeValue.builder().n("1").build());
        vals.put(":new", AttributeValue.builder().l(Collections.singletonList(AttributeValue.builder().s(studentId).build())).build());
        vals.put(":empty", AttributeValue.builder().l(Collections.emptyList()).build());
        vals.put(":max", AttributeValue.builder().n(String.valueOf(maxSeats)).build());

        UpdateItemRequest req = UpdateItemRequest.builder()
                .tableName(table)
                .key(key)
                .updateExpression(update)
                .conditionExpression("currentEnrolledCount < :max")
                .expressionAttributeValues(vals)
                .build();

        try {
            client.updateItem(req);
            return true;
        } catch (ConditionalCheckFailedException ex) {
            return false;
        }
    }

    @Override
    public void replaceEnrolled(String courseId, List<String> newEnrolledIds, int newCount) {
        Map<String, AttributeValue> key = Map.of("courseId", AttributeValue.builder().s(courseId).build());
        List<AttributeValue> av = newEnrolledIds.stream().map(id -> AttributeValue.builder().s(id).build()).collect(Collectors.toList());
        Map<String, AttributeValue> vals = Map.of(
                ":e", AttributeValue.builder().l(av).build(),
                ":cnt", AttributeValue.builder().n(String.valueOf(newCount)).build()
        );
        client.updateItem(UpdateItemRequest.builder()
                .tableName(table)
                .key(key)
                .updateExpression("SET enrolledIds = :e, currentEnrolledCount = :cnt")
                .expressionAttributeValues(vals)
                .build());
    }

    @Override
    public void addToWaitlist(String courseId, String studentId) {
        Map<String, AttributeValue> key = Map.of("courseId", AttributeValue.builder().s(courseId).build());
        String update = "SET waitlistIds = list_append(if_not_exists(waitlistIds, :empty), :new)";
        Map<String, AttributeValue> vals = Map.of(
                ":empty", AttributeValue.builder().l(Collections.emptyList()).build(),
                ":new", AttributeValue.builder().l(Collections.singletonList(AttributeValue.builder().s(studentId).build())).build()
        );
        client.updateItem(UpdateItemRequest.builder().tableName(table).key(key).updateExpression(update).expressionAttributeValues(vals).build());
    }

    @Override
    public void replaceWaitlist(String courseId, List<String> newWaitlist) {
        Map<String, AttributeValue> key = Map.of("courseId", AttributeValue.builder().s(courseId).build());
        List<AttributeValue> av = newWaitlist.stream().map(s -> AttributeValue.builder().s(s).build()).collect(Collectors.toList());
        client.updateItem(UpdateItemRequest.builder().tableName(table).key(key).updateExpression("SET waitlistIds = :wl").expressionAttributeValues(Map.of(":wl", AttributeValue.builder().l(av).build())).build());
    }
}
