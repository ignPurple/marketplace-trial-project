package net.ignpurple.marketplace.util.replacer;

import java.util.HashMap;

public class Replacer extends HashMap<String, String> {

    private Replacer() {}

    public static Replacer create() {
        return new Replacer();
    }

    public Replacer replacer(String key, String value) {
        this.put(key, value);
        return this;
    }
}
