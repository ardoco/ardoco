/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PersistenceTestCleanupContractTest {

    @Test
    @DisplayName("No AbstractPersistenceTest subclass declares its own @AfterEach (the base class only wipes a database the test owns)")
    void noSubclassDeclaresItsOwnAfterEach() throws Exception {
        Path classesRoot = Path.of(AbstractPersistenceTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path packageDirectory = classesRoot.resolve(AbstractPersistenceTest.class.getPackageName().replace('.', File.separatorChar));
        List<Class<?>> subclasses = new ArrayList<>();
        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(packageDirectory)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".class")).toList()) {
                String relative = classesRoot.relativize(file).toString();
                String className = relative.substring(0, relative.length() - ".class".length()).replace(File.separatorChar, '.');
                Class<?> type = Class.forName(className, false, getClass().getClassLoader());
                if (type == AbstractPersistenceTest.class || !AbstractPersistenceTest.class.isAssignableFrom(type)) {
                    continue;
                }
                subclasses.add(type);
                for (Method method : type.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(AfterEach.class)) {
                        offenders.add(type.getSimpleName() + "#" + method.getName());
                    }
                }
            }
        }
        Assertions.assertFalse(subclasses.isEmpty(), "the scan must find the persistence test classes");
        Assertions.assertEquals(List.of(), offenders);
    }
}
