# Programming Language Text Extensions

Analyzing the benchmark projects we found the following types of sentences about programming languages:
```
    TYPE
    5 possible template sentences for the type

1.  IMPLEMENTATION — whole system/application completely/partly
    The system is written completely in [LANGUAGE].
    The application is implemented entirely in [LANGUAGE].
    The software is developed using [LANGUAGE].
    The entire system is built using [LANGUAGE].
    The project is implemented primarily in [LANGUAGE].
2.  LANGUAGE_USE — language used for a particular purpose
    [LANGUAGE] is used for [TASK].
    The system uses [LANGUAGE] for [TASK].
    [LANGUAGE] is used to handle [TASK].
    The application uses [LANGUAGE] for [TASK].
    [LANGUAGE] is used primarily for [TASK].
3.  LANGUAGE_FILES — mention of specific files in a language
    The project contains [FILE].
    [FILE] is included in the project.
    The repository contains [FILE].
    The system includes [FILE].
    [FILE] is part of the source code.
4.  LANGUAGE_ARTIFACT — language associated with files/code/classes/scripts
    The project contains [LANGUAGE] source files.
    The application includes [LANGUAGE] classes.
    The system uses [LANGUAGE] scripts for [TASK].
    The repository contains [LANGUAGE] code.
    The frontend consists of [LANGUAGE] files.
5.  LANGUAGE_COMPONENT — language associated with a particular component
    The [COMPONENT] is implemented entirely in [LANGUAGE].
    The [COMPONENT] is written primarily in [LANGUAGE].
    The [COMPONENT] uses [LANGUAGE] for its implementation.
    The [COMPONENT] is developed using [LANGUAGE].
    The implementation of the [COMPONENT] is based on [LANGUAGE].
6.  LANGUAGE_STACK — language mentioned as part of a technology stack/list
    The system uses [LANGUAGE], [LANGUAGE], and [LANGUAGE].
    The technology stack includes [LANGUAGE] alongside [TECHNOLOGY].
    The application is built with [LANGUAGE] and [TECHNOLOGY].
    The project uses [LANGUAGE] as well as [TECHNOLOGY] and [TECHNOLOGY].
    The software stack consists of [LANGUAGE], [TECHNOLOGY], and [TECHNOLOGY].

Template insertions:
[LANGUAGE] Python, Java, C++, Ruby, JavaScript, TypeScript, Go, C, Rust, PHP
[COMPONENT] frontend, backend, storage component, client component, UI component
[TECHNOLOGY] React, Angular, PostgreSQL, MySQL, Docker, Maven
[TASK] data processing, deployment, database migration, build automation, data transformation
[FILE] utils.py UserService.java server.cpp database.rb app.js config.ts main.go parser.c lib.rs login.php
```

