package com.zhuravel.aws.rest.service;

import com.zhuravel.aws.rest.model.FileAttribute;
import com.zhuravel.aws.rest.model.FileAttributeItem;
import com.zhuravel.aws.rest.model.converter.FileAttributeConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Evgenii Zhuravel created on 24.10.2022
 */
@Service
public class DynamoDBService {

    private static final String TABLE_NAME = "FileAttributes";

    @Value("${date.format}")
    private String dateFormatNow;

    private final FileAttributeConverter converter;

    public DynamoDBService(FileAttributeConverter converter) {
        this.converter = converter;
    }

    private DynamoDbClient getClient() {
        Region region = Region.EU_CENTRAL_1;
        return DynamoDbClient.builder()
                .region(region)
                .build();
    }

    public List<FileAttributeItem> getAllItems() {
        try {
            DynamoDbTable<FileAttribute> table = getTable();

            return converter.convert(table.scan().items().iterator());

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return null;
    }

    public List<FileAttributeItem> getAllItems(List<String> keys) {
        try {
            List<FileAttributeItem> allItems = getAllItems();

            return allItems.stream()
                    .filter(item -> keys.contains(item.getFilename()))
                    .collect(Collectors.toList());

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return null;
    }

    public FileAttributeItem getItemByName(String filename) {
        DynamoDbTable<FileAttribute> table = getTable();

        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":val1", AttributeValue.builder()
                .s(filename)
                .build());

        Map<String, String> names = new HashMap<>();
        names.put("#filename", "filename");

        Expression expression = Expression.builder()
                .expressionValues(values)
                .expressionNames(names)
                .expression("#filename = :val1")
                .build();

        ScanEnhancedRequest enhancedRequest = ScanEnhancedRequest.builder()
                .filterExpression(expression)
                .limit(1)
                .build();

        return converter.convert(table.scan(enhancedRequest).items().iterator()).get(0);
    }

    public void putItem(FileAttributeItem item) {
        try {
            DynamoDbTable<FileAttribute> workTable = getTable();

            String myGuid = java.util.UUID.randomUUID().toString();

            FileAttribute record = new FileAttribute();
            record.setId(myGuid);
            record.setFilename(item.getFilename());
            record.setDate(now());
            record.setSize(item.getSize());
            record.setUrl(item.getUrl());

            workTable.putItem(record);

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public void deleteItem(String fileName) {
        FileAttributeItem item = getItemByName(fileName);

        HashMap<String, AttributeValue> keyToGet = new HashMap<>();
        keyToGet.put("id", AttributeValue.builder()
                .s(item.getId())
                .build());
        keyToGet.put("filename", AttributeValue.builder()
                .s(fileName)
                .build());

        DeleteItemRequest deleteReq = DeleteItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(keyToGet)
                .build();

        try {
            getClient().deleteItem(deleteReq);
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private String now() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormatNow);
        return sdf.format(cal.getTime());
    }

    private DynamoDbTable<FileAttribute> getTable() {
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(getClient())
                .build();

        return enhancedClient.table(TABLE_NAME, TableSchema.fromBean(FileAttribute.class));
    }
}
