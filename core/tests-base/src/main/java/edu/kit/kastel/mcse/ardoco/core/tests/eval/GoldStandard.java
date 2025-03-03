/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ComponentModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;

public class GoldStandard {
    private final Logger logger = LoggerFactory.getLogger(GoldStandard.class);

    private final File goldStandard;
    private final ComponentModel model;

    private final MutableList<MutableList<ArchitectureItem>> sentence2instance = Lists.mutable.empty();

    public GoldStandard(File goldStandard, ComponentModel model) {
        this.goldStandard = goldStandard;
        this.model = model;
        this.load();
    }

    private void load() {
        try (Scanner scan = new Scanner(this.goldStandard, StandardCharsets.UTF_8)) {
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                if (line == null || line.isBlank() || line.startsWith("modelElement") || line.contains("modelElementID")) {
                    // continue if line is empty, null, or is the header (that starts with "modelElementID")
                    continue;
                }

                String[] idXline = line.strip().split(",", -1);
                ArchitectureItem instance = Lists.immutable.withAll(this.model.getContent()).select(i -> i.getId().equals(idXline[0])).getFirst();
                if (instance == null) {
                    System.err.println("No instance found for id \"" + idXline[0] + "\"");
                    continue;
                }
                int sentence = Integer.parseInt(idXline[1]);
                while (this.sentence2instance.size() <= sentence) {
                    this.sentence2instance.add(Lists.mutable.empty());
                }
                this.sentence2instance.get(sentence).add(instance);
            }
        } catch (IOException e) {
            this.logger.warn(e.getMessage(), e.getCause());
        }
    }

    public ImmutableList<ArchitectureItem> getModelInstances(int sentenceNo) {
        // Index starts at 1
        return this.sentence2instance.get(sentenceNo).toImmutable();
    }

    public ImmutableList<Integer> getSentencesWithElement(ArchitectureItem elem) {
        MutableList<Integer> sentences = Lists.mutable.empty();
        for (int i = 0; i < this.sentence2instance.size(); i++) {
            var instances = this.sentence2instance.get(i);
            if (instances.anySatisfy(e -> e.getId().equals(elem.getId()))) {
                sentences.add(i);
            }
        }
        return sentences.toImmutable();
    }
}
