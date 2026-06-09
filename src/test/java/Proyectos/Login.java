package Proyectos;

import Proyectos.Pages.LoginPage;
import com.automation.base.BaseTest;
import org.testng.annotations.Test;

public class Login extends BaseTest {

    @Test
    public void loginExitoso() {
        System.out.println("La app abrio correctamente");

        LoginPage loginPage = new LoginPage();

        loginPage.ingresarUsuario("standard_user");
        loginPage.ingresarPassword("secret_sauce");
        loginPage.btnIngresarElement();
    }
}