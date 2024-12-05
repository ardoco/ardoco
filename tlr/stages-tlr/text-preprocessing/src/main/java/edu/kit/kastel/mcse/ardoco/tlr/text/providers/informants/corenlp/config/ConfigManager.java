/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.text.providers.informants.corenlp.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Singleton manages access to the config file.
 */
public final class ConfigManager {

    public static ConfigManager INSTANCE = new ConfigManager();

    private final Logger logger = LoggerFactory.getLogger(ConfigManager.class);

    private final Properties properties;
    private static final String FILE_PATH = "config.properties";
    private static final String PROPERTY_MICROSERVICE_URL = "microserviceUrl";
    private static final String PROPERTY_NLP_PROVIDER_SOURCE = "nlpProviderSource";
    private static final String PROPERTY_CORENLP_SERVICE = "corenlpService";
    private static final String PROPERTY_HEALTH_SERVICE = "healthService";

    private ConfigManager() {
        properties = new Properties();
        try (InputStream fileInputStream = ConfigManager.class.getClassLoader().getResourceAsStream(FILE_PATH)) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            logger.warn("Could not load config file. ", e);
            properties.setProperty(PROPERTY_MICROSERVICE_URL, "http://localhost:8080");
            properties.setProperty(PROPERTY_NLP_PROVIDER_SOURCE, "local");
            properties.setProperty(PROPERTY_CORENLP_SERVICE, "/stanfordnlp");
            properties.setProperty(PROPERTY_HEALTH_SERVICE, "/stanfordnlp/health");
        }
        if (System.getenv("MICROSERVICE_URL") != null) {
            properties.setProperty(PROPERTY_MICROSERVICE_URL, System.getenv("MICROSERVICE_URL"));
        }
        if (System.getenv("NLP_PROVIDER_SOURCE") != null) {
            properties.setProperty(PROPERTY_NLP_PROVIDER_SOURCE, System.getenv("NLP_PROVIDER_SOURCE"));
        }
    }

    public String getMicroserviceUrl() {
        return properties.getProperty(PROPERTY_MICROSERVICE_URL);
    }

    public String getNlpProviderSource() {
        return properties.getProperty(PROPERTY_NLP_PROVIDER_SOURCE);
    }

    public String getCorenlpService() {
        return properties.getProperty(PROPERTY_CORENLP_SERVICE);
    }

    public String getHealthService() {
        return properties.getProperty(PROPERTY_HEALTH_SERVICE);
    }

}
