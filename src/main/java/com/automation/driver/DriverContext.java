package com.automation.driver;

import io.appium.java_client.android.AndroidDriver;

public class DriverContext {

    private static AndroidDriver driver;

    public static void setDriver(AndroidDriver androidDriver) {
        driver = androidDriver;
    }

    public static AndroidDriver getDriver() {
        return driver;
    }

    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}