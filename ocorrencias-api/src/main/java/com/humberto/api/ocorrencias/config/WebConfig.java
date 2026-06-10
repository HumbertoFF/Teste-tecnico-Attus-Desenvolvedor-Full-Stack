package com.humberto.api.ocorrencias.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:4200,http://localhost:80,http://localhost}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // cache do preflight por 1h
    }
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.stream()
          .filter(c -> c instanceof MappingJackson2HttpMessageConverter)
          .map(c -> (MappingJackson2HttpMessageConverter) c)
          .findFirst()
          .ifPresent(converter -> {
            List<MediaType> mediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
            mediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
            converter.setSupportedMediaTypes(mediaTypes);
          });
   }
}
