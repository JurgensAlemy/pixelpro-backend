package com.pixelpro.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI pixelproOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("pixelpro E-Commerce API")
                        .description("REST API for the pixelpro lingerie e-commerce platform")
                        .version("1.0.0"));
    }
}
