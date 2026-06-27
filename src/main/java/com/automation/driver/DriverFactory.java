package com.automation.driver;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

import java.net.MalformedURLException;
import java.net.URL;

public class DriverFactory {

    private static final String DEFAULT_APPIUM_SERVER_URL = "http://127.0.0.1:4723";
    private static final String DEFAULT_DEVICE_NAME = "emulator-5554";
    private static final String DEFAULT_APP_FILE = "Android.SauceLabs.Mobile.Sample.app.2.7.1.apk";

    public static AndroidDriver createAndroidDriver() throws MalformedURLException {

        UiAutomator2Options options = new UiAutomator2Options();

        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");
        options.setDeviceName(getEnv("ANDROID_DEVICE_NAME", DEFAULT_DEVICE_NAME));
        options.setApp(getAppPath());
        options.setAppWaitActivity("*");

        return new AndroidDriver(
                new URL(getEnv("APPIUM_SERVER_URL", DEFAULT_APPIUM_SERVER_URL)),
                options
        );
    }

    private static String getAppPath() {
        String appPath = System.getenv("APP_PATH");
        if (appPath != null && !appPath.isBlank()) {
            return appPath;
        }
        return System.getProperty("user.dir") + "/src/test/resources/app/" + DEFAULT_APP_FILE;
    }

    private static String getEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value != null && !value.isBlank()) {
            return value;
        }
        return defaultValue;
    }
}
