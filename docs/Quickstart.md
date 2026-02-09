# Quickstart Guide

This guide helps you get started with ARDoCo, whether you want to use it as a library in your projects or contribute to its development.

## Using ARDoCo as a Dependency

ARDoCo is a Maven project and can be embedded in your project by adding its dependencies. See the [main pom.xml](https://github.com/ardoco/ardoco/blob/main/pom.xml) for available modules and their coordinates.

## Prerequisites

- **Java**: JDK 21 or higher
- **Maven**: 3.9 or higher
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code with Java extensions

## Contributing to ARDoCo

Before contributing, please acknowledge the [Code of Conduct](https://github.com/ardoco/ardoco/blob/main/core/CODE_OF_CONDUCT.md).

### Development Setup

1. **Fork and Clone**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/ardoco.git
   cd ardoco
   ```

2. **Build the Project**:
   ```bash
   mvn clean install
   ```

3. **Import into IDE**:
   - Import as Maven project
   - Ensure JDK 21 is configured

### Code Quality Checks

ARDoCo uses **SonarCloud** for code quality analysis. GitHub Actions automatically:
- Verify builds
- Generate SonarCloud reports
- Check pull requests against Quality Gates

Pull requests must pass the Quality Gate before merging.

#### Setting Up SonarCloud for Your Fork

If you fork the project, configure SonarCloud to ensure quality checks work:

1. **Generate Token**:
   - Log into [SonarCloud](https://sonarcloud.io/)
   - Navigate to: Profile → My Account → Security (or directly to `account/security`)
   - Generate an access token and copy it

2. **Add Token to GitHub**:
   - Go to your fork's Settings → Secrets and variables → Actions
   - Add a new repository secret:
     - **Name**: `SONAR_TOKEN`
     - **Value**: Your SonarCloud access token

### Code Formatting

ARDoCo uses consistent code formatting enforced by Spotless:

1. **Use the Provided Formatter**:
   - Formatter configuration: [formatter.xml](https://github.com/ardoco/ardoco/blob/main/formatter.xml)
   - Import this formatter profile into your IDE

2. **Apply Formatting with Spotless**:
   ```bash
   mvn spotless:apply
   ```

   Run this before committing to ensure consistent formatting. More information: [Spotless Maven Plugin](https://github.com/diffplug/spotless/tree/main/plugin-maven)

3. **Check Formatting**:
   ```bash
   mvn spotless:check
   ```

### Nullness Annotations (JSpecify)

ARDoCo uses [JSpecify](https://jspecify.dev/) for null-safety annotations:

- **Default**: All references are non-null via `@NullMarked` (auto-generated during build)
- **Nullable References**: Explicitly mark with `org.jspecify.annotations.Nullable`
- **Avoid**: Do not use other `@Nullable` variants (e.g., `javax.annotation.Nullable`)
- **Auto-Generated Files**: `package-info.java` files are auto-generated and git-ignored - do not commit them

For detailed information, see the dedicated guide: [Nullness and JSpecify](jspecify)

### Eclipse Save Actions (Optional)

If using Eclipse, configure save actions for automatic code cleanup.

**Instructions**:
1. Go to your Eclipse workspace folder
2. Open: `.metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.jdt.ui.prefs`
3. Replace all `sp_cleanup.*` properties with the configuration below

<details>
<summary>Eclipse Save Actions Configuration</summary>

```properties
sp_cleanup.add_default_serial_version_id=true
sp_cleanup.add_generated_serial_version_id=false
sp_cleanup.add_missing_annotations=true
sp_cleanup.add_missing_deprecated_annotations=true
sp_cleanup.add_missing_methods=false
sp_cleanup.add_missing_nls_tags=false
sp_cleanup.add_missing_override_annotations=true
sp_cleanup.add_missing_override_annotations_interface_methods=true
sp_cleanup.add_serial_version_id=false
sp_cleanup.always_use_blocks=true
sp_cleanup.always_use_parentheses_in_expressions=true
sp_cleanup.always_use_this_for_non_static_field_access=false
sp_cleanup.always_use_this_for_non_static_method_access=false
sp_cleanup.convert_functional_interfaces=true
sp_cleanup.convert_to_enhanced_for_loop=true
sp_cleanup.convert_to_enhanced_for_loop_if_loop_var_used=false
sp_cleanup.correct_indentation=true
sp_cleanup.format_source_code=true
sp_cleanup.format_source_code_changes_only=false
sp_cleanup.insert_inferred_type_arguments=false
sp_cleanup.lazy_logical_operator=false
sp_cleanup.make_local_variable_final=true
sp_cleanup.make_parameters_final=false
sp_cleanup.make_private_fields_final=true
sp_cleanup.make_type_abstract_if_missing_method=false
sp_cleanup.make_variable_declarations_final=false
sp_cleanup.merge_conditional_blocks=false
sp_cleanup.never_use_blocks=false
sp_cleanup.never_use_parentheses_in_expressions=false
sp_cleanup.number_suffix=true
sp_cleanup.on_save_use_additional_actions=true
sp_cleanup.organize_imports=true
sp_cleanup.push_down_negation=false
sp_cleanup.qualify_static_field_accesses_with_declaring_class=false
sp_cleanup.qualify_static_member_accesses_through_instances_with_declaring_class=true
sp_cleanup.qualify_static_member_accesses_through_subtypes_with_declaring_class=true
sp_cleanup.qualify_static_member_accesses_with_declaring_class=false
sp_cleanup.qualify_static_method_accesses_with_declaring_class=false
sp_cleanup.remove_private_constructors=true
sp_cleanup.remove_redundant_modifiers=true
sp_cleanup.remove_redundant_semicolons=true
sp_cleanup.remove_redundant_type_arguments=true
sp_cleanup.remove_trailing_whitespaces=true
sp_cleanup.remove_trailing_whitespaces_all=true
sp_cleanup.remove_trailing_whitespaces_ignore_empty=false
sp_cleanup.remove_unnecessary_array_creation=false
sp_cleanup.remove_unnecessary_casts=true
sp_cleanup.remove_unnecessary_nls_tags=false
sp_cleanup.remove_unused_imports=true
sp_cleanup.remove_unused_local_variables=false
sp_cleanup.remove_unused_private_fields=true
sp_cleanup.remove_unused_private_members=false
sp_cleanup.remove_unused_private_methods=true
sp_cleanup.remove_unused_private_types=true
sp_cleanup.simplify_lambda_expression_and_method_ref=false
sp_cleanup.sort_members=false
sp_cleanup.sort_members_all=false
sp_cleanup.use_anonymous_class_creation=false
sp_cleanup.use_autoboxing=true
sp_cleanup.use_blocks=true
sp_cleanup.use_blocks_only_for_return_and_throw=false
sp_cleanup.use_directly_map_method=false
sp_cleanup.use_lambda=true
sp_cleanup.use_parentheses_in_expressions=false
sp_cleanup.use_this_for_non_static_field_access=true
sp_cleanup.use_this_for_non_static_field_access_only_if_necessary=true
sp_cleanup.use_this_for_non_static_method_access=true
sp_cleanup.use_this_for_non_static_method_access_only_if_necessary=true
sp_cleanup.use_unboxing=true
sp_cleanup.use_var=false
```
</details>

### Pull Request Workflow

1. **Create a Branch**:
   ```bash
   git checkout -b feature/my-feature
   ```

2. **Make Changes**:
   - Write your code
   - Add tests if applicable
   - Run formatting: `mvn spotless:apply`

3. **Test Locally**:
   ```bash
   mvn clean verify
   ```

4. **Commit and Push**:
   ```bash
   git add .
   git commit -m "Description of changes"
   git push origin feature/my-feature
   ```

5. **Open Pull Request**:
   - Go to GitHub and create a PR from your branch
   - Wait for automated checks (build, SonarCloud)
   - Address any issues if checks fail

6. **Code Review**:
   - Respond to reviewer feedback
   - Make requested changes
   - Re-run formatting and tests

## Next Steps

- Explore the [Pipeline Architecture](pipeline) to understand ARDoCo's execution model
- Learn about [Traceability Link Recovery](traceability-link-recovery) approaches
- Read about [Inconsistency Detection](inconsistency-detection) capabilities
- Check out [Intermediate Artifacts](intermediate-artifacts) to understand data models
- Visit the [Home page](home) for more information about ARDoCo and its approaches
