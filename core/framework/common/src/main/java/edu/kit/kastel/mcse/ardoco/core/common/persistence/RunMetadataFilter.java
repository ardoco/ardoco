/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.common.persistence;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

public final class RunMetadataFilter {

    private static final List<String> SECRET_KEY_SUBSTRINGS = List.of("password", "secret", "token", "apikey", "key");

    private static final Pattern URI_USER_INFO = Pattern.compile("^([a-zA-Z][a-zA-Z0-9+.-]*://)[^@/]+@(.*)$");

    private RunMetadataFilter() {
    }

    public static boolean isSecretLikeKey(String key) {
        String lower = key.toLowerCase();
        return SECRET_KEY_SUBSTRINGS.stream().anyMatch(lower::contains);
    }

    public static String maskUserInfo(String value) {
        var matcher = URI_USER_INFO.matcher(value);
        if (!matcher.matches()) {
            return value;
        }
        return matcher.group(1) + "***@" + matcher.group(2);
    }

    public static SortedMap<String, String> filterSecrets(ImmutableSortedMap<String, String> configuration, List<String> droppedKeysOut) {
        SortedMap<String, String> result = new TreeMap<>();
        for (var entry : configuration.keyValuesView()) {
            String key = entry.getOne();
            if (isSecretLikeKey(key)) {
                droppedKeysOut.add(key);
                continue;
            }
            result.put(key, maskUserInfo(entry.getTwo()));
        }
        return result;
    }
}
