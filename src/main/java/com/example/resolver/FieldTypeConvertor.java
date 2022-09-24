package com.example.resolver;

import com.alibaba.fastjson2.JSONArray;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface FieldTypeConvertor {

    Object convert(String[] values, int index);

    Object convertFirst(String[] values);

    Object convertJsonObject(Object o, int index, String key);

    enum FieldType implements FieldTypeConvertor {
        STRING_LIST("java.util.List<java.lang.String>"),
        INTEGER("java.lang.Integer"),
        BOOLEAN("java.lang.Boolean"),
        STRING("java.lang.String");

        private String typeName;

        FieldType(String typeName) {
            this.typeName = typeName;
        }

        public String getTypeName() {
            return typeName;
        }

        @Override
        public Object convert(String[] values, int index) {
            return switch (this) {
                case STRING_LIST -> List.of(values);
                case BOOLEAN -> Boolean.valueOf(values[index]);
                case INTEGER -> Integer.valueOf(values[index]);
                case STRING -> (index < values.length) && StringUtils.isNotBlank(values[index]) ? values[index] : null;
            };
        }

        @Override
        public Object convertFirst(String[] values) {
            return switch (this) {
                case STRING_LIST -> List.of(values);
                case BOOLEAN -> Boolean.valueOf(values[0]);
                case INTEGER -> Integer.valueOf(values[0]);
                case STRING -> values[0];
            };
        }

        @Override
        public Object convertJsonObject(Object o, int index, String key) {
            List<String> values = new ArrayList<>();
            if (o instanceof JSONArray) {
                JSONArray array = (JSONArray) o;
                values = array.stream().map(Object::toString).toList();
            }  else {
                values.add(o.toString());
            }
            String v = values.get(index);
            return switch (this) {
                case STRING_LIST -> values;
                case BOOLEAN -> Boolean.valueOf(v);
                case INTEGER -> Integer.valueOf(v);
                case STRING -> (index < values.size()) && StringUtils.isNotBlank(v) ? v : null;
            };
        }

        public static FieldType of(String typeName) {
            return Arrays.stream(FieldType.values())
                    .filter(f -> f.getTypeName().equals(typeName))
                    .findAny()
                    .orElseThrow(() -> new UnsupportedOperationException("暂不支持的类型:" + typeName));
        }
    }
}


