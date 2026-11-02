package com.servet.hybrid.framework.api;

/** Reports are shared; tokens and passwords must not be in them. */
public final class Masking {

    private static final String SECRET_FIELDS = "token|accessToken|refreshToken|password|oldPassword|newPassword";

    private Masking() {
    }

    public static String mask(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replaceAll("(\"(" + SECRET_FIELDS + ")\"\\s*:\\s*\")[^\"]*\"", "$1***\"")
                .replaceAll("eyJ[\\w-]{5,}\\.[\\w-]+\\.[\\w-]+", "***jwt***");
    }
}
