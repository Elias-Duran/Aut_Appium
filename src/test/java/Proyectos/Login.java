package Proyectos;

import Proyectos.Pages.LoginPage;
import com.automation.base.BaseTest;
import com.automation.base.report.ExtentReportManager;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import org.testng.annotations.Test;

public class Login extends BaseTest {

    @Test
    public void loginExitoso() {
        ExtentTest test = ExtentReportManager.getReport().createTest("Login exitoso");

        LoginPage loginPage = new LoginPage();

        test.info("Paso 1: La app abrió correctamente",
                MediaEntityBuilder.createScreenCaptureFromPath(tomarCaptura("01_app_abierta")).build());

        loginPage.ingresarUsuario("standard_user");
        test.pass("Paso 2: Usuario ingresado correctamente",
                MediaEntityBuilder.createScreenCaptureFromPath(tomarCaptura("02_usuario_ingresado")).build());

        loginPage.ingresarPassword("secret_sauce");
        test.pass("Paso 3: Password ingresada correctamente",
                MediaEntityBuilder.createScreenCaptureFromPath(tomarCaptura("03_password_ingresado")).build());

        loginPage.clickIngresar();
        test.pass("Paso 4: Botón login presionado correctamente",
                MediaEntityBuilder.createScreenCaptureFromPath(tomarCaptura("04_login_presionado")).build());

        ExtentReportManager.getReport().flush();
    }
}