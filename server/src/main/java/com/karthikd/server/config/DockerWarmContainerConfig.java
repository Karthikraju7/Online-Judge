package com.karthikd.server.config;

import com.karthikd.server.util.WarmContainerManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DockerWarmContainerConfig {

    @PostConstruct
    public void init() {
        try {
            log.info("Starting warm Docker container...");
            WarmContainerManager.initialize();
            log.info("Warm Docker container ready.");
        } catch (Exception e) {
            log.error("Failed to start warm container: {}", e.getMessage());
            throw new RuntimeException("Docker warm container init failed", e);
        }
    }

    @PreDestroy
    public void destroy() {
        log.info("Stopping warm Docker container...");
        WarmContainerManager.shutdown();
    }
}