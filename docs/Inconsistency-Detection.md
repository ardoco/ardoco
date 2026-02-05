# Inconsistency Detection

Inconsistency Detection in ARDoCo identifies discrepancies between software architecture documentation (SAD) and architecture models (SAM). It detects two types of inconsistencies: Text Entity Absent from Model (TEAM) that are described in documentation but absent from the model, and Model Entity Absent from Text (MEAT) that exist in the model but are not documented.

> **Note:** In previous versions of ARDoCo (V1), these were called "Missing Model Elements (MMEs)" and "Unmentioned/Undocumented Model Elements (UMEs)" respectively. The terminology was updated in V2 to be more descriptive and consistent.

For more information about inconsistency detection approaches, visit [ardoco.de/approaches/inconsistency-detection](https://ardoco.de/approaches/inconsistency-detection/).

## Types of Inconsistencies

ARDoCo detects two main types of inconsistencies between documentation and architecture models:

### Text Entity Absent from Model (TEAM)

**Text Entity Absent from Model (TEAM)** inconsistencies (formerly called "Missing Model Elements" or MME) are architecture elements described in the Software Architecture Documentation (SAD) that cannot be traced to the Software Architecture Model (SAM).

**Detection Approach**:
1. Runs trace link recovery (using SWATTR) to link documentation sentences to model elements
2. Identifies text mentions that cannot be linked to any model element
3. Applies filters to increase precision:
   - Removes common software development terminology that resembles components but rarely represents actual model elements
   - Filters out generic terms and framework-specific terminology
   - Uses configurable whitelists and thresholds

Each text mention without a corresponding model link indicates an architectural concept described in documentation but missing from the formal model.

### Model Entity Absent from Text (MEAT)

**Model Entity Absent from Text (MEAT)** inconsistencies (formerly called "Unmentioned Model Elements" or "Undocumented Model Elements" or UME) are elements within the Software Architecture Model (SAM) that do not have corresponding descriptions (or insufficient documentation) in the natural language Software Architecture Documentation (SAD).

**Detection Approach**:
1. Examines each model element and its associated trace links from the TLR results
2. Identifies elements with no trace links or below a configurable threshold (default: 1 link)
3. Applies regex-based whitelisting to exclude intentionally unmentioned elements

**Configuration Options**:
- **Threshold**: Minimum number of trace links required (default: 1)
- **Whitelist**: Regex patterns for elements that should be excluded from MEAT detection

## Detection Strategy

The inconsistency detection approach uses trace link recovery as a bridge:

1. **TLR Pipeline**: Run SWATTR to establish trace links between SAD and SAM
2. **Analysis**: Identify orphan elements (MEAT with no links, TEAM with no matching model elements)
3. **Filtering**: Apply heuristics and whitelists to reduce false positives
4. **Reporting**: Output identified inconsistencies for manual review

## Use Cases

- **Architecture Evolution**: Identifying outdated or missing documentation during system evolution
- **Quality Assurance**: Ensuring completeness of documentation before releases
- **Onboarding**: Helping new team members understand gaps in documentation
- **Consistency Checking**: Verifying alignment between informal documents and formal models
- **Reverse Engineering**: Finding unmentioned components when analyzing existing systems

For implementation details, see the [inconsistency-detection module](https://github.com/ardoco/ardoco/tree/main/inconsistency-detection).
