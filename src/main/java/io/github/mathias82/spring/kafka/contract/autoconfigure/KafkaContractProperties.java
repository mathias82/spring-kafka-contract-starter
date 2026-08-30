package io.github.mathias82.spring.kafka.contract.autoconfigure;

import io.github.mathias82.spring.kafka.contract.model.CompatibilityMode;
import io.github.mathias82.spring.kafka.contract.model.SchemaSubject;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "kafka.contract")
public class KafkaContractProperties {

    private boolean enabled = false;
    private CompatibilityMode compatibility = CompatibilityMode.BACKWARD;
    private Registry registry = new Registry();
    private Retry retry = new Retry();
    private List<SchemaSubject> subjects = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public CompatibilityMode getCompatibility() {
        return compatibility;
    }

    public void setCompatibility(CompatibilityMode compatibility) {
        this.compatibility = compatibility;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public Retry getRetry() {
        return retry;
    }

    public void setRetry(Retry retry) {
        this.retry = retry;
    }

    public List<SchemaSubject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<SchemaSubject> subjects) {
        this.subjects = subjects;
    }

    public static class Registry {
        private String url;
        private int connectTimeoutMs = 2_000;
        private int readTimeoutMs = 5_000;
        private String username;
        private String password;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public int getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class Retry {
        private int maxAttempts = 3;
        private long initialBackoffMs = 500;
        private double multiplier = 2.0;
        private long maxBackoffMs = 5_000;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = Math.max(1, maxAttempts);
        }

        public long getInitialBackoffMs() {
            return initialBackoffMs;
        }

        public void setInitialBackoffMs(long initialBackoffMs) {
            this.initialBackoffMs = Math.max(0, initialBackoffMs);
        }

        public double getMultiplier() {
            return multiplier;
        }

        public void setMultiplier(double multiplier) {
            this.multiplier = multiplier < 1.0 ? 1.0 : multiplier;
        }

        public long getMaxBackoffMs() {
            return maxBackoffMs;
        }

        public void setMaxBackoffMs(long maxBackoffMs) {
            this.maxBackoffMs = Math.max(0, maxBackoffMs);
        }
    }
}
