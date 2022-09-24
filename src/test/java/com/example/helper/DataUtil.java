package com.example.helper;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public final class DataUtil {

    private DataUtil(){}

    /**
     * 添加 json 文档数据到 mongo 中
     * @param filePath json 数据文件地址
     * @param mongoTemplate mongoTemplate
     * @param collection 数据要插入的集合名
     */
    public static void insertJsonDataToMongo(String filePath, MongoTemplate mongoTemplate,
                                                     String collection) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        List<Document> documents = lines.stream().map(Document::parse).toList();
        mongoTemplate.insert(documents, collection);
    }

    /**
     * 添加 json 文档数据到 mongo 中
     * @param filePath json 数据文件地址
     * @param mongoTemplate mongoTemplate
     * @param entityClass @Document 标记的 mongo collection 实体类
     */
    public static <T> void insertJsonDataToMongo(String filePath, MongoTemplate mongoTemplate,
                                             Class<T> entityClass) throws IOException {
        checkHasDocumentAnnotation(entityClass);
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        List<Document> documents = lines.stream().map(Document::parse).toList();
        mongoTemplate.insert(documents, entityClass);
    }

    private static <T> void checkHasDocumentAnnotation(Class<T> entityClass) {
        org.springframework.data.mongodb.core.mapping.Document document =
                entityClass.getAnnotation(org.springframework.data.mongodb.core.mapping.Document.class);
        if (Objects.isNull(document)) throw new IllegalArgumentException("请传入 @Document 注解标注的实体类");
    }
}
