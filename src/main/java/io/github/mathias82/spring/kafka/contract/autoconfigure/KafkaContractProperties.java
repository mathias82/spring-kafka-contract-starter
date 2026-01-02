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

    public List<SchemaSubject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<SchemaSubject> subjects) {
        this.subjects = subjects;
    }

    public static class Registry {
        private RegistryType type;
        private String url;

        public RegistryType getType() {
            return type;
        }

        public void setType(RegistryType type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public enum RegistryType {
        CONFLUENT,
        APICURIO
    }
}
