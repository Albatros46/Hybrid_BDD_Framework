package com.servet.hybrid.framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * Configuration in three layers, the first one that has a value wins:
 * system property (-Dcafe.api.url=...) > environment variable (CAFE_API_URL) > env/&lt;env&gt;.properties.
 * The profile is chosen with -Denv=local|ci (default local).
 */
public final class Env {

    public static final String NAME = System.getProperty("env", "local");
    private static final Properties FILE = load(NAME);

    private Env() {
    }

    public static String get(String key) {
        String value = System.getProperty(key);
        if (blank(value)) {
            value = System.getenv(key.toUpperCase(Locale.ROOT).replace('.', '_'));
        }
        if (blank(value)) {
            value = FILE.getProperty(key);
        }
        if (blank(value)) {
            throw new IllegalStateException("Missing setting '" + key + "' (profile " + NAME + "): set -D" + key
                    + " or " + key.toUpperCase(Locale.ROOT).replace('.', '_'));
        }
        return value.trim();
    }

    public static boolean flag(String key) {
        return Boolean.parseBoolean(get(key));
    }

    private static Properties load(String name) {
        Properties properties = new Properties();
        try (InputStream in = Env.class.getResourceAsStream("/env/" + name + ".properties")) {
            if (in == null) {
                throw new IllegalStateException("No profile src/test/resources/env/" + name + ".properties");
            }
            properties.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read profile " + name, e);
        }
        return properties;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
