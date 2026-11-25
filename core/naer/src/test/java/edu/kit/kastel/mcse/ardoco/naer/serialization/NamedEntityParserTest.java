/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.naer.serialization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntity;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;
import edu.kit.kastel.mcse.ardoco.naer.model.SoftwareArchitectureDocumentation;

class NamedEntityParserTest {
    private final SoftwareArchitectureDocumentation sad = new SoftwareArchitectureDocumentation("""
            The AuthenticationService handles login requests.
            It forwards valid credentials to the UserDatabase.
            The service logs each attempt.
            Clients call the LoginApi.""");

    @Test
    void parsesSectionsOccurrencesAndAlternativeNames() throws IOException {
        String output = """
                COMPONENT entities recognized:
                AuthenticationService, 'The AuthenticationService handles login requests.'
                AuthenticationService, 'It forwards valid credentials to the UserDatabase.'

                UserDatabase, 'It forwards valid credentials to the UserDatabase.'
                AuthenticationService, 'The service logs each attempt.'
                Alternative names:
                AuthenticationService: service, auth
                UserDatabase: None
                INTERFACE entities recognized:
                LoginApi, 'Clients call the LoginApi.'
                """;

        Map<String, NamedEntity> entities = NamedEntityParser.fromString(output, sad)
                .stream()
                .collect(Collectors.toMap(NamedEntity::getName, Function.identity()));

        assertThat(entities).containsOnlyKeys("AuthenticationService", "UserDatabase", "LoginApi");

        NamedEntity authenticationService = entities.get("AuthenticationService");
        assertThat(authenticationService.getType()).isEqualTo(NamedEntityType.COMPONENT);
        assertThat(authenticationService.getAlternativeNames()).containsExactly("auth", "service");
        assertThat(authenticationService.getOccurrenceLines()).containsExactly(1, 2, 3);

        NamedEntity userDatabase = entities.get("UserDatabase");
        assertThat(userDatabase.getType()).isEqualTo(NamedEntityType.COMPONENT);
        assertThat(userDatabase.getAlternativeNames()).isEmpty();
        assertThat(userDatabase.getOccurrenceLines()).containsExactly(2);

        NamedEntity loginApi = entities.get("LoginApi");
        assertThat(loginApi.getType()).isEqualTo(NamedEntityType.INTERFACE);
        assertThat(loginApi.getOccurrenceLines()).containsExactly(4);
    }

    @Test
    void rejectsEntriesBeforeSectionHeader() {
        assertThatThrownBy(() -> NamedEntityParser.fromString("UserDatabase, 'It forwards valid credentials to the UserDatabase.'", sad)).isInstanceOf(
                IOException.class).hasMessageContaining("Entity type not specified");
    }

    @Test
    void rejectsUnknownEntityType() {
        assertThatThrownBy(() -> NamedEntityParser.fromString("GADGET entities recognized:", sad)).isInstanceOf(IOException.class)
                .hasMessageContaining("Unknown entity type");
    }

    @Test
    void rejectsAlternativeNamesForUnknownEntity() {
        String output = """
                COMPONENT entities recognized:
                Alternative names:
                UserDatabase: db
                """;
        assertThatThrownBy(() -> NamedEntityParser.fromString(output, sad)).isInstanceOf(IOException.class).hasMessageContaining("unknown entity");
    }

    @Test
    void ignoresEmptyOutput() throws IOException {
        assertThat(NamedEntityParser.fromString("\n\n", sad)).isEqualTo(Set.of());
    }
}
