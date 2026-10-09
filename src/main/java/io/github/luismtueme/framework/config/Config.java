package io.github.luismtueme.framework.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.UnaryOperator;

/**
 * The single source of settings for the framework and both runners (Cucumber and JUnit specs).
 *
 * <p>Each setting is read from, first match wins: JVM system properties ({@code -DPLATFORM=ios}), environment
 * variables, a {@code .env} file in the working directory, then {@code config/defaults.properties} on the classpath
 * (non-secret defaults only). Every variable is listed in {@code .env.example}.
 *
 * <p>Invalid values fail at startup with the variable name, so a typo never silently falls back to a default.
 */
public record Config(
        Platform platform,
        String environment,
        URI appiumServerUrl,
        Path appPath,
        Optional<String> androidAppPackage,
        Optional<String> androidAppActivity,
        Optional<String> androidAppWaitActivity,
        Optional<String> iosBundleId,
        Optional<String> deviceName,
        Optional<String> platformVersion,
        Optional<String> deviceUdid,
        Optional<Path> appiumWdaPath,
        Duration waitTimeout,
        Credentials credentials,
        String apiBaseUrl,
        boolean usesDemoApp) {

    private static final String DEFAULTS = "/config/defaults.properties";

    /** The login for mobile scenarios, or a message saying which variables to set. */
    public Credentials requireCredentials() {
        return credentials;
    }

    /** The settings for this run, loaded once. */
    public static Config get() {
        return Holder.INSTANCE;
    }

    private static final class Holder {
        private static final Config INSTANCE = load();
    }

    /** Loads from system properties, environment variables, {@code .env} and the classpath defaults. */
    public static Config load() {
        Map<String, String> dotenv = new HashMap<>();
        for (DotenvEntry entry :
                Dotenv.configure().ignoreIfMissing().load().entries(Dotenv.Filter.DECLARED_IN_ENV_FILE)) {
            dotenv.put(entry.getKey(), entry.getValue());
        }
        Properties defaults = new Properties();
        try (InputStream in = Config.class.getResourceAsStream(DEFAULTS)) {
            if (in != null) defaults.load(in);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + DEFAULTS, e);
        }
        return from(name -> {
            String value = System.getProperty(name);
            if (value == null) value = System.getenv(name);
            if (value == null) value = dotenv.get(name);
            if (value == null) value = defaults.getProperty(name);
            return value;
        });
    }

    /**
     * Builds a config from a lookup function (variable name to value, or null when unset). Unit tests pass a map.
     *
     * @throws ConfigException naming the first invalid variable
     */
    public static Config from(UnaryOperator<String> lookup) {
        Settings settings = new Settings(lookup);

        Platform platform = settings.text("PLATFORM")
                .map(value -> Platform.fromId("PLATFORM", value))
                .orElse(Platform.ANDROID);

        String customAndroidApp = settings.text("ANDROID_APP").orElse("");
        String customIosApp = settings.text("IOS_APP").orElse("");
        boolean usesDemoApp =
                switch (platform) {
                    case ANDROID -> customAndroidApp.isEmpty();
                    case IOS -> customIosApp.isEmpty();
                };

        Path appPath = Path.of(
                switch (platform) {
                    case ANDROID -> usesDemoApp
                            ? settings.require("DEMO_ANDROID_APP", "demo Android app path is missing from defaults")
                            : customAndroidApp;
                    case IOS -> usesDemoApp
                            ? settings.require("DEMO_IOS_APP", "demo iOS app path is missing from defaults")
                            : customIosApp;
                });

        Optional<Credentials> credentials = settings.text("APP_USERNAME")
                .flatMap(user -> settings.text("APP_PASSWORD").map(password -> new Credentials(user, password)));
        if (credentials.isEmpty() && usesDemoApp) {
            credentials = Optional.of(new Credentials(
                    settings.require("DEMO_USERNAME", "demo credentials are missing from defaults"),
                    settings.require("DEMO_PASSWORD", "demo credentials are missing from defaults")));
        }
        Credentials resolvedCredentials = credentials.orElseThrow(() -> new ConfigException(
                "APP_USERNAME and APP_PASSWORD must be set when ANDROID_APP or IOS_APP points at your own app"));

        Optional<String> androidPackage = settings.text("ANDROID_APP_PACKAGE");
        Optional<String> androidActivity = settings.text("ANDROID_APP_ACTIVITY");
        Optional<String> androidWaitActivity = settings.text("ANDROID_APP_WAIT_ACTIVITY");
        Optional<String> iosBundleId = settings.text("IOS_BUNDLE_ID");
        if (usesDemoApp) {
            androidPackage = Optional.of(
                    settings.require("DEMO_ANDROID_PACKAGE", "demo Android package is missing from defaults"));
            androidActivity = Optional.of(
                    settings.require("DEMO_ANDROID_ACTIVITY", "demo Android activity is missing from defaults"));
            androidWaitActivity = Optional.of(settings.require(
                    "DEMO_ANDROID_WAIT_ACTIVITY", "demo Android wait activity is missing from defaults"));
            iosBundleId =
                    Optional.of(settings.require("DEMO_IOS_BUNDLE_ID", "demo iOS bundle id is missing from defaults"));
        }

        URI appiumServerUrl =
                settings.url("APPIUM_SERVER_URL").map(URI::create).orElse(URI.create("http://127.0.0.1:4723"));

        Optional<Path> appiumWdaPath =
                settings.text("APPIUM_WDA_PATH").map(Path::of).filter(Files::isDirectory);

        return new Config(
                platform,
                settings.text("TEST_ENV").orElse("local"),
                appiumServerUrl,
                appPath,
                androidPackage,
                androidActivity,
                androidWaitActivity,
                iosBundleId,
                settings.text("DEVICE_NAME"),
                settings.text("PLATFORM_VERSION"),
                settings.text("DEVICE_UDID"),
                appiumWdaPath,
                Duration.ofMillis(settings.positiveInt("WAIT_TIMEOUT").orElse(15_000)),
                resolvedCredentials,
                settings.url("API_BASE_URL").orElse("https://jsonplaceholder.typicode.com"),
                usesDemoApp);
    }

    /** True when the configured app binary exists on disk (after {@code scripts/download-apps.sh}). */
    public void requireAppPresent() {
        if (!Files.exists(appPath)) {
            throw new ConfigException(
                    ("App binary not found at %s. Run scripts/download-apps.sh for the demo apps, or set ANDROID_APP /"
                                    + " IOS_APP to your own binary.")
                            .formatted(appPath.toAbsolutePath().normalize()));
        }
    }

    /** Typed, validated reads. Blank values count as unset. */
    private record Settings(UnaryOperator<String> lookup) {

        Optional<String> text(String name) {
            return Optional.ofNullable(lookup.apply(name)).map(String::trim).filter(value -> !value.isEmpty());
        }

        String require(String name, String because) {
            return text(name)
                    .orElseThrow(() -> new ConfigException("%s must be set because %s".formatted(name, because)));
        }

        Optional<Integer> positiveInt(String name) {
            return text(name).map(value -> {
                try {
                    int number = Integer.parseInt(value);
                    if (number > 0) return number;
                } catch (NumberFormatException ignored) {
                    // reported below
                }
                throw new ConfigException("%s must be a positive whole number, got \"%s\"".formatted(name, value));
            });
        }

        /** An http(s) URL without a trailing slash. */
        Optional<String> url(String name) {
            return text(name).map(value -> {
                if (!value.matches("https?://[^\\s/]+(/\\S*)?")) {
                    throw new ConfigException(
                            "%s must be an http(s) URL like http://127.0.0.1:4723, got \"%s\"".formatted(name, value));
                }
                return value.replaceAll("/+$", "");
            });
        }
    }
}
