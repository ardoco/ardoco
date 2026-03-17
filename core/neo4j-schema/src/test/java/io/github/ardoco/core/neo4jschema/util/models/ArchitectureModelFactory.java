package io.github.ardoco.core.neo4jschema.util.models;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelWithComponentsAndInterfaces;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class ArchitectureModelFactory {

    public static ArchitectureModelWithComponentsAndInterfaces createArchitectureModel() {
        var loginMethod = new ArchitectureMethod("login(User, Pass)");
        var logoutMethod = new ArchitectureMethod("logout(Session)");
        var fetchDataMethod = new ArchitectureMethod("fetchData(Query)");

        var authMethods = new TreeSet<>(List.of(loginMethod, logoutMethod));
        var authInterface = new ArchitectureInterface("IAuth", "id_iface_auth", authMethods);

        var dataMethods = new TreeSet<>(List.of(fetchDataMethod));
        var dataInterface = new ArchitectureInterface("IDataStore", "id_iface_data", dataMethods);

        var dbDriver = new ArchitectureComponent(
                "DatabaseDriver", "id_comp_db_driver",
                new TreeSet<>(), new TreeSet<>(), new TreeSet<>(), "Driver"
        );


        var authComponent = new ArchitectureComponent(
                "AuthService",
                "id_comp_auth",
                new TreeSet<>(), // No subcomponents
                new TreeSet<>(List.of(authInterface)), // Provides IAuth
                new TreeSet<>(List.of(dataInterface)), // Requires IDataStore (Dependency)
                "Service"
        );

        var persistenceComponent = new ArchitectureComponent(
                "PersistenceLayer",
                "id_comp_persistence",
                new TreeSet<>(List.of(dbDriver)), // Has a subcomponent
                new TreeSet<>(List.of(dataInterface)), // Provides IDataStore
                new TreeSet<>(), // No requirements
                "Component"
        );

        List<ArchitectureItem> allItems = new ArrayList<>();
        allItems.add(authComponent);
        allItems.add(persistenceComponent);
        allItems.add(dbDriver);
        allItems.add(authInterface);
        allItems.add(dataInterface);
        return new ArchitectureModelWithComponentsAndInterfaces(allItems);
    }
}
