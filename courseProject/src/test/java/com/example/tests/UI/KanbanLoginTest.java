package com.example.tests.UI;

import com.codeborne.selenide.Selenide;
import com.example.tests.BaseTest;
import com.example.tests.ConfigReader;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

// class for testing of login into the system
@Epic("Kanban App Tests")
@Feature("Login Functionality")
public class KanbanLoginTest extends BaseTest {
    //configuration from config.properties
    public static final ConfigReader configReader = new ConfigReader();
    private static final String BASE_URL = configReader.getLocalAppUrl();
    private static final String USERNAME = ConfigReader.getUsername();
    private static final String PASSWORD = ConfigReader.getPassword();
    //negative test with invalid data
    @Test(dataProvider = "invalidCredentials", groups = "login_tests")
    @Story("Incorrect Credentials")
    public void testIncorrectCredentials(String username, String password) {
        enterCredentials(username, password);
        // assert error message or check for an element indicating failure
    }
   // positive test with successful login
   @Test(groups = "login_tests", dependsOnMethods = "testIncorrectCredentials")
   @Story("Successful Login")
    public void testSuccessfulLogin() {
        enterCredentials(USERNAME, PASSWORD);
        assertSuccessfulLogin();
    }
    // method to verify successful login
    private void assertSuccessfulLogin() {
        // verify that title "Dashboard for admin" is displayed after successful login
        $("title").shouldHave(attribute("text", "Dashboard for admin"));
    }
    //method for entering of credentials
    private void enterCredentials(String username, String password) {
        open(BASE_URL);
        $("#username").setValue(username);
        $("#password").setValue(password);
        $("input[type='submit']").click();
    }
   // DataProvided for testing of invalid data during login
    @DataProvider(name = "invalidCredentials")
    public Object[][] invalidCredentials() {
        return new Object[][]{
                {"incorrect_username", PASSWORD},
                {USERNAME, "incorrect_password"},
        };
    }
    //Method for session ending - browser closing
    @AfterMethod(groups = "login_tests")
    public void tearDown() {
        // close the browser after the test
        Selenide.closeWebDriver();
    }
}
