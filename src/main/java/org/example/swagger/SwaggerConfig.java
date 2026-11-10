package org.example.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Mini Project BNI API",
                version = "1.0",
                description = "Dokumentasi REST API Mini Project BNI"
        )
)
public class SwaggerConfig {

}
