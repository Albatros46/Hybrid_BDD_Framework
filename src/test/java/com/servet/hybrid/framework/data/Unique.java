package com.servet.hybrid.framework.data;

import java.util.Locale;
import java.util.UUID;

/**
 * Every scenario creates its own data with a random part: scenarios never collide, not in parallel
 * and not when they run again against the same database. One instance per scenario (PicoContainer).
 */
public class Unique {

    private final String id = UUID.randomUUID().toString().substring(0, 8);

    public String id() {
        return id;
    }

    /** "Lena" -> lena.1a2b3c4d@hybrid-bdd.test */
    public String email(String alias) {
        return alias.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "") + "." + id + "@hybrid-bdd.test";
    }

    /** "Kaffee" -> "Kaffee 1a2b3c4d" for names that must be unique in the application. */
    public String name(String alias) {
        return alias + " " + id;
    }

    /** A throw-away password for a throw-away account. */
    public String password(String alias) {
        return "Hb-" + id + "-" + alias;
    }
}
