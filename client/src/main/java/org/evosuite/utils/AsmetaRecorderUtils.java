package org.evosuite.utils;

import org.evosuite.Properties;

public final class AsmetaRecorderUtils {

    private static final String RESERVED_METHOD_PREFIX = "__asmeta";

    private AsmetaRecorderUtils() {
    }

    public static boolean isRecorderMethod(String methodName) {
        return Properties.ASMETA_CHOICE_TRACE_FILE != null
                && !Properties.ASMETA_CHOICE_TRACE_FILE.trim().isEmpty()
                && methodName.startsWith(RESERVED_METHOD_PREFIX);
    }
}
