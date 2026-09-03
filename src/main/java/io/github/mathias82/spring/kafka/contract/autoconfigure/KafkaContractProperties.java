package io.github.mathias82.spring.kafka.contract.autoconfigure;

import io.github.mathias82.spring.kafka.contract.model.CompatibilityMode;
import io.github.mathias82.spring.kafka.contract.model.RegistryUnavailablePolicy;
import io.github.mathias82.spring.kafka.contract.model.SchemaSubject;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for startup Kafka schema contract validation.
 */
@ConfigurationProperties(prefix = "kafka.contract")
public class KafkaContractProperties {

    /**
     * Whether startup contract validation is enabled.
     */
    private boolean enabled = false;

    /**
     * Compatibility mode the application expects for configured subjects.
     */
    private CompatibilityMode compatibility = CompatibilityMode.BACKWARD;

    /**
     * Schema Registry connection settings used for startup validation.
     */
    private Registry registry = new Registry();

    /**
     * Retry policy for transient Schema Registry communication failures.
     */
    private Retry retry = new Retry();

    /**
     * Schema Registry subjects and local schemas that must be validated at startup.
     */
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

        /**
         * Base URL of the Confluent-compatible Schema Registry.
         */
        private String url;

        /**
         * HTTP connection timeout in milliseconds.
         */
        private int connectTimeoutMs = 2_000;

        /**
         * HTTP read timeout in milliseconds.
         */
        private int readTimeoutMs = 5_000;

        /**
         * Startup policy when Schema Registry remains temporarily unavailable after retries.
         * Contract violations and non-retryable HTTP failures always fail startup.
         */
        private RegistryUnavailablePolicy unavailablePolicy = RegistryUnavailablePolicy.FAIL;

        /**
         * Optional HTTP Basic authentication username or Confluent Cloud API key.
         */
        private String username;

        /**
         * Optional HTTP Basic authentication password or Confluent Cloud API secret.
         */
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

        public RegistryUnavailablePolicy getUnavailablePolicy() {
            return unavailablePolicy;
        }

        public void setUnavailablePolicy(RegistryUnavailablePolicy unavailablePolicy) {
            this.unavailablePolicy = unavailablePolicy == null ? RegistryUnavailablePolicy.FAIL : unavailablePolicy;
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

        /**
         * Maximum number of attempts for transient Schema Registry communication failures.
         * A value of 1 disables retries.
         */
        private int maxAttempts = 3;

        /**
         * Initial delay in milliseconds before the first retry.
         */
        private long initialBackoffMs = 500;

        /**
         * Exponential multiplier applied to the retry delay.
         */
        private double multiplier = 2.0;

        /**
         * Maximum retry delay in milliseconds.
         */
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