We filled these templates like this:
``` python
import random

# Fixed seed for reproducibility
random.seed(42)

templates = {
    1: [
        "The system is written completely in [LANGUAGE].",
        "The application is implemented entirely in [LANGUAGE].",
        "The software is developed using [LANGUAGE].",
        "The entire system is built using [LANGUAGE].",
        "The project is implemented primarily in [LANGUAGE].",
    ],

    2: [
        "[LANGUAGE] is used for [TASK].",
        "The system uses [LANGUAGE] for [TASK].",
        "[LANGUAGE] is used to handle [TASK].",
        "The application uses [LANGUAGE] for [TASK].",
        "[LANGUAGE] is used primarily for [TASK].",
    ],

    3: [
        "The project contains [FILE].",
        "[FILE] is included in the project.",
        "The repository contains [FILE].",
        "The system includes [FILE].",
        "[FILE] is part of the source code.",
    ],

    4: [
        "The project contains [LANGUAGE] source files.",
        "The application includes [LANGUAGE] classes.",
        "The system uses [LANGUAGE] scripts for [TASK].",
        "The repository contains [LANGUAGE] code.",
        "The frontend consists of [LANGUAGE] files.",
    ],

    5: [
        "The [COMPONENT] is implemented entirely in [LANGUAGE].",
        "The [COMPONENT] is written primarily in [LANGUAGE].",
        "The [COMPONENT] uses [LANGUAGE] for its implementation.",
        "The [COMPONENT] is developed using [LANGUAGE].",
        "The implementation of the [COMPONENT] is based on [LANGUAGE].",
    ],

    6: [
        "The system uses [LANGUAGE], [LANGUAGE], and [LANGUAGE].",
        "The technology stack includes [LANGUAGE] alongside [TECHNOLOGY].",
        "The application is built with [LANGUAGE] and [TECHNOLOGY].",
        "The project uses [LANGUAGE] as well as [TECHNOLOGY] and [TECHNOLOGY].",
        "The software stack consists of [LANGUAGE], [TECHNOLOGY], and [TECHNOLOGY].",
    ],
}


values = {
    "LANGUAGE": [
        "Python",
        "Java",
        "C++",
        "Ruby",
        "JavaScript",
        "TypeScript",
        "Go",
        "C",
        "Rust",
        "PHP",
    ],

    "COMPONENT": [
        "frontend",
        "backend",
        "storage component",
        "client component",
        "UI component",
    ],

    "TECHNOLOGY": [
        "React",
        "Angular",
        "PostgreSQL",
        "MySQL",
        "Docker",
        "Maven",
    ],

    "TASK": [
        "data processing",
        "deployment",
        "database migration",
        "build automation",
        "data transformation",
    ],

    "FILE": [
        "utils.py",
        "UserService.java",
        "server.cpp",
        "database.rb",
        "app.js",
        "config.ts",
        "main.go",
        "parser.c",
        "lib.rs",
        "login.php",
    ],
}


def replace_placeholder(template, placeholder):
    """
    Replace every occurrence of a placeholder independently.

    This means:
        [LANGUAGE], [LANGUAGE]
    can become:
        Python, Java

    and:
        [TECHNOLOGY], [TECHNOLOGY]
    can become:
        Docker, PostgreSQL
    """
    token = f"[{placeholder}]"

    while token in template:
        value = random.choice(values[placeholder])
        template = template.replace(token, value, 1)

    return template


def fill_template(template):
    """Fill all placeholders in a template."""

    # These are all independently sampled, including repeated occurrences.
    placeholders = [
        "LANGUAGE",
        "TECHNOLOGY",
        "COMPONENT",
        "TASK",
        "FILE",
    ]

    for placeholder in placeholders:
        template = replace_placeholder(template, placeholder)

    return template


def generate_batch(batch_number):
    batch = []

    for type_id in range(1, 7):
        template = templates[type_id][batch_number-1]
        sentence = fill_template(template)

        batch.append(sentence)

    return batch


# Generate 4 projects × 5 batches
for project_number in range(1, 5):
    print(f"=== Project {project_number} ===")

    for batch_number in range(1, 6):
        print(f"--- Batch {batch_number} ---")

        batch = generate_batch(batch_number)

        for sentence in batch:
            print(sentence)

        print()
```

