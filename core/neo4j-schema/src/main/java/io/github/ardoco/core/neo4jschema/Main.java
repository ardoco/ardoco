package io.github.ardoco.core.neo4jschema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication
//@EntityScan("io.github.ardoco.core.entities")
@EnableNeo4jRepositories("io.github.ardoco.core.neo4jschema.repository")
//@ComponentScan(basePackages = {
//        "io.github.ardoco.core.neo4jschema.service",
//        "io.github.ardoco.core.neo4jschema.adapter"
//})
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
