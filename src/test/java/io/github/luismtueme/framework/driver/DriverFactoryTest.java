package io.github.luismtueme.framework.driver;

import static org.assertj.core.api.Assertions.assertThat;

import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.options.XCUITestOptions;
import io.github.luismtueme.framework.config.Config;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DriverFactoryTest {

    private static Config config(Map<String, String> overrides) {
        Map<String, String> all = new HashMap<>();
        all.put("DEMO_ANDROID_APP", "apps/demo.apk");
        all.put("DEMO_IOS_APP", "apps/demo.app");
        all.put("DEMO_ANDROID_PACKAGE", "com.swaglabsmobileapp");
        all.put("DEMO_ANDROID_ACTIVITY", "com.swaglabsmobileapp.MainActivity");
        all.put("DEMO_IOS_BUNDLE_ID", "com.saucelabs.SwagLabsMobileApp");
        all.put("DEMO_USERNAME", "bob@example.com");
        all.put("DEMO_PASSWORD", "10203040");
        all.put("API_BASE_URL", "https://jsonplaceholder.typicode.com");
        all.put("APPIUM_SERVER_URL", "http://127.0.0.1:4723");
        all.putAll(overrides);
        return Config.from(all::get);
    }

    @Test
    void androidOptionsUseUiAutomator2AndTheAppPath() {
        UiAutomator2Options options = DriverFactory.androidOptions(config(Map.of("PLATFORM", "android")));
        assertThat(options.getPlatformName()).hasToString("ANDROID");
        assertThat(options.getAutomationName())
                .hasValueSatisfying(name -> assertThat(name).hasToString("UiAutomator2"));
        assertThat(options.getApp()).hasValueSatisfying(app -> assertThat(app).endsWith("apps/demo.apk"));
        assertThat(options.getAppPackage()).hasValue("com.swaglabsmobileapp");
    }

    @Test
    void iosOptionsUseXcuiTestAndTheAppPath() {
        XCUITestOptions options = DriverFactory.iosOptions(config(Map.of(
                "PLATFORM", "ios",
                "DEVICE_NAME", "iPhone 16",
                "PLATFORM_VERSION", "18.5")));
        assertThat(options.getPlatformName()).hasToString("IOS");
        assertThat(options.getAutomationName())
                .hasValueSatisfying(name -> assertThat(name).hasToString("XCUITest"));
        assertThat(options.getApp()).hasValueSatisfying(app -> assertThat(app).endsWith("apps/demo.app"));
        assertThat(options.getDeviceName()).hasValue("iPhone 16");
        assertThat(options.getPlatformVersion()).hasValue("18.5");
    }
}
