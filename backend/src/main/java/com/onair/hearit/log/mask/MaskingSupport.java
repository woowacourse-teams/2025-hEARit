package com.onair.hearit.log.mask;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.onair.hearit.log.mask.strategy.MaskingStrategy;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Iterator;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaskingSupport {

    private final ObjectMapper objectMapper;
    private final Map<MaskingType, MaskingStrategy> maskingStrategyMap;

    public Object mask(Object obj) {
        try {
            if (obj instanceof ResponseEntity<?> responseEntity) {
                Object maskedBody = mask(responseEntity.getBody());
                return ResponseEntity.status(responseEntity.getStatusCode())
                        .headers(responseEntity.getHeaders())
                        .body(maskedBody);
            }

            JsonNode rootNode = objectMapper.valueToTree(obj);
            applyMaskingWithAnnotations(obj, rootNode);
            return rootNode;
        } catch (Exception e) {
            return "[마스킹 실패: " + e.getMessage() + "]";
        }
    }

    private void applyMaskingWithAnnotations(Object obj, JsonNode node)
            throws IllegalAccessException, InvocationTargetException {
        if (obj == null || node == null) {
            return;
        }

        if (node.isObject()) {
            applyMaskingToObjectNode(obj, (ObjectNode) node);
        } else if (node.isArray() && obj instanceof Iterable<?>) {
            applyMaskingToArrayNode(obj, node);
        }
    }

    private void applyMaskingToObjectNode(Object obj, ObjectNode objectNode)
            throws IllegalAccessException, InvocationTargetException {
        Class<?> clazz = obj.getClass();

        // Record와 일반 클래스를 하나의 로직으로 처리하기 위해 if-else 분기 필요
        if (clazz.isRecord()) {
            for (RecordComponent comp : clazz.getRecordComponents()) {
                Masking masking = findMaskingAnnotation(clazz, comp);
                String name = comp.getName();
                Object value = comp.getAccessor().invoke(obj);
                JsonNode childNode = objectNode.get(name);

                applyMaskingToField(masking, objectNode, name, value, childNode);
            }
        } else {
            for (Field field : clazz.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Masking masking = field.getAnnotation(Masking.class);
                    String name = field.getName();
                    Object value = field.get(obj);
                    JsonNode childNode = objectNode.get(name);

                    applyMaskingToField(masking, objectNode, name, value, childNode);
                } catch (InaccessibleObjectException e) {
                    // 접근 불가 필드는 무시
                }
            }
        }
    }

    private void applyMaskingToArrayNode(Object obj, JsonNode node)
            throws IllegalAccessException, InvocationTargetException {
        Iterator<?> it = ((Iterable<?>) obj).iterator();
        int index = 0;
        while (it.hasNext() && index < node.size()) {
            Object item = it.next();
            JsonNode itemNode = node.get(index++);
            applyMaskingWithAnnotations(item, itemNode);
        }
    }

    private void applyMaskingToField(Masking masking, ObjectNode objectNode, String name, Object value,
                                     JsonNode childNode)
            throws IllegalAccessException, InvocationTargetException {
        if (childNode == null) {
            return;
        }

        if (masking != null && childNode.isTextual()) {
            MaskingStrategy strategy = maskingStrategyMap.get(masking.type());
            if (strategy != null) {
                String mask = strategy.mask(childNode.asText());
                objectNode.put(name, mask);
                return; // 마스킹이 적용되었으면 재귀 호출하지 않음
            }
        }

        if (value != null && (childNode.isObject() || childNode.isArray())) {
            applyMaskingWithAnnotations(value, childNode);
        }
    }

    private Masking findMaskingAnnotation(Class<?> clazz, RecordComponent comp) {
        Masking masking = comp.getAnnotation(Masking.class);
        if (masking != null) {
            return masking;
        }

        try {
            Field field = clazz.getDeclaredField(comp.getName());
            return field.getAnnotation(Masking.class);
        } catch (NoSuchFieldException ignored) {
            // RecordComponent와 동일한 이름의 필드는 항상 존재하므로,
            // 이 예외는 실질적으로 발생하지 않습니다.
        }
        return null;
    }
}
