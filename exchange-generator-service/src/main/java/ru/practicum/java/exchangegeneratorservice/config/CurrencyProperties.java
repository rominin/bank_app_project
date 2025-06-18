package ru.practicum.java.exchangegeneratorservice.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "currency")
public class CurrencyProperties {
    private List<String> pairs;
    private Map<String, Range> range;

    @Data
    @AllArgsConstructor
    public static class Range {
        private double min;
        private double max;
    }
}
