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
        all.put("DEMO_ANDROID_ACTIVITY", "com.swaglabsmobileapp.SplashActivity");
        all.put("DEMO_ANDROID_WAIT_ACTIVITY", "com.swaglabsmobileapp.MainActivity");
        all.put("DEMO_IOS_BUNDLE_ID", "com.saucelabs.SwagLabsMobileApp");
        all.put("DEMO_USERNAME", "standard_user");
        all.put("DEMO_PASSWORD", "secret_sauce");
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
        assertThat(options.getAppActivity()).hasValue("com.swaglabsmobileapp.SplashActivity");
        assertThat(options.getAppWaitActivity()).hasValue("com.swaglabsmobileapp.MainActivity");
        assertThat(options.doesIgnoreHiddenApiPolicyError()).hasValue(true);
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

    @Test
    void iosOptionsPreferUdidAndPrebuiltWdaWhenProvided() throws Exception {
        java.nio.file.Path wda = java.nio.file.Files.createTempDirectory("wda-runner");
        try {
            XCUITestOptions options = DriverFactory.iosOptions(config(Map.of(
                    "PLATFORM", "ios",
                    "DEVICE_UDID", "AAAA-BBBB",
                    "APPIUM_WDA_PATH", wda.toString())));
            assertThat(options.getUdid()).hasValue("AAAA-BBBB");
            assertThat(options.getCapability("appium:prebuiltWDAPath"))
                    .isEqualTo(wda.toAbsolutePath().normalize().toString());
            assertThat(options.getCapability("appium:usePreinstalledWDA")).isEqualTo(true);
        } finally {
            java.nio.file.Files.deleteIfExists(wda);
        }
    }
}
