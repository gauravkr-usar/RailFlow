package com.railflow.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

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
        // 1. Sanitize host: remove angle brackets, quotes, and whitespace
        String cleanHost = (redisHost != null) ? redisHost.replaceAll("[<>\"']", "").trim() : "localhost";
        if (cleanHost.isEmpty()) {
            cleanHost = "localhost";
        }

        // 2. Detect template placeholder strings like 'upstash-endpoint', '<...>'
        if (isPlaceholderHost(cleanHost)) {
            log.warn("Redis host '{}' is a template placeholder. Starting in-memory fallback lock engine.", cleanHost);
            return createInMemoryFallbackClient();
        }

        try {
            Config config = new Config();
            boolean isSsl = sslEnabled || cleanHost.contains("upstash.io") || cleanHost.startsWith("rediss://");
            String prefix = isSsl ? "rediss://" : "redis://";

            if (cleanHost.startsWith("redis://")) {
                cleanHost = cleanHost.substring("redis://".length());
                prefix = "redis://";
            } else if (cleanHost.startsWith("rediss://")) {
                cleanHost = cleanHost.substring("rediss://".length());
                prefix = "rediss://";
            }

            int port = redisPort > 0 ? redisPort : 6379;
            if (cleanHost.contains(":")) {
                String[] parts = cleanHost.split(":", 2);
                cleanHost = parts[0];
                try {
                    port = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ignored) {}
            }

            String address = prefix + cleanHost + ":" + port;
            log.info("Connecting Redisson client to: {}", address);

            SingleServerConfig serverConfig = config.useSingleServer()
                    .setAddress(address)
                    .setConnectTimeout(8000)
                    .setTimeout(4000)
                    .setRetryAttempts(2)
                    .setRetryInterval(1000);

            if (redisPassword != null && !redisPassword.isBlank()) {
                String cleanPassword = redisPassword.replaceAll("[<>\"']", "").trim();
                if (!cleanPassword.isEmpty()) {
                    serverConfig.setPassword(cleanPassword);
                }
            }

            return Redisson.create(config);
        } catch (Exception ex) {
            log.warn("Could not connect to Redis at '{}': {}. Falling back to in-memory lock engine.", cleanHost, ex.getMessage());
            return createInMemoryFallbackClient();
        }
    }

    private boolean isPlaceholderHost(String host) {
        String lower = host.toLowerCase();
        return lower.equals("upstash-endpoint")
                || lower.equals("upstash-host")
                || lower.equals("your-host")
                || lower.contains("placeholder")
                || lower.contains("endpoint")
                || lower.contains("<");
    }

    private RedissonClient createInMemoryFallbackClient() {
        ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

        return (RedissonClient) Proxy.newProxyInstance(
                RedissonClient.class.getClassLoader(),
                new Class<?>[]{RedissonClient.class},
                (proxy, method, args) -> {
                    if ("getLock".equals(method.getName()) && args != null && args.length == 1) {
                        String lockName = (String) args[0];
                        ReentrantLock jvmLock = lockMap.computeIfAbsent(lockName, k -> new ReentrantLock(true));
                        return createRLockProxy(jvmLock);
                    }
                    if ("shutdown".equals(method.getName())) {
                        lockMap.clear();
                        return null;
                    }
                    if ("isShutdown".equals(method.getName()) || "isShuttingDown".equals(method.getName())) {
                        return false;
                    }
                    return null;
                }
        );
    }

    private RLock createRLockProxy(ReentrantLock jvmLock) {
        return (RLock) Proxy.newProxyInstance(
                RLock.class.getClassLoader(),
                new Class<?>[]{RLock.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("tryLock".equals(name)) {
                        if (args.length == 0) {
                            return jvmLock.tryLock();
                        } else if (args.length >= 2) {
                            long waitTime = ((Number) args[0]).longValue();
                            TimeUnit unit = (TimeUnit) args[args.length - 1];
                            return jvmLock.tryLock(waitTime, unit);
                        }
                    }
                    if ("isHeldByCurrentThread".equals(name)) {
                        return jvmLock.isHeldByCurrentThread();
                    }
                    if ("unlock".equals(name)) {
                        if (jvmLock.isHeldByCurrentThread()) {
                            jvmLock.unlock();
                        }
                        return null;
                    }
                    if ("lock".equals(name)) {
                        jvmLock.lock();
                        return null;
                    }
                    if ("isLocked".equals(name)) {
                        return jvmLock.isLocked();
                    }
                    return null;
                }
        );
    }
}
