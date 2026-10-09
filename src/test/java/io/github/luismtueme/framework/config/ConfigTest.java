package io.github.luismtueme.framework.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConfigTest {

    private static Config from(Map<String, String> values) {
        Map<String, String> all = new HashMap<>();
        // Defaults the unit test needs when not overridden
        all.put("DEMO_ANDROID_APP", "apps/demo.apk");
        all.put("DEMO_IOS_APP", "apps/demo.app");
        all.put("DEMO_ANDROID_PACKAGE", "com.example.app");
        all.put("DEMO_ANDROID_ACTIVITY", "com.example.app.Main");
        all.put("DEMO_IOS_BUNDLE_ID", "com.example.app");
        all.put("DEMO_USERNAME", "bob@example.com");
        all.put("DEMO_PASSWORD", "10203040");
        all.put("API_BASE_URL", "https://jsonplaceholder.typicode.com");
        all.put("APPIUM_SERVER_URL", "http://127.0.0.1:4723");
        all.putAll(values);
        return Config.from(all::get);
    }

    @Test
    void defaultsToAndroidDemoApp() {
        Config config = from(Map.of());
        assertThat(config.platform()).isEqualTo(Platform.ANDROID);
        assertThat(config.usesDemoApp()).isTrue();
        assertThat(config.appPath()).isEqualTo(Path.of("apps/demo.apk"));
        assertThat(config.credentials().username()).isEqualTo("bob@example.com");
    }

    @Test
    void readsIosPlatform() {
        Config config = from(Map.of("PLATFORM", "ios"));
        assertThat(config.platform()).isEqualTo(Platform.IOS);
        assertThat(config.appPath()).isEqualTo(Path.of("apps/demo.app"));
        assertThat(config.iosBundleId()).contains("com.example.app");
    }

    @Test
    void customAppRequiresCredentials() {
        assertThatThrownBy(() -> from(Map.of("ANDROID_APP", "/tmp/my.apk")))
                .isInstanceOf(ConfigException.class)
                .hasMessageContaining("APP_USERNAME");
    }

    @Test
    void customAppUsesProvidedCredentials() {
        Config config = from(Map.of(
                "ANDROID_APP", "/tmp/my.apk",
                "APP_USERNAME", "user",
                "APP_PASSWORD", "secret",
                "ANDROID_APP_PACKAGE", "com.mine",
                "ANDROID_APP_ACTIVITY", ".Main"));
        assertThat(config.usesDemoApp()).isFalse();
        assertThat(config.appPath()).isEqualTo(Path.of("/tmp/my.apk"));
        assertThat(config.credentials().username()).isEqualTo("user");
        assertThat(config.androidAppPackage()).contains("com.mine");
    }

    @Test
    void rejectsUnknownPlatform() {
        assertThatThrownBy(() -> from(Map.of("PLATFORM", "windows")))
                .isInstanceOf(ConfigException.class)
                .hasMessageContaining("PLATFORM");
    }

    @Test
    void rejectsInvalidServerUrl() {
        assertThatThrownBy(() -> from(Map.of("APPIUM_SERVER_URL", "not-a-url")))
                .isInstanceOf(ConfigException.class)
                .hasMessageContaining("APPIUM_SERVER_URL");
    }

    @Test
    void rejectsNonPositiveTimeout() {
        assertThatThrownBy(() -> from(Map.of("WAIT_TIMEOUT", "0")))
                .isInstanceOf(ConfigException.class)
                .hasMessageContaining("WAIT_TIMEOUT");
    }

    @Test
    void credentialsToStringHidesPassword() {
        assertThat(new Credentials("bob", "secret").toString())
                .doesNotContain("secret")
                .contains("bob");
    }

    @Test
    void requireAppPresentFailsWhenBinaryIsMissing() {
        Config config = from(Map.of(
                "ANDROID_APP", "/tmp/definitely-missing-appium-demo.apk", "APP_USERNAME", "u", "APP_PASSWORD", "p"));
        assertThatThrownBy(config::requireAppPresent)
                .isInstanceOf(ConfigException.class)
                .hasMessageContaining("download-apps.sh");
    }

    @Test
    void platformHelpers() {
        assertThat(Platform.ANDROID.isAndroid()).isTrue();
        assertThat(Platform.ANDROID.isIos()).isFalse();
        assertThat(Platform.IOS.isIos()).isTrue();
        assertThat(Platform.IOS.id()).isEqualTo("ios");
    }
}
