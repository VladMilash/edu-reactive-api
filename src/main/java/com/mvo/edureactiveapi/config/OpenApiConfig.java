package com.mvo.edureactiveapi.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
    info = @Info(
        title = "edu reactive API",
        version = "1.0.0",
        contact = @Contact(
            name = "Milash Vlad"
        )
    )
)
public class OpenApiConfig {
}
