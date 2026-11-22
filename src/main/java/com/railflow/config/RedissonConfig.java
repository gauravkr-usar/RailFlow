package com.railflow.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.ssl.enabled:false}")
    private boolean sslEnabled;

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient() {
        Config config = new Config();

        // 1. Sanitize host: remove angle brackets, quotes, and whitespace
        String cleanHost = (redisHost != null) ? redisHost.replaceAll("[<>\"']", "").trim() : "localhost";
        if (cleanHost.isEmpty()) {
            cleanHost = "localhost";
        }

        // 2. Handle protocol and SSL
        boolean isSsl = sslEnabled || cleanHost.contains("upstash.io") || cleanHost.startsWith("rediss://");
        String prefix = isSsl ? "rediss://" : "redis://";

        if (cleanHost.startsWith("redis://")) {
            cleanHost = cleanHost.substring("redis://".length());
            prefix = "redis://";
        } else if (cleanHost.startsWith("rediss://")) {
            cleanHost = cleanHost.substring("rediss://".length());
            prefix = "rediss://";
        }

        // Strip port if already part of cleanHost (e.g. host:6379)
        int port = redisPort > 0 ? redisPort : 6379;
        if (cleanHost.contains(":")) {
            String[] parts = cleanHost.split(":", 2);
            cleanHost = parts[0];
            try {
                port = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {}
        }

        String address = prefix + cleanHost + ":" + port;
        log.info("Configuring Redisson client with address: {}", address);

        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress(address)
                .setConnectTimeout(10000)
                .setTimeout(5000)
                .setRetryAttempts(3)
                .setRetryInterval(1500);

        // 3. Sanitize and set password if present
        if (redisPassword != null && !redisPassword.isBlank()) {
            String cleanPassword = redisPassword.replaceAll("[<>\"']", "").trim();
            if (!cleanPassword.isEmpty()) {
                serverConfig.setPassword(cleanPassword);
            }
        }

        return Redisson.create(config);
    }
}