We use these extensions (=filled templates) to the original SAD texts:
```
=== Project 1 = TEAMMATES ===
--- Batch 1 ---
The system is written completely in Java.
Python is used for database migration.
The project contains database.rb.
The project contains Ruby source files.
The frontend is implemented entirely in C++.
The system uses Rust, Java, and PHP.
--- Batch 2 ---
The application is implemented entirely in Go.
The system uses Python for data processing.
UserService.java is included in the project.
The application includes Ruby classes.
The UI component is written primarily in Ruby.
The technology stack includes PHP alongside React.
--- Batch 3 ---
The software is developed using Rust.
Ruby is used to handle data transformation.
The repository contains main.go.
The system uses Ruby scripts for build automation.
The storage component uses PHP for its implementation.
The application is built with Python and Angular.
--- Batch 4 ---
The entire system is built using Go.
The application uses TypeScript for database migration.
The system includes server.cpp.
The repository contains Ruby code.
The frontend is developed using TypeScript.
The project uses Java as well as MySQL and React.
--- Batch 5 ---
The project is implemented primarily in TypeScript.
TypeScript is used primarily for data transformation.
app.js is part of the source code.
The frontend consists of Python files.
The implementation of the UI component is based on C.
The software stack consists of Java, MySQL, and React.
=== Project 2 = SCONS ===
--- Batch 1 ---
The system is written completely in Rust.
JavaScript is used for data transformation.
The project contains config.ts.
The project contains PHP source files.
The frontend is implemented entirely in Ruby.
The system uses Python, Ruby, and JavaScript.
--- Batch 2 ---
The application is implemented entirely in Java.
The system uses Ruby for data processing.
main.go is included in the project.
The application includes JavaScript classes.
The storage component is written primarily in C.
The technology stack includes C++ alongside PostgreSQL.
--- Batch 3 ---
The software is developed using TypeScript.
Ruby is used to handle database migration.
The repository contains UserService.java.
The system uses PHP scripts for deployment.
The backend uses Rust for its implementation.
The application is built with C++ and MySQL.
--- Batch 4 ---
The entire system is built using Go.
The application uses JavaScript for data transformation.
The system includes database.rb.
The repository contains TypeScript code.
The backend is developed using Python.
The project uses Python as well as PostgreSQL and MySQL.
--- Batch 5 ---
The project is implemented primarily in JavaScript.
Java is used primarily for deployment.
login.php is part of the source code.
The frontend consists of TypeScript files.
The implementation of the client component is based on Ruby.
The software stack consists of Go, Maven, and MySQL.
=== Project 3 = ROD===
--- Batch 1 ---
The system is written completely in C++.
JavaScript is used for deployment.
The project contains database.rb.
The project contains Rust source files.
The storage component is implemented entirely in Rust.
The system uses PHP, Go, and PHP.
--- Batch 2 ---
The application is implemented entirely in Go.
The system uses TypeScript for deployment.
server.cpp is included in the project.
The application includes Rust classes.
The frontend is written primarily in C.
The technology stack includes Python alongside React.
--- Batch 3 ---
The software is developed using C++.
C++ is used to handle build automation.
The repository contains login.php.
The system uses Java scripts for build automation.
The UI component uses Go for its implementation.
The application is built with C and Docker.
--- Batch 4 ---
The entire system is built using JavaScript.
The application uses Rust for data processing.
The system includes UserService.java.
The repository contains Rust code.
The storage component is developed using JavaScript.
The project uses Java as well as PostgreSQL and MySQL.
--- Batch 5 ---
The project is implemented primarily in C++.
C is used primarily for data processing.
app.js is part of the source code.
The frontend consists of Rust files.
The implementation of the UI component is based on C++.
The software stack consists of Java, Maven, and PostgreSQL.
=== Project 4 = ZENGARDEN===
--- Batch 1 ---
The system is written completely in Rust.
PHP is used for deployment.
The project contains server.cpp.
The project contains TypeScript source files.
The UI component is implemented entirely in C++.
The system uses Rust, Python, and PHP.
--- Batch 2 ---
The application is implemented entirely in TypeScript.
The system uses C for data processing.
UserService.java is included in the project.
The application includes TypeScript classes.
The backend is written primarily in JavaScript.
The technology stack includes Python alongside Angular.
--- Batch 3 ---
The software is developed using PHP.
Java is used to handle data processing.
The repository contains parser.c.
The system uses Java scripts for data transformation.
The backend uses C++ for its implementation.
The application is built with C and Docker.
--- Batch 4 ---
The entire system is built using C++.
The application uses JavaScript for data transformation.
The system includes login.php.
The repository contains Go code.
The UI component is developed using Ruby.
The project uses Ruby as well as Maven and PostgreSQL.
--- Batch 5 ---
The project is implemented primarily in Go.
TypeScript is used primarily for build automation.
lib.rs is part of the source code.
The frontend consists of C files.
The implementation of the backend is based on Java.
The software stack consists of Ruby, React, and PostgreSQL.
```
