package com.automation.driver;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

import java.net.MalformedURLException;
import java.net.URL;

public class DriverFactory {

    public static AndroidDriver createAndroidDriver() throws MalformedURLException {

        UiAutomator2Options options = new UiAutomator2Options();

        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");
        options.setDeviceName("emulator-5554");
        options.setApp(System.getProperty("user.dir") + "/src/main/resources/app/app.apk");

        return new AndroidDriver(
                new URL("http://127.0.0.1:4723"),
                options
        );
    }
}
