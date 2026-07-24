package com.asset.itassetsystem.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 灵活 LocalDate 反序列化器：支持 yyyy-MM-dd 和 yyyy/M/d 格式
 */
public class FlexibleLocalDateDeserializer extends JsonDeserializer<LocalDate> {
    private static final DateTimeFormatter[] FORMATTERS = {
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("yyyy/M/d"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
        DateTimeFormatter.ofPattern("yyyy-M-d")
    };

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        if (text == null || text.isEmpty()) return null;
        // 去除空白
        text = text.trim();
        for (DateTimeFormatter fmt : FORMATTERS) {
            try { return LocalDate.parse(text, fmt); } catch (DateTimeParseException ignored) {}
        }
        // 兼容 Excel 序列号（数字日期）
        try {
            double serial = Double.parseDouble(text);
            return LocalDate.of(1899, 12, 30).plusDays((long) serial);
        } catch (NumberFormatException ignored) {}
        throw new IOException("无法解析日期: " + text);
    }
}
