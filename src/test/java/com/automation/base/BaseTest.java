package com.automation.base;

import com.automation.driver.DriverContext;
import com.automation.driver.DriverFactory;
import io.appium.java_client.android.AndroidDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;


import java.net.MalformedURLException;

public class BaseTest {

    protected AndroidDriver driver;

    @BeforeMethod
    public void setUp() throws MalformedURLException {
        driver = DriverFactory.createAndroidDriver();
        DriverContext.setDriver(driver);
    }

    @AfterMethod
    public void tearDown() {
        DriverContext.quitDriver();
    }
}