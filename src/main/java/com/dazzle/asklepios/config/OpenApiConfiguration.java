package com.dazzle.asklepios.config;

import com.dazzle.asklepios.config.openapi.CustomOpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile(Constants.SPRING_PROFILE_API_DOCS)
public class OpenApiConfiguration {

    public static final String API_PACKAGE = "com.dazzle.asklepios";

    @Bean
    @ConditionalOnMissingBean(name = "setupGroupedOpenAPI")
    public GroupedOpenApi setupGroupedOpenAPI(
        CustomOpenApiCustomizer customOpenApiCustomizer
    ) {
        return GroupedOpenApi.builder()
            .group("setup")
            .addOpenApiCustomizer(customOpenApiCustomizer)
            .packagesToScan(API_PACKAGE)
            .pathsToMatch("/api/**")
            .build();
    }
}
