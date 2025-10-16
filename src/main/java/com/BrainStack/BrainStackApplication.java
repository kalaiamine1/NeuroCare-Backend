package com.brainstack;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "NeuroCare API",
        version = "1.0",
        description = "API for NeuroCare appointment management"
    )
)
public class BrainStackApplication {
	public static void main(String[] args) {
		SpringApplication.run(BrainStackApplication.class, args);
	}
}
