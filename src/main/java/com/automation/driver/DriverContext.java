package com.automation.driver;

import io.appium.java_client.android.AndroidDriver;

public class DriverContext {

    private static AndroidDriver driver;

    public static void setDriver(AndroidDriver androidDriver) {
        DriverContext.driver = androidDriver;
    }

    public static AndroidDriver getDriver() {
        return DriverContext.driver;
    }

    public static void quitDriver() {
        if (DriverContext.driver != null) {
            DriverContext.driver.quit();
            DriverContext.driver = null;
        }
    }
}