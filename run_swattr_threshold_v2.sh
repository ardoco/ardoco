#!/bin/bash
# Test SwATTR with aggressive precision configs focusing on MinProportion and Levenshtein.
set -e

PROPS_FILE="core/framework/common/src/main/resources/configs/CommonTextToolsConfig.properties"
RESULTS_DIR="tlr/tests-tlr/target/swattr-threshold-v2"

cp "$PROPS_FILE" "${PROPS_FILE}.bak"
mkdir -p "$RESULTS_DIR"

run_config() {
    local name=$1
    local jw_thresh=$2
    local lev_thresh=$3
    local lev_maxdist=$4
    local min_prop=$5
    local lev_enabled=${6:-true}

    echo ""
    echo "=========================================="
    echo "  CONFIG: $name"
    echo "  JW=$jw_thresh  LEV=$lev_thresh  DIST=$lev_maxdist  PROP=$min_prop  LEV_ON=$lev_enabled"
    echo "=========================================="

    cat > "$PROPS_FILE" <<EOF
separators_ToContain=. :: : _
separators_ToSplit=\\\\. :: : _
getMostRecommendedIByRef_MinProportion=$min_prop
getMostRecommendedIByRef_Increase=0.05
# Levenshtein
levenshtein_Enabled=$lev_enabled
levenshtein_MinLength=2
levenshtein_MaxDistance=$lev_maxdist
levenshtein_Threshold=$lev_thresh
# Jaro Winkler
jaroWinkler_Enabled=true
jaroWinkler_SimilarityThreshold=$jw_thresh
EOF

    mvn install -pl core/framework/common -DskipTests -q
    mvn test -pl tlr/tests-tlr -Dtest=SwattrHyperparamSingle -q 2>&1 || true

    if [ -d "tlr/tests-tlr/target/swattr-single-results" ]; then
        for f in tlr/tests-tlr/target/swattr-single-results/*_single.txt; do
            if [ -f "$f" ]; then
                newname=$(echo "$f" | sed "s|swattr-single-results/|swattr-threshold-v2/|;s/_single\.txt/_${name}.txt/")
                cp "$f" "$newname"
            fi
        done
    fi
}

# A: High MinProportion (0.8) — require 80% of name parts to match
run_config "prop80" "0.90" "0.90" "1" "0.8"

# B: Full MinProportion (1.0) — require ALL name parts to match
run_config "prop100" "0.90" "0.90" "1" "1.0"

# C: No Levenshtein — only JW similarity, no edit distance
run_config "no_lev" "0.90" "0.90" "0" "0.5" "false"

# D: JW=0.95 + high MinProp (0.8) — combined precision
run_config "jw95_prop80" "0.95" "0.95" "0" "0.8"

# E: JW=0.95 + full MinProp (1.0) — maximum precision
run_config "jw95_prop100" "0.95" "0.95" "0" "1.0"

# F: Default JW + no Lev + high MinProp — isolate MinProportion effect
run_config "nolev_prop80" "0.90" "0.90" "0" "0.8" "false"

# Restore original
cp "${PROPS_FILE}.bak" "$PROPS_FILE"
mvn install -pl core/framework/common -DskipTests -q
rm "${PROPS_FILE}.bak"

echo ""
echo "=========================================="
echo "  ALL V2 CONFIGS COMPLETE"
echo "  Results in: $RESULTS_DIR"
echo "=========================================="
