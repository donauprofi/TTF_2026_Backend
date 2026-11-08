package at.salzburg.hackathon.hackathontourism03.presentation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Value("${app.cors-origins}")
    private String[] origins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOriginPatterns(origins).allowedMethods("GET", "POST", "PUT", "OPTIONS");
    }
}
