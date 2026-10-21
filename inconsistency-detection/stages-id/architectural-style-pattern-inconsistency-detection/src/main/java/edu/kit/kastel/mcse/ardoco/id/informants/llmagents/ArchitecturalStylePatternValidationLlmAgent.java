package edu.kit.kastel.mcse.ardoco.id.informants.llmagents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ArchitecturalStylePatternValidationLlmAgent {

    /**
     * Validates whether a natural-language statement about an architectural style or architectural design pattern in a software repository is true based on the
     * repository's source-code evidence from a code project directory.
     *
     * @param namedEntityOccurrence     the architectural style or pattern named entity occurrence in the statement
     * @param architecturalStylePattern the architectural style or architectural design pattern mentioned in the statement
     * @param fullText                  the full documentation text containing the statement
     * @param line                      the specific line of documentation containing the statement
     * @param codeProjectPath           the path to the root of the code project directory
     * @return an {@link ArchitecturalStylePatternValidationResult} object that indicates whether the statement is true or false, along with a concise
     * explanation of the validation findings
     */
    @SystemMessage("""
            You are a careful software-architecture validation agent.
            
            Your task is to verify whether a natural-language statement about an architectural style or architectural design pattern in a software repository is true.
            
            ## You may use only the provided read-only tools:
            - listDirectory(path): Lists files and directories directly below the given project-relative path.
            - tree(path, maxDepth): Shows a bounded recursive tree below the given project-relative path.
            - readFile(path): Reads a UTF-8 text file from a project-relative path.
            - readFileLines(path, startLine, endLine): Reads a specific inclusive range of lines from a UTF-8 text file.
            - findFiles(glob): Finds project files matching a glob pattern.
            - search(query): Searches project file paths and UTF-8 text contents case-insensitively.
            - getFileInfo(path): Returns metadata about a project-relative file or directory.
            
            ## Validation rules:
            
            1. Validate the statement using repository evidence and do not guess.
            
            2. Determine first whether the statement makes a repository-specific claim about the existence, implementation, application, or structure of the architectural style or pattern.
               * If the statement does not make a repository-specific claim, return true.
               * If the statement makes a repository-specific claim, verify it using source-code evidence, project structure, package structure, configuration files, and implementation artifacts.
               * Return false when the repository evidence contradicts the statement.
            
            3. Do not treat a mere naming coincidence as sufficient evidence.
               For example, a package named "controller" alone does not prove MVC unless the relevant model/view/controller responsibilities or dependencies are visible.
            
            4. Do not use repository documentation as final evidence.
               Documentation may only be used for orientation.
            
            ## Return:
            * true if the statement is supported by repository evidence, or if there is nothing repository-specific to verify
            * false if the statement is contradicted by repository evidence
            * a concise explanation with the concrete evidence you used
            """)
    @UserMessage("""
            Named entity occurrence:
            {{namedEntityOccurrence}}
            
            Architectural style or pattern entity:
            {{architecturalStylePattern}}
            
            Full documentation text:
            {{fullText}}
            
            Current documentation line:
            {{line}}
            
            Current code project root path:
            {{codeProjectPath}}
            
            Decide whether the statement in the current line about the architectural style or pattern ({{architecturalStylePattern}}) is true.
            If the line also talks about other architectural styles or patterns, ignore them and only consider {{architecturalStylePattern}}.
            Use the provided read-only tools to make an informed decision based on repository evidence.
            """)
    ArchitecturalStylePatternValidationResult validateArchitecturalStylePatternStatementAgainstCodeDirectory(
            @V("namedEntityOccurrence") String namedEntityOccurrence, @V("architecturalStylePattern") String architecturalStylePattern,
            @V("fullText") String fullText, @V("line") String line, @V("codeProjectPath") String codeProjectPath);

    /**
     * Validates whether a natural-language statement about an architectural style or architectural design pattern in a software repository is true based on the
     * repository's source-code evidence from a code model.
     *
     * @param namedEntityOccurrence     the architectural style or pattern named entity occurrence in the statement
     * @param architecturalStylePattern the architectural style or architectural design pattern mentioned in the statement
     * @param fullText                  the full documentation text containing the statement
     * @param line                      the specific line of documentation containing the statement
     * @return an {@link ArchitecturalStylePatternValidationResult} object that indicates whether the statement is true or false, along with a concise
     * explanation of the validation findings
     */
    @SystemMessage("""
            You are a careful software-architecture validation agent.
            
            Your task is to verify whether a natural-language statement about an architectural style or architectural design pattern in a software repository is true.
            
            ## You may use only the provided read-only tools:
            - listDirectory(path): Lists files and directories directly below the given model-relative path.
            - tree(path, maxDepth): Shows a bounded recursive tree below the given model-relative path.
            - findFiles(glob): Finds code-model elements matching a glob pattern.
            - getFileInfo(path): Returns metadata about a model element.
            
            ## Validation rules:
            
            1. Validate the statement using repository evidence and do not guess.
            
            2. Determine first whether the statement makes a repository-specific claim about the existence, implementation, application, or structure of the architectural style or pattern.
               * If the statement does not make a repository-specific claim, return true.
               * If the statement makes a repository-specific claim, verify it using source-code evidence, project structure, package structure, configuration files, and implementation artifacts.
               * Return false when the repository evidence contradicts the statement.
            
            3. Do not treat a mere naming coincidence as sufficient evidence.
               For example, a package named "controller" alone does not prove MVC unless the relevant model/view/controller responsibilities or dependencies are visible.
            
            4. Do not use repository documentation as final evidence.
               Documentation may only be used for orientation.
            
            ## Return:
            * true if the statement is supported by repository evidence, or if there is nothing repository-specific to verify
            * false if the statement is contradicted by repository evidence
            * a concise explanation with the concrete evidence you used
            """)
    @UserMessage("""
            Named entity occurrence:
            {{namedEntityOccurrence}}
            
            Architectural style or pattern entity:
            {{architecturalStylePattern}}
            
            Full documentation text:
            {{fullText}}
            
            Current documentation line:
            {{line}}
            
            Decide whether the statement in the current line about the architectural style or pattern ({{architecturalStylePattern}}) is true.
            If the line also talks about other architectural styles or patterns, ignore them and only consider {{architecturalStylePattern}}.
            Use the provided read-only tools to make an informed decision based on code-model evidence.
            """)
    ArchitecturalStylePatternValidationResult validateArchitecturalStylePatternStatementAgainstCodeModel(
            @V("namedEntityOccurrence") String namedEntityOccurrence, @V("architecturalStylePattern") String architecturalStylePattern,
            @V("fullText") String fullText, @V("line") String line);
}
