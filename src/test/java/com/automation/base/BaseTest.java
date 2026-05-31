package com.automation.base;

import com.automation.driver.DriverContext;
import com.automation.driver.DriverFactory;
import io.appium.java_client.android.AndroidDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.net.MalformedURLException;

public class BaseTest {

    protected AndroidDriver driver;

    @BeforeEach
    public void setUp() throws MalformedURLException {
        driver = DriverFactory.createAndroidDriver();
        DriverContext.setDriver(driver);
    }

    @AfterEach
    public void tearDown() {
        DriverContext.quitDriver();
    }
}