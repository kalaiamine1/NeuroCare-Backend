package com.BrainStack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.BrainStack", "com.example.healthai"})
public class BrainStackApplication {

	public static void main(String[] args) {
		SpringApplication.run(BrainStackApplication.class, args);
	}

}
