package io.github.mathias82.spring.kafka.contract.model;

import org.springframework.core.io.Resource;

public class SchemaSubject {

    private String name;
    private Resource schemaFile;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Resource getSchemaFile() {
        return schemaFile;
    }

    public void setSchemaFile(Resource schemaFile) {
        this.schemaFile = schemaFile;
    }
}
