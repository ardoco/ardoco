package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.summary.ResultSummary;

public class DriverClasspathDebugTest {

    @Test
    void printDriverLocation() {
        Class<?> cls = ResultSummary.class;
        var cs = cls.getProtectionDomain().getCodeSource();
        System.out.println("ResultSummary class: " + cls.getName());
        System.out.println("CodeSource: " + (cs == null ? "null (possibly in a container classloader)" : cs.getLocation()));
        Package p = cls.getPackage();
        if (p != null) {
            System.out.println("Package Implementation Title: " + p.getImplementationTitle());
            System.out.println("Package Implementation Version: " + p.getImplementationVersion());
            System.out.println("Package Specification Version: " + p.getSpecificationVersion());
        } else {
            System.out.println("Package info is null");
        }
        // Print a simple reflection check for method presence:
        try {
            var m = cls.getDeclaredMethod("gqlStatusObjects");
            System.out.println("Method gqlStatusObjects() present: " + (m != null));
        } catch (NoSuchMethodException e) {
            System.out.println("Method gqlStatusObjects() NOT present at runtime");
        }
    }
}
