package com.onair.hearit.common.log.mask;

import com.onair.hearit.common.log.mask.strategy.MaskingStrategy;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MaskingConfig {

    @Bean
    public Map<MaskingType, MaskingStrategy> maskingStrategyMap(List<MaskingStrategy> strategies) {
        return strategies.stream()
                .collect(Collectors.toUnmodifiableMap(MaskingStrategy::type, Function.identity()));
    }
}
