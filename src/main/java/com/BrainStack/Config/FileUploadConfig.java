package com.BrainStack.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 🔹 Même dossier que là où tes fichiers sont réellement sauvegardés
        String uploadPath = "C:/Users/azizc/Desktop/NeuroCare-Backend/NeuroCare-Backend/uploads/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
        // ⚠️ ← ce "/" à la fin est essentiel
    }
}
