package com.example.demo.global.converter;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Collections;
import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {1

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {

        try {
            if (attribute == null || attribute.isEmpty()) {
                return "[]";
            }
            return objectMapper.writeValueAsString(attribute);

        } catch (Exception e) {
            throw new CustomException(ErrorStatus.COMMON_JSON_CONVERT_ERROR);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {

        try {
            if (dbData == null || dbData.trim().isEmpty()) {
                return Collections.emptyList();
            }
            return objectMapper.readValue(dbData, new TypeReference<List<String>>() {});

        } catch (Exception e) {
            throw new CustomException(ErrorStatus.COMMON_JSON_CONVERT_ERROR);
        }
    }

}