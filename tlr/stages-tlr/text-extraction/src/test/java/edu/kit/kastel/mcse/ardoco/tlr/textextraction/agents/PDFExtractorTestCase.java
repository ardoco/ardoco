/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.textextraction.agents;

import static edu.kit.kastel.mcse.ardoco.core.common.JsonHandling.createObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

/**
 * Not a test, but it extracts the data from the downloaded pdfs.
 */
@EnabledIfEnvironmentVariable(matches = "true", named = "ExtractPDF")
@Deterministic
class PDFExtractorTestCase {

    @Test
    void extractStandardGlossary() throws IOException {
        extract("./src/main/resources/pdfs/Standard_glossary_of_terms_used_in_Software_Engineering_1.0.pdf",
                "./src/main/resources/pdfs/Standard_glossary_of_terms_used_in_Software_Engineering_1.0.pdf.words.txt", this::processTextStandardGlossary);

    }

    private void processTextStandardGlossary(String text, Set<String> foundWords) {
        String[] lines = text.lines().toArray(String[]::new);
        boolean started = false;
        Predicate<String> hasStarted = l -> l != null && l.trim().matches("6\\.\\s+Definitions");
        Predicate<String> end = l -> l.contains("[Fenton] N. Fenton (1991)");

        for (String line : lines) {
            if (hasStarted.test(line)) {
                started = true;
                continue;
            }
            if (!started)
                continue;

            if (line == null || !line.contains(":"))
                continue;

            if (end.test(line)) {
                System.out.println("Ending at: " + line);
                break;
            }

            String[] definitions = line.split(":", 2);
            if (definitions.length == 2 && (definitions[1].isBlank() || definitions[0].contains("."))) {
                System.out.println("Skipping: " + line);
                continue;
            }

            // TODO Synonyms via "see XY"
            foundWords.add(definitions[0].trim());

        }
    }

    @Test
    void extractISO24765() throws IOException {
        extract("./src/main/resources/pdfs/24765-2017.pdf", "./src/main/resources/pdfs/24765-2017.pdf.words.txt", this::processTextISO24765);
    }

    private void processTextISO24765(String text, MutableSortedSet<String> foundWords) {
        String[] lines = text.lines().toArray(String[]::new);
        boolean lastLineWasIdentifier = false;
        Predicate<String> isIdentifier = l -> l != null && l.trim().matches("^3\\.[0-9]+$");
        for (String line : lines) {
            if (lastLineWasIdentifier) {
                // TODO Maybe skip elements with numbers? e.g., 1GL
                foundWords.add(line.trim());
                lastLineWasIdentifier = false;
                continue;
            }
            if (isIdentifier.test(line)) {
                lastLineWasIdentifier = true;
            }
        }
    }

    private void extract(String in, String out, BiConsumer<String, MutableSortedSet<String>> processor) throws IOException {
        var file = new File(in);
        var pdf = Loader.loadPDF(file);
        MutableSortedSet<String> foundWords = SortedSets.mutable.empty();
        PDFTextStripper pts = new PDFTextStripper();
        var text = pts.getText(pdf);
        processor.accept(text, foundWords);

        /*
         * for (int i = 1; i <= maxPages; i++) { PDFTextStripper pts = new PDFTextStripper(); pts.setStartPage(i);
         * pts.setEndPage(i); var text = pts.getText(pdf); processor.accept(text, foundWords); }
         */

        createObjectMapper().writeValue(new File(out), foundWords.stream().toList());
    }
}
