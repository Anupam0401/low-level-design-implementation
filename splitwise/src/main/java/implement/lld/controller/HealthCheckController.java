package implement.lld.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for health check endpoints
 */
@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/api/health")
public class HealthCheckController implements HealthIndicator {

    /**
     * Simple health check endpoint that returns a 200 OK response
     * @return 200 OK response
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        log.info("Health check ping received");
        return ResponseEntity.ok("pong");
    }

    /**
     * Detailed health check endpoint that returns application status information
     * @return application status information
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        log.info("Health check status requested");
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now().toString());
        status.put("service", "Splitwise Application");
        status.put("version", "1.0.0");
        
        // Add memory information
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("total", runtime.totalMemory());
        memory.put("free", runtime.freeMemory());
        memory.put("used", runtime.totalMemory() - runtime.freeMemory());
        memory.put("max", runtime.maxMemory());
        status.put("memory", memory);
        
        return ResponseEntity.ok(status);
    }

    /**
     * Implementation of the HealthIndicator interface for Spring Boot Actuator
     * @return Health object indicating the application's health
     */
    @Override
    public Health health() {
        try {
            return Health.up()
                    .withDetail("service", "Splitwise Application")
                    .withDetail("version", "1.0.0")
                    .withDetail("timestamp", LocalDateTime.now().toString())
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
