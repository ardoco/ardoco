# Inconsistency Detection

Inconsistency Detection in ARDoCo identifies discrepancies between software architecture documentation (SAD) and architecture models (SAM). It detects two types of inconsistencies: Missing Model Elements (MMEs) that are described in documentation but absent from the model, and Unmentioned Model Elements (UMEs) that exist in the model but are not documented.

For more information about inconsistency detection approaches, visit [ardoco.de/approaches/inconsistency-detection](https://ardoco.de/approaches/inconsistency-detection/).

## Types of Inconsistencies

ARDoCo detects two main types of inconsistencies between documentation and architecture models:

### Missing Model Elements (MME)

**Missing Model Elements** are architecture elements described in the Software Architecture Documentation (SAD) that cannot be traced to the Software Architecture Model (SAM).

**Detection Approach**:
1. Runs trace link recovery (using SWATTR) to link documentation sentences to model elements
2. Identifies text mentions that cannot be linked to any model element
3. Applies filters to increase precision:
   - Removes common software development terminology that resembles components but rarely represents actual model elements
   - Filters out generic terms and framework-specific terminology
   - Uses configurable whitelists and thresholds

Each text mention without a corresponding model link indicates an architectural concept described in documentation but missing from the formal model.

### Unmentioned Model Elements (UME)

**Unmentioned Model Elements** are elements within the Software Architecture Model (SAM) that do not have corresponding descriptions (or insufficient documentation) in the natural language Software Architecture Documentation (SAD).

**Detection Approach**:
1. Examines each model element and its associated trace links from the TLR results
2. Identifies elements with no trace links or below a configurable threshold (default: 1 link)
3. Applies regex-based whitelisting to exclude intentionally undocumented elements

**Configuration Options**:
- **Threshold**: Minimum number of trace links required (default: 1)
- **Whitelist**: Regex patterns for elements that should be excluded from UME detection

## Detection Strategy

The inconsistency detection approach uses trace link recovery as a bridge:

1. **TLR Pipeline**: Run SWATTR to establish trace links between SAD and SAM
2. **Analysis**: Identify orphan elements (UMEs with no links, MMEs with no matching model elements)
3. **Filtering**: Apply heuristics and whitelists to reduce false positives
4. **Reporting**: Output identified inconsistencies for manual review

## Use Cases

- **Architecture Evolution**: Identifying outdated or missing documentation during system evolution
- **Quality Assurance**: Ensuring completeness of documentation before releases
- **Onboarding**: Helping new team members understand gaps in documentation
- **Consistency Checking**: Verifying alignment between informal documents and formal models
- **Onboarding**: Helping new team members understand gaps in documentation
- **Reverse Engineering**: Finding undocumented components when analyzing existing systems

For implementation details, see the [inconsistency-detection module](https://github.com/ardoco/ardoco/tree/main/inconsistency-detection).
