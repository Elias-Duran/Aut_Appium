package Proyectos.Pages;

import com.automation.pages.BasePage;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class LoginPage extends BasePage {

    private final By rut = AppiumBy.accessibilityId("test-Username");
    private final By pass = AppiumBy.accessibilityId("test-Password");
    private final By btnIngresar = AppiumBy.accessibilityId("test-LOGIN");

    public WebElement rutElement() {
        return waitForElementVisible(rut);
    }

    public WebElement passElement() {
        return waitForElementVisible(pass);
    }

    public void clickIngresar() {
        click(btnIngresar);
    }
    public void ingresarUsuario(String usuario) {
        rutElement().sendKeys(usuario);
    }
    public void ingresarPassword(String passWord) {
        passElement().sendKeys(passWord);
    }
}