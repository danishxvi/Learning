package com.danish.spring.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Customizes the document-level metadata - title, version, description - that appears
// at the top of the generated spec and the Swagger UI page. Without this bean, springdoc
// still generates a complete spec; it just uses generic defaults for these fields.
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI bookApiInfo() {
        return new OpenAPI().info(new Info()
                .title("Book Catalog API")
                .version("1.0.0")
                .description("Lesson 28 of the Spring stack - a minimal API documented with OpenAPI."));
    }
}
