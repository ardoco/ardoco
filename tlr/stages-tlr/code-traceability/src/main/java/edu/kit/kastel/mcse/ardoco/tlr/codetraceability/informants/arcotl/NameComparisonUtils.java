/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.entity.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeModule;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodePackage;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import opennlp.tools.stemmer.PorterStemmer;

public class NameComparisonUtils {

    private NameComparisonUtils() {
        throw new IllegalStateException("No instantiation provided");
    }

    //

    public static boolean areEqual(Entity namedEntity1, Entity namedEntity2) {
        return areEqual(namedEntity1.getName(), namedEntity2.getName(), PreprocessingMethod.NONE);
    }

    public static double getContainedRatio(Entity namedEntity, List<String> names, PreprocessingMethod methodToUse) {
        List<String> namesSplit = getProcessedSplit(namedEntity.getName());
        List<List<String>> allSublists = getAllSublists(namesSplit);
        int max = 0;
        for (List<String> sublist : allSublists) {
            String substring = concatStrings(sublist);
            if (isContained(substring, names, methodToUse)) {
                max = Math.max(max, sublist.size());
            }
        }
        return (double) max / namesSplit.size();
    }

    // checks if b contains a while respecting word boundaries
    public static boolean isContained(Entity a, Entity b, PreprocessingMethod methodToUse) {
        return isContained(a.getName(), b.getName(), methodToUse);
    }

    public static boolean isInterfaceContained(Entity interfaceNamedEntity, Entity otherNamedEntity, PreprocessingMethod methodToUse) {
        String name = interfaceNamedEntity.getName();
        if (!name.matches("I[A-Z].*$")) {
            return false;
        }
        return isContained(name.substring(1), otherNamedEntity.getName(), methodToUse);
    }

    public static double getRatio(Entity namedEntity1, Entity namedEntity2) {
        return Math.min(1, (double) getWordCount(namedEntity1.getName()) / getWordCount(namedEntity2.getName()));
    }

    public static double getInterfaceRatio(Entity interfaceEntity, Entity otherNamedEntity) {
        return Math.min(1, (double) (getWordCount(interfaceEntity.getName().substring(1))) / getWordCount(otherNamedEntity.getName()));
    }

    //

    private static boolean isContained(String a, String b, PreprocessingMethod methodToUse) {
        List<String> bSplit = getProcessedSplit(b);
        return isContained(preprocess(a), bSplit, methodToUse);
    }

    private static boolean isContained(String a, List<String> names, PreprocessingMethod methodToUse) {
        List<List<String>> allSublists = getAllSublists(names);
        for (List<String> sublist : allSublists) {
            String substring = concatStrings(sublist);
            if (areEqual(a, substring, methodToUse)) {
                return true;
            }
        }
        return false;
    }

    //

    public static List<String> removeWords(Entity namedEntity, SortedSet<String> wordsToRemove) {
        List<String> words = NameComparisonUtils.getProcessedSplit(namedEntity.getName());
        for (String word : wordsToRemove) {
            words.remove(NameComparisonUtils.preprocess(word));
        }
        return words;
    }

    public static SortedSet<String> removeWords(SortedSet<String> words, Entity namedEntity) {
        SortedSet<String> result = new TreeSet<>();
        List<String> wordsToRemove = NameComparisonUtils.getProcessedSplit(namedEntity.getName());
        for (String word : words) {
            word = preprocess(word);
            if (!wordsToRemove.contains(word)) {
                result.add(word);
            }
        }
        return result;
    }

    //

    public static List<CodePackage> getMatchedPackages(Entity archEndpoint, CodeCompilationUnit compUnit) {
        List<String> p = compUnit.getParentPackageNames();
        double similarity = NameComparisonUtils.getContainedRatio(archEndpoint, p, PreprocessingMethod.STEMMING);
        if (similarity == 0) {
            return List.of();
        }
        List<CodePackage> matchedPackages = new ArrayList<>();
        List<CodePackage> p2 = getPackageList(compUnit.getParent());
        for (int i = p.size() - 1; i > 0; i--) {
            double similarity2 = NameComparisonUtils.getContainedRatio(archEndpoint, p.subList(0, i), PreprocessingMethod.STEMMING);
            if (similarity2 < similarity) {
                matchedPackages.addFirst(p2.get(i));
            }
            if (similarity2 == 0) {
                break;
            }
        }
        return matchedPackages;
    }

