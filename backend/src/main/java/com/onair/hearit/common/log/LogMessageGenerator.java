package com.onair.hearit.common.log;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

@Component
public class LogMessageGenerator {

    private static final DefaultPrettyPrinter DEFAULT_PRETTY_PRINTER = createPrettyPrinter();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public LogMessageGenerator() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private static DefaultPrettyPrinter createPrettyPrinter() {
        final DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
        printer.indentArraysWith(new DefaultIndenter("  ", System.lineSeparator()));
        return printer;
    }

    public String getLogMessage(Object object) {
        return "\n" + convertToPrettyJson(object);
    }

    private String convertToPrettyJson(Object object) {
        try {
            return objectMapper.writer(DEFAULT_PRETTY_PRINTER).writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("json 직렬화에 실패하였습니다.", e);
        }
    }
}
