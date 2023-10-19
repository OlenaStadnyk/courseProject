package com.example.tests;

import com.codeborne.selenide.Selenide;
import com.example.exceptions.DockerCloseException;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import io.cucumber.java.Before;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;

import java.io.IOException;

import static com.codeborne.selenide.Selenide.$;
import static com.example.tests.UI.KanbanLoginTest.configReader;

public class BaseTest {

    protected static DockerClient dockerClient;
    protected static String containerId;
    public static final String BASE_URL = configReader.getLocalAppUrl();

    protected static DockerClient getDockerClient() {
        return dockerClient;
    }

    protected static void setDockerClient(DockerClient client) {
        dockerClient = client;
    }

    protected static String getContainerId() {
        return containerId;
    }

    protected static void setContainerId(String id) {
        containerId = id;
    }

    @Before
    public static void setUpDocker() {
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        dockerClient = DockerClientBuilder.getInstance(config).build();
    }

    @Before
    public void setUp() {
        Selenide.open(BASE_URL);
        login();
    }

    protected void login() {
        login(ConfigReader.getUsername(), ConfigReader.getPassword());
    }

    protected void login(String username, String password) {
        $("#username").setValue(username);
        $("#password").setValue(password);
        $("input[type='submit']").click();
    }

    // End of test
    @AfterMethod
    public void tearDown() throws DockerCloseException {
        // close the browser after the test
        closeBrowser();

        // Close Docker client
        if (dockerClient != null) {
            try {
                dockerClient.close();
            } catch (IOException e) {
                throw new DockerCloseException("Error closing Docker client", e);
            }
        }
    }

    protected void closeBrowser() {
        Selenide.closeWebDriver();
    }

}