    public static List<CodePackage> getPackageList(CodeModule codePackage) {
        List<CodePackage> parents = new ArrayList<>();
        CodeModule parent = codePackage;
        if (parent instanceof CodePackage parentPackage) {
            parents.addFirst(parentPackage);
        }
        while (parent.hasParent()) {
            parent = parent.getParent();
            if (parent instanceof CodePackage parentPackage) {
                parents.addFirst(parentPackage);
            }
        }
        return parents;
    }

    //

    public static String preprocess(String name) {
        name = name.replaceAll("\\W", "");
        return name.toLowerCase();
    }

    public static List<String> getProcessedSplit(String name) {
        List<String> namesSplit = split(name);
        List<String> namesProcessed = new ArrayList<>();
        for (String s : namesSplit) {
            namesProcessed.add(preprocess(s));
        }
        return namesProcessed;
    }

    public static List<String> getProcessedSplit(List<String> names) {
        List<String> namesProcessed = new ArrayList<>();
        for (String s : names) {
            namesProcessed.addAll(getProcessedSplit(s));
        }
        return namesProcessed;
    }

    private static <T> List<List<T>> getAllSublists(List<T> list) {
        List<List<T>> allSublists = new ArrayList<>();
        for (int numberOfElements = 1; numberOfElements <= list.size(); numberOfElements++) {
            for (int startIndex = 0; startIndex <= list.size() - numberOfElements; startIndex++) {
                allSublists.add(list.subList(startIndex, startIndex + numberOfElements));
            }
        }
        return allSublists;
    }

    private static String concatStrings(List<String> names) {
        StringBuilder concat = new StringBuilder();
        for (String name : names) {
            concat.append(name);
        }
        return concat.toString();
    }

    private static int getWordCount(String name) {
        return getProcessedSplit(name).size();
    }

    //

    public enum PreprocessingMethod {
        NONE, STEMMING, LEMMATIZATION
    }

    private static boolean areEqual(String name1, String name2, PreprocessingMethod methodToUse) {
        return switch (methodToUse) {
            case NONE -> areEqual(name1, name2);
            case STEMMING -> areEqualStemmed(name1, name2);
            case LEMMATIZATION -> areEqualLemmatized(name1, name2);
        };
    }

    private static boolean areEqual(String name1, String name2) {
        return preprocess(name1).equals(preprocess(name2));
    }

    private static boolean areEqualStemmed(String name1, String name2) {
        PorterStemmer stemmer = new PorterStemmer();
        String stemmed1 = stemmer.stem(name1);
        String stemmed2 = stemmer.stem(name2);
        return areEqual(stemmed1, stemmed2);
    }

    private static boolean areEqualLemmatized(String name1, String name2) {
        // set up pipeline properties
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,pos,lemma");
        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // create document objects
        CoreDocument document = pipeline.processToCoreDocument(name1);
        CoreDocument document2 = pipeline.processToCoreDocument(name2);
        if (document.tokens().size() != document2.tokens().size()) {
            return false;
        }
        for (int i = 0; i < document.tokens().size(); i++) {
            CoreLabel tok = document.tokens().get(i);
            CoreLabel tok2 = document2.tokens().get(i);
            if (!areEqual(tok.lemma(), tok2.lemma())) {
                return false;
            }
        }
        return true;
    }

    //

    private static List<String> split(String name) {
        var a = splitWhiteSpace(name);
        if (a.size() > 1) {
            return a;
        }
        return splitCase(name);
    }

    private static List<String> splitWhiteSpace(String string) {
        return List.of(string.split("[^A-Za-z0-9]"));
    }

    private static List<String> splitCase(String string) {
        return List.of(string.split("(?<!(^|[^a-z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])"));
    }
}
