package com.googlecode.dex2jar;

import org.slf4j.Logger;

public class ExceptionUtils {
    public static void niceExceptionMessage(Logger log, Throwable t, int deep) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < deep + 1; i++) {
            sb.append(".");
        }
        sb.append(' ');
        if (t instanceof DexException) {
            sb.append(t.getMessage());
            log.error(sb.toString());
            if (t.getCause() != null) {
                niceExceptionMessage(log, t.getCause(), deep + 1);
            }
        } else {
            if (t != null) {
                log.error(sb.append("ROOT cause:").toString(), t);
            }
        }
    }
}
