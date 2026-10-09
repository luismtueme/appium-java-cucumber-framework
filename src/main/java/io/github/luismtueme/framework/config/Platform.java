package io.github.luismtueme.framework.config;

import java.util.Locale;

/** The mobile platform under test for this run. */
public enum Platform {
    ANDROID("android"),
    IOS("ios");

    private final String id;

    Platform(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public boolean isAndroid() {
        return this == ANDROID;
    }

    public boolean isIos() {
        return this == IOS;
    }

    public static Platform fromId(String variable, String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (Platform platform : values()) {
            if (platform.id.equals(normalized)) return platform;
        }
        throw new ConfigException("%s must be one of android, ios, got \"%s\"".formatted(variable, value));
    }
}
