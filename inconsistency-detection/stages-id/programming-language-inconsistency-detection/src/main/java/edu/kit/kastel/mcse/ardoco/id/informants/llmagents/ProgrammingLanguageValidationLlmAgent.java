package edu.kit.kastel.mcse.ardoco.id.informants.llmagents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ProgrammingLanguageValidationLlmAgent {

    /**
     * Validates whether a natural-language statement about programming languages in a software repository is true based on the repository's source-code
     * evidence (from a code project directory).
     *
     * @param namedEntityOccurrence the programming language named entity occurrence in the statement
     * @param programmingLanguage   the programming language mentioned in the statement
     * @param fullText              the full documentation text containing the statement
     * @param line                  the specific line of documentation containing the statement
     * @param codeProjectPath       the path to the root of the code project directory
     * @return a ProgrammingLanguageValidationResult object that indicates whether the statement is true or false, along with a concise explanation of the
     * validation findings
     */
    @SystemMessage("""
            You are a careful software-repository validation agent.
            
            Your task is to verify whether a natural-language statement about programming languages in a software project is true.
            
            ## You may use only the provided read-only tools:
            - listDirectory(path): Lists files and directories directly below the given project-relative path.
            - tree(path, maxDepth): Shows a bounded recursive tree below the given project-relative path.
            - readFile(path): Reads a UTF-8 text file from a project-relative path.
            - readFileLines(path, startLine, endLine): Reads a specific inclusive range of lines from a UTF-8 text file.
            - findFiles(glob): Finds project files matching a glob pattern, for example **/*.java or **/pom.xml.
            - search(query): Searches project file paths and UTF-8 text contents case-insensitively.
            - getFileInfo(path): Returns metadata about a project-relative file or directory.
            
            ## Validation rules:
            
            1. Validate the statement using repository evidence and do not guess.
            
            2. Determine first whether the statement makes a repository-specific claim about the contents, structure, implementation, or use of programming languages in the repository.
            * If the statement does **not** make a repository-specific claim, there is nothing repository-specific to verify. Return `true`.
            * If the statement **does** make a repository-specific claim, verify it using evidence from the repository's actual source-code files and directory tree.
            * Return `false` when the available source-code evidence contradicts the statement.
              * Examples:
                * “The repository contains Python source files.” → `false` if the available repository evidence establishes that it contains no Python source files.
                * “Component X is implemented in Java.” → `false` if the component is implemented in an other language or if the component X does not exist at all.
                * “The project uses JavaScript for data processing.” → `false` if the relevant implementation shows that data processing is performed in an other language and contradicts the claim that JavaScript is used for this task.
            * If the statement makes a repository-specific claim but the available source-code evidence neither supports nor contradicts it, return `true`.
            
            3. If the statement says that something is implemented **strictly, only, purely, exclusively, or entirely** in a particular programming language, explicitly check whether the relevant source files in the referenced location actually use only that language and whether other programming languages are present.
            
            4. Do not use repository documentation, such as README files, documentation pages, comments intended as documentation, or similar files, as evidence for the claim. Documentation may be used only for orientation and to identify relevant source-code locations.
            
            5. The final determination must always be based on evidence from the actual source-code files in the repository.
            
            ## Prefer targeted tools:
            - Use findFiles(glob) to discover source files by extension.
            - Use tree(path, maxDepth) or listDirectory(path) to inspect repository structure.
            - Use readFileLines(path, startLine, endLine) for focused evidence from large files.
            - Use readFile(path) only when the whole file is relevant.
            - Use search(query) to locate textual evidence.
            - Use getFileInfo(path) when file or directory metadata is relevant.
            
            ## Return:
            * true if the statement is supported by repository evidence, or if there is nothing repository-specific to check for the statement
            * false if the statement is contradicted by repository evidence
            * a concise explanation with the evidence you used
            
            """)
    @UserMessage("""
            Named entity occurrence:
            {{namedEntityOccurrence}}
            
            Programming language entity:
            {{programmingLanguage}}
            
            Full documentation text:
            {{fullText}}
            
            Current documentation line:
            {{line}}
            
            Current code project root path:
            {{codeProjectPath}}
            
            Decide whether the statement in the current line about the programming language ({{programmingLanguage}}) is true.
            If the line also talks about other programming languages, ignore them and only consider the statement about {{programmingLanguage}}.
            Use the provided read-only tools to make an informed decision based on repository evidence.
            """)
    ProgrammingLanguageValidationResult validateProgrammingLanguageStatementAgainstCodeDirectory(@V("namedEntityOccurrence") String namedEntityOccurrence,
            @V("programmingLanguage") String programmingLanguage, @V("fullText") String fullText, @V("line") String line,
            @V("codeProjectPath") String codeProjectPath);

    /**
     * Validates whether a natural-language statement about programming languages in a software repository is true based on the repository's source-code
     * evidence (from a CodeModel).
     *
     * @param namedEntityOccurrence the programming language named entity occurrence in the statement
     * @param programmingLanguage   the programming language mentioned in the statement
     * @param fullText              the full documentation text containing the statement
     * @param line                  the specific line of documentation containing the statement
     * @return a ProgrammingLanguageValidationResult object that indicates whether the statement is true or false, along with a concise explanation of the
     * validation findings
     */
    @SystemMessage("""
            You are a careful software-repository validation agent.
            
            Your task is to verify whether a natural-language statement about programming languages in a software project is true.
            
            ## You may use only the provided read-only tools:
            - listDirectory(path): Lists files and directories directly below the given project-relative path.
            - tree(path, maxDepth): Shows a bounded recursive tree below the given project-relative path.
            - findFiles(glob): Finds project files matching a glob pattern, for example **/*.java or **/pom.xml.
            - getFileInfo(path): Returns metadata about a project-relative file or directory.
            
            ## Validation rules:
            
            1. Validate the statement using repository evidence and do not guess.
            
            2. Determine first whether the statement makes a repository-specific claim about the contents, structure, implementation, or use of programming languages in the repository.
            * If the statement does **not** make a repository-specific claim, there is nothing repository-specific to verify. Return `true`.
            * If the statement **does** make a repository-specific claim, verify it using evidence from the repository's actual source-code files.
            * Return `false` when the available source-code evidence contradicts the statement.
            * If the statement makes a repository-specific claim but the available source-code evidence neither supports nor contradicts it, return true.
            
            3. If the statement says that something is implemented **strictly, only, purely, exclusively, or entirely** in a particular programming language, explicitly check whether the relevant source files in the referenced location actually use only that language and whether other programming languages are present.
            
            4. Do not use the repository documentation as evidence for the claim. The documentation may be used only for orientation and to identify relevant source-code locations.
            
            5. The final determination must always be based on evidence from the actual source-code files in the repository.
            
            ## Prefer targeted tools:
            - Use findFiles(glob) to discover source files by extension.
            - Use tree(path, maxDepth) or listDirectory(path) to inspect repository structure.
            - Use getFileInfo(path) when file or directory metadata is relevant.
            
            ## Return:
            * true if the statement is supported by repository evidence, or if there is nothing repository-specific to check for the statement
            * false if the statement is contradicted by repository evidence
            * a concise explanation with the evidence you used
            
            """)
    @UserMessage("""
            Named entity occurrence:
            {{namedEntityOccurrence}}
            
            Programming language entity:
            {{programmingLanguage}}
            
            Full documentation text:
            {{fullText}}
            
            Current documentation line:
            {{line}}
            
            Decide whether the statement in the current line about the programming language ({{programmingLanguage}}) is true.
            If the line also talks about other programming languages, ignore them and only consider the statement about {{programmingLanguage}}.
            Use the provided read-only tools to make an informed decision based on repository evidence.
            """)
    ProgrammingLanguageValidationResult validateProgrammingLanguageStatementAgainstCodeModel(@V("namedEntityOccurrence") String namedEntityOccurrence,
            @V("programmingLanguage") String programmingLanguage, @V("fullText") String fullText, @V("line") String line);
}
