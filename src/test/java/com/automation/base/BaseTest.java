package com.automation.base;

import com.automation.driver.DriverContext;
import com.automation.driver.DriverFactory;
import io.appium.java_client.android.AndroidDriver;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;



import java.io.File;
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
    public String tomarCaptura(String nombre) {
        try {
            File source = driver.getScreenshotAs(OutputType.FILE);

            String carpeta = System.getProperty("user.dir") + "/target/reports/screenshots/";
            File directorio = new File(carpeta);
            directorio.mkdirs();

            String rutaCompleta = carpeta + nombre + ".png";
            File destino = new File(rutaCompleta);

            FileUtils.copyFile(source, destino);

            return "screenshots/" + nombre + ".png";

        } catch (Exception e) {
            throw new RuntimeException("Error al tomar captura: " + e.getMessage());
        }
    }
}