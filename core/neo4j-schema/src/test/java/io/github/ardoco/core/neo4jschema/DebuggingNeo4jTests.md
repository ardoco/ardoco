# How to debug Neo4j tests
In order to debug Neo4j tests it may help to look at the visual representation of the graph. To do so, you can use the Neo4j Browser.

To access the Neo4j Browser, you can follow these steps:

1. Select the test you want to debug in your IDE and set a breakpoint where you want to inspect the graph.
2. In order to get the connection details for the Neo4j Browser, you can for example add a print statement in your test to output 
the connection details (e.g., URI, username, password) when the test runs. For example, you can add the following code snippet to the beginning of 
your test method:
```
        // --- VISUALIZATION BLOCK ---
        System.out.println("----------------------------------------------------------");
        System.out.println("neo4j browser: " + neo4jContainer.getHttpUrl()); // e.g., http://localhost:32789
        System.out.println("password:      " + neo4jContainer.getAdminPassword());
        System.out.println("Connect URL:   " + neo4jContainer.getBoltUrl());
        System.out.println("----------------------------------------------------------");
```
3. Run the test in debug mode. When the execution hits the breakpoint, it will pause, allowing you to inspect the state of the application.
4. Open the Neo4j Browser in your web browser by navigating to the URL printed in the console (e.g., http://localhost:32789).
5. Log in to the Neo4j Browser using the username (usually "neo4j") and the password printed in the console.
6. Once logged in, you can execute Cypher queries to explore the graph.
