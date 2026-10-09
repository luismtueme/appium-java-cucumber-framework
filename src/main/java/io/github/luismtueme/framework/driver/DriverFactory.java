package io.github.luismtueme.framework.driver;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import io.github.luismtueme.framework.config.Config;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;

/**
 * Creates one Appium session per scenario or JUnit spec.
 *
 * <p>Android uses UiAutomator2; iOS uses XCUITest. The app path, package/activity or bundle id come from
 * {@link Config}. Implicit waits stay at zero — screen objects use explicit waits only.
 */
public final class DriverFactory {

    private DriverFactory() {}

    public static AppiumDriver create(Config config) {
        config.requireAppPresent();
        URL serverUrl = toUrl(config.appiumServerUrl());
        AppiumDriver driver =
                switch (config.platform()) {
                    case ANDROID -> new AndroidDriver(serverUrl, androidOptions(config));
                    case IOS -> new IOSDriver(serverUrl, iosOptions(config));
                };
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        return driver;
    }

    static UiAutomator2Options androidOptions(Config config) {
        UiAutomator2Options options = new UiAutomator2Options()
                .setPlatformName("Android")
                .setAutomationName("UiAutomator2")
                .setApp(config.appPath().toAbsolutePath().normalize().toString())
                .setNoReset(false)
                .setNewCommandTimeout(Duration.ofSeconds(120));
        config.deviceName().ifPresent(options::setDeviceName);
        config.platformVersion().ifPresent(options::setPlatformVersion);
        config.androidAppPackage().ifPresent(options::setAppPackage);
        config.androidAppActivity().ifPresent(options::setAppActivity);
        if (config.deviceName().isEmpty()) {
            options.setDeviceName("Android Emulator");
        }
        return options;
    }

    static XCUITestOptions iosOptions(Config config) {
        XCUITestOptions options = new XCUITestOptions()
                .setPlatformName("iOS")
                .setAutomationName("XCUITest")
                .setApp(config.appPath().toAbsolutePath().normalize().toString())
                .setNoReset(false)
                .setNewCommandTimeout(Duration.ofSeconds(120))
                .setWdaLaunchTimeout(Duration.ofMinutes(2));
        config.deviceName().ifPresent(options::setDeviceName);
        config.platformVersion().ifPresent(options::setPlatformVersion);
        config.deviceUdid().ifPresent(options::setUdid);
        config.iosBundleId().ifPresent(options::setBundleId);
        if (config.deviceName().isEmpty() && config.deviceUdid().isEmpty()) {
            options.setDeviceName("iPhone 16");
        }
        // Prefer a downloaded / prebuilt WebDriverAgent when CI (or a local cache) provides one.
        config.appiumWdaPath().ifPresent(wda -> {
            String absolute = wda.toAbsolutePath().normalize().toString();
            options.setCapability("appium:prebuiltWDAPath", absolute);
            options.setCapability("appium:usePreinstalledWDA", true);
        });
        return options;
    }

    private static URL toUrl(URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid APPIUM_SERVER_URL: " + uri, e);
        }
    }
}
