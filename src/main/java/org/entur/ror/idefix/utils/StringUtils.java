package org.entur.ror.idefix.utils;

public class StringUtils {

    private StringUtils() {
    }

    public static String extractLookupKey(String ref) {
        int firstColon = ref.indexOf(':');
        if (firstColon < 0) return ref;
        int secondColon = ref.indexOf(':', firstColon + 1);
        if (secondColon < 0) return ref;
        int thirdColon = ref.indexOf(':', secondColon + 1);
        if (thirdColon < 0) return ref;

        String operator = ref.substring(firstColon + 1, secondColon);
        String numericalValue = ref.substring(thirdColon + 1);
        return operator + ":" + numericalValue;
    }
}
