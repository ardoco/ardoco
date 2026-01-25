package io.github.ardoco.core.neo4jschema;

import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import jakarta.annotation.PostConstruct; // or javax.annotation.PostConstruct
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This bean automatically registers the Neo4j implementation with the
 * static PersistenceBridge when the Spring Context starts.
 */
@Component
public class Neo4jBridgeActivator {
    private static final Logger logger = LoggerFactory.getLogger(Neo4jBridgeActivator.class);

    private final Neo4jPersistenceHandler handler;

    public Neo4jBridgeActivator(Neo4jPersistenceHandler handler) {
        this.handler = handler;
    }

    @PostConstruct
    public void activate() {
        // Plug the implementation into the static bridge so the persitance handler can be used by other components.
        PersistenceBridge.setHandler(this.handler);
        logger.info("Neo4j Persistence has been plugged into the ArDoCo Core Framework.");
    }
}
