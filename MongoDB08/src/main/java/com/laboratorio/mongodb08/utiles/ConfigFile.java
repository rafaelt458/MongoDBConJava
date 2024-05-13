package com.laboratorio.mongodb08.utiles;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigFile {
    private final String filename;

    public ConfigFile(String filename) {
        this.filename = filename;
    }
    
    public Properties readPropertiesFile() throws IOException {
        Properties properties = null;
        
        try (InputStream inputStream = ConfigFile.class.getClassLoader().getResourceAsStream(this.filename)) {
            properties = new Properties();
            properties.load(inputStream);
        }
        
        return properties;
    }
}