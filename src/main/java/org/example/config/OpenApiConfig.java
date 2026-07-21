package org.example.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.media.MediaType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Currency Converter API")
                        .version("1.0")
                        .description("API для получения официальных курсов валют НБРБ")
                        .contact(new Contact().name("Ваше имя").email("your@email.com")))
                .components(new Components()
                        .addResponses("NotFound", new io.swagger.v3.oas.models.responses.ApiResponse()
                                .description("Валюта не найдена или курс на эту дату отсутствует")
                                .content(new io.swagger.v3.oas.models.media.Content()
                                        .addMediaType("application/json", new MediaType()
                                                .schema(new io.swagger.v3.oas.models.media.Schema<>()
                                                        .$ref("#/components/schemas/ProblemDetail")))))
                        .addResponses("BadRequestError", new io.swagger.v3.oas.models.responses.ApiResponse()
                                .description("Некорректный формат кода валюты или даты")
                                .content(new io.swagger.v3.oas.models.media.Content()
                                        .addMediaType("application/json", new MediaType()
                                                .schema(new io.swagger.v3.oas.models.media.Schema<>()
                                                        .$ref("#/components/schemas/ProblemDetail"))))));
    }
}
