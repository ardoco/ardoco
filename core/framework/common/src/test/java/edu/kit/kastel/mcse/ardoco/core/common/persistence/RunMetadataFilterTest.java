/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.common.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.factory.SortedMaps;
import org.junit.jupiter.api.Test;

class RunMetadataFilterTest {

    @Test
    void dropsSecretLikeKeys() {
        assertThat(RunMetadataFilter.isSecretLikeKey("PersistenceBridge::neo4jPassword")).isTrue();
        assertThat(RunMetadataFilter.isSecretLikeKey("apiKey")).isTrue();
        assertThat(RunMetadataFilter.isSecretLikeKey("X-TOKEN")).isTrue();
    }

    @Test
    void keepsNonSecretKeys() {
        assertThat(RunMetadataFilter.isSecretLikeKey("PersistenceBridge::storage")).isFalse();
    }

    @Test
    void masksUserInfoInBoltUri() {
        assertThat(RunMetadataFilter.maskUserInfo("bolt://user:pass@127.0.0.1:7687")).isEqualTo("bolt://***@127.0.0.1:7687");
    }

    @Test
    void masksUserInfoInNeo4jPlusSUri() {
        assertThat(RunMetadataFilter.maskUserInfo("neo4j+s://user:pass@host")).isEqualTo("neo4j+s://***@host");
    }

    @Test
    void leavesUriWithoutUserInfoUnchanged() {
        assertThat(RunMetadataFilter.maskUserInfo("bolt://127.0.0.1:7687")).isEqualTo("bolt://127.0.0.1:7687");
    }

    @Test
    void leavesPlainStringWithAtSignUnchanged() {
        assertThat(RunMetadataFilter.maskUserInfo("foo@bar")).isEqualTo("foo@bar");
    }

    @Test
    void filterSecretsDropsAndMasksAndReportsDroppedKeys() {
        var configuration = SortedMaps.immutable.of("PersistenceBridge::storage", "neo4j", "PersistenceBridge::neo4jPassword", "s3cr3t-run-metadata-test",
                "PersistenceBridge::neo4jUri", "bolt://user:s3cr3t-run-metadata-test@127.0.0.1:7687");

        List<String> droppedKeys = new ArrayList<>();
        var filtered = RunMetadataFilter.filterSecrets(configuration, droppedKeys);

        assertThat(filtered.get("PersistenceBridge::storage")).isEqualTo("neo4j");
        assertThat(filtered.get("PersistenceBridge::neo4jUri")).isEqualTo("bolt://***@127.0.0.1:7687");
        assertThat(filtered.containsKey("PersistenceBridge::neo4jPassword")).isFalse();
        assertThat(filtered.toString()).doesNotContain("s3cr3t-run-metadata-test");
        assertThat(droppedKeys).containsExactly("PersistenceBridge::neo4jPassword");
    }
}
