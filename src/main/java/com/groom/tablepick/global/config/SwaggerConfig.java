package com.groom.tablepick.global.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TablePick API 문서")
                        .version("v1.0.0")
                        .description("테이블 예약, 식당 관리, 결제 등 API 명세서입니다."));
    }
}
