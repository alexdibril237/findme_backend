package com.geolink.findme.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticFilesConfig implements WebMvcConfigurer {

    private final String storageRoot;

    public StaticFilesConfig(@Value("${storage.root}") String storageRoot) {
        this.storageRoot = storageRoot;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = storageRoot.endsWith("/") ? storageRoot : storageRoot + "/";
        registry.addResourceHandler("/files/**").addResourceLocations("file:" + location);
    }
}
