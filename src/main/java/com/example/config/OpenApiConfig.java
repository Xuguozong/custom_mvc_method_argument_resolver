package com.example.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class OpenApiConfig {

    @Bean
    @Profile("!prod")
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info().title("自定义参数转换器示例工程")
                        .description("自定义参数转换器示例工程")
                        .version("v1"))
                .externalDocs(new ExternalDocumentation()
                        .description("springdoc官方文档")
                        .url("https://springdoc.org/#migrating-from-springfox"));
    }


}
