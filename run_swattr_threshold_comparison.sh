#!/bin/bash
# Runs SwATTR with different similarity thresholds by modifying the properties file
# and running in separate JVM invocations.

set -e

PROPS_FILE="core/framework/common/src/main/resources/configs/CommonTextToolsConfig.properties"
RUNNER_CLASS="edu.kit.kastel.mcse.ardoco.tlr.tests.integration.evaluation.SwattrHyperparamRunner"
RESULTS_DIR="tlr/tests-tlr/target/swattr-threshold-results"

# Save original
cp "$PROPS_FILE" "${PROPS_FILE}.bak"

mkdir -p "$RESULTS_DIR"

run_config() {
    local name=$1
    local jw_thresh=$2
    local lev_thresh=$3
    local lev_maxdist=$4
    local min_prop=$5

    echo ""
    echo "=========================================="
    echo "  CONFIG: $name"
    echo "  JaroWinkler threshold: $jw_thresh"
    echo "  Levenshtein threshold: $lev_thresh"
    echo "  Levenshtein maxDist:   $lev_maxdist"
    echo "  MinProportion:         $min_prop"
    echo "=========================================="

    # Write properties file
    cat > "$PROPS_FILE" <<EOF
separators_ToContain=. :: : _
separators_ToSplit=\\\\. :: : _
getMostRecommendedIByRef_MinProportion=$min_prop
getMostRecommendedIByRef_Increase=0.05
# Levenshtein
levenshtein_Enabled=true
levenshtein_MinLength=2
levenshtein_MaxDistance=$lev_maxdist
levenshtein_Threshold=$lev_thresh
# Jaro Winkler
jaroWinkler_Enabled=true
jaroWinkler_SimilarityThreshold=$jw_thresh
EOF

    echo "Properties file:"
    cat "$PROPS_FILE"
    echo ""

    # Rebuild only the common module (fast, contains the properties)
    mvn install -pl core/framework/common -DskipTests -q

    # Run the single-config test
    mvn test -pl tlr/tests-tlr -Dtest=SwattrHyperparamSingle -q 2>&1 || true

    # Copy results
    if [ -d "tlr/tests-tlr/target/swattr-single-results" ]; then
        cp -r "tlr/tests-tlr/target/swattr-single-results/"* "$RESULTS_DIR/" 2>/dev/null || true
        # Rename files to include config name
        for f in "$RESULTS_DIR/"*_single.txt; do
            if [ -f "$f" ]; then
                newname=$(echo "$f" | sed "s/_single\.txt/_${name}.txt/")
                mv "$f" "$newname"
            fi
        done
    fi
}

# Config: DEFAULT (original values)
run_config "default" "0.90" "0.90" "1" "0.5"

# Config: PRECISION (stricter matching)
run_config "precision" "0.95" "0.95" "0" "0.7"

# Config: RECALL (looser matching)
run_config "recall" "0.85" "0.85" "2" "0.4"

# Config: PRECISION_JW_ONLY (only tighten JaroWinkler)
run_config "precision_jw" "0.95" "0.90" "1" "0.5"

# Config: RECALL_JW_ONLY (only loosen JaroWinkler)
run_config "recall_jw" "0.85" "0.90" "1" "0.5"

# Restore original
cp "${PROPS_FILE}.bak" "$PROPS_FILE"
mvn install -pl core/framework/common -DskipTests -q
rm "${PROPS_FILE}.bak"

echo ""
echo "=========================================="
echo "  ALL CONFIGS COMPLETE"
echo "  Results in: $RESULTS_DIR"
echo "=========================================="
