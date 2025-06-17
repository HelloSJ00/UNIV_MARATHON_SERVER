package com.runningRank.runningRank.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("러닝랭크 API")
                        .description("러닝랭크 백엔드 Swagger 문서입니다")
                        .version("v1.0.0"));
    }
}
