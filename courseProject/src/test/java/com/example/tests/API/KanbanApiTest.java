package com.example.tests.API;

import com.codeborne.selenide.Selenide;
import com.example.tests.ConfigReader;
import com.example.tests.DockerConfig;
import com.example.tests.KanbanApiHelper;
import com.example.tests.UI.KanbanLoginTest;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.WaitContainerResultCallback;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.matcher.ResponseAwareMatcher;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;
import java.util.Base64;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.example.tests.BaseTest.BASE_URL;
import static com.google.common.base.Predicates.equalTo;
import static org.testng.Assert.assertNotNull;

@Epic("Kanban App API Tests")
public class KanbanApiTest {
    private static final Logger log = LoggerFactory.getLogger(KanbanApiTest.class);

    private static final String API_AUTH_HEADER = "X-API-Auth";
    private static final String CONFIG_USERNAME = ConfigReader.getApiUsername();
    private static final String CONFIG_API_TOKEN = ConfigReader.getApiToken();
    private static final String API_URL = ConfigReader.getLocalAppUrl();

    private DockerClient dockerClient;
    private String containerId;
    private String createdProjectId;
    private String createdTaskId;
    private String createdUserId;
    private static String getAuthorizationHeader() {
        String credentials = CONFIG_USERNAME + ":" + CONFIG_API_TOKEN;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
        return "Basic " + encodedCredentials;
    }

    private void openAppAndLogin() {
        Selenide.open(BASE_URL);
        login();
    }
    private void login() {
        KanbanLoginTest kanbanLoginTest = new KanbanLoginTest();
        kanbanLoginTest.setUp();
        kanbanLoginTest.testSuccessfulLogin();
    }
    private void setUpTests(String appType) {
        String appUrl = "local".equals(appType) ? ConfigReader.getLocalAppUrl() : ConfigReader.getRemoteAppUrl();
        setUpContainer("kanboard-1", "80:80", appUrl);
    }

    @BeforeGroups(groups = "api_tests")
    public void setUpApiTests() {
        openAppAndLogin();
        setUpTests(System.getProperty("appType", "local"));
    }
    private void deleteApiObject(String objectId) {
        try {
            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .header(API_AUTH_HEADER, getAuthorizationHeader())
                    .pathParam("id", objectId)
                    .delete(API_URL + "/jsonrpc.php")
                    .then()
                    .statusCode(200);

            log.info("Object with ID {} deleted.", objectId);
        } catch (Exception e) {
            log.error("Error deleting object with ID {}: {}", objectId, e.getMessage());
        }
    }
    @Test
    @Feature("API Functionality")
    @Story("Create and Delete User")
    @Description("Test API functionality for creating and deleting a user")
    public void testCreateAndDeleteUserForApi() {
        String endpoint = "/jsonrpc.php";

        // Create user
        String createUserRequest = "{ " +
                "\"jsonrpc\": \"2.0\", " +
                "\"method\": \"createUser\", " +
                "\"id\": " + System.currentTimeMillis() + ", " +
                "\"params\": { " +
                "\"username\": \"testapi\", " +
                "\"password\": \"123456\", " +
                "\"name\": \"Test User\", " +
                "\"email\": \"alyonastadnick@gmail.com\", " +
                "\"role\": \"app-user\" " +
                "} " +
                "}";

        int userId = RestAssured.given()
                .contentType(ContentType.JSON)
                .header(API_AUTH_HEADER, getAuthorizationHeader())
                .body(createUserRequest)
                .post(API_URL + endpoint)
                .then()
                .statusCode(200)
                .extract().path("result");
        createdUserId = String.valueOf(userId);

        // Delete user
        deleteApiObject(String.valueOf(userId));
    }
    @Test
    @Feature("API Functionality")
    @Story("Create and Delete Project")
    @Description("Test API functionality for creating and deleting a project")
    public void testCreateAndDeleteProjectForApi() {
        String projectName = "API Project";

        // Check if project with the same name already exists
        int existingProjectId = KanbanApiHelper.getProjectIdByName(projectName);
        if (existingProjectId != 0) {
            // If project exists, delete it to start with a clean slate
            KanbanApiHelper.removeProject(existingProjectId);
        }

        // Create a new project
        int projectId = KanbanApiHelper.createProject(projectName, true);
        assertNotNull(projectId, "Failed to create a new project");

        // Delete the created project
        KanbanApiHelper.removeProject(projectId);
    }
    @Test
    @Feature("API Functionality")
    @Story("Create and Delete Task")
    @Description("Test API functionality for creating and deleting a task")
    public void testCreateAndDeleteTaskForApi() {
        String endpoint = "/api/tasks";
        String taskData = "{ \"title\": \"API Task\", \"description\": \"Test task created via API\" }";

        int taskId = RestAssured.given()
                .contentType(ContentType.JSON)
                .header(API_AUTH_HEADER, getAuthorizationHeader())
                .body(taskData)
                .post(API_URL + endpoint)
                .then()
                .statusCode(200)
                .extract().path("id");
        createdTaskId = String.valueOf(taskId);

        // Delete task
//        deleteApiObject(createdTaskId);
    }
    public static void removeTask(int taskId) {
        // Ваш код для видалення завдання за допомогою API
        String endpoint = "/jsonrpc.php";
        String removeTaskRequest = "{ " +
                "\"jsonrpc\": \"2.0\", " +
                "\"method\": \"removeTask\", " +
                "\"id\": " + System.currentTimeMillis() + ", " +
                "\"params\": { " +
                "\"task_id\": " + taskId + " " +
                "} " +
                "}";

        RestAssured.given()
                .contentType(ContentType.JSON)
                .header(API_AUTH_HEADER, getAuthorizationHeader())
                .body(removeTaskRequest)
                .post(API_URL + endpoint)
                .then()
                .statusCode(200)
                .body("result", (ResponseAwareMatcher<Response>) equalTo(true));
    }
    public static String createNewTaskAndGetId() {
        String endpoint = "/api/tasks";
        String taskData = "{ \"title\": \"New API Task\", \"description\": \"Test task created via API\" }";

        // Sending a request to create a new task
        int taskId = RestAssured.given()
                .contentType(ContentType.JSON)
                .header(API_AUTH_HEADER, getAuthorizationHeader())
                .body(taskData)
                .post(API_URL + endpoint)
                .then()
                .statusCode(200)
                .extract().path("id");

        return String.valueOf(taskId);
    }
    @Test(groups = "api_tests")
    @Feature("API Functionality")
    @Story("Add Comment to Task")
    @Description("Test API functionality for adding a comment to a task")
    public void testAddCommentToTaskForApi() {
        String endpoint = "/jsonrpc.php";
        String taskId = createNewTaskAndGetId();
        log.info("New Task ID: {}", taskId);

        String commentData = "{\n" +
                "    \"jsonrpc\": \"2.0\",\n" +
                "    \"method\": \"createComment\",\n" +
                "    \"id\": " + System.currentTimeMillis() + ",\n" +
                "    \"params\": {\n" +
                "        \"task_id\": " + taskId + ",\n" +
                "        \"user_id\": 1,\n" +
                "        \"content\": \"This is a test comment.\"\n" +
                "    }\n" +
                "}";

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(commentData)
                .post(API_URL + endpoint)
                .then()
                .statusCode(200); // Assuming comment creation returns 200 (OK)

        // Close task
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header(API_AUTH_HEADER, getAuthorizationHeader())
                .pathParam("id", taskId)
                .post(API_URL + endpoint + "/{id}/close")
                .then()
                .statusCode(200); // Assuming task closure returns 200 (OK)
    }

    @AfterMethod(groups = "api_tests")
    public void cleanup() {
        log.info("Cleaning up...");

        // Delete user
        if (createdUserId != null) {
            deleteApiObject(createdUserId);
        }

        // Delete project
        if (createdProjectId != null) {
            deleteApiObject(createdProjectId);
        }

        // Delete task
        if (createdTaskId != null) {
            deleteApiObject(createdTaskId);
        }
        tearDownContainer();
        log.info("Cleanup complete.");
    }

    private void setUpContainer(String imageName, String portBinding, String appUrl) {
        try {
            DockerClient dockerClient = DockerConfig.getDockerClient();

            String[] ports = portBinding.split(":");

            Ports.Binding hostBinding = Ports.Binding.bindPort(Integer.parseInt(ports[0]));
            Ports.Binding containerBinding = Ports.Binding.bindPort(Integer.parseInt(ports[1]));

            Ports portBindings = new Ports();
            portBindings.bind(ExposedPort.tcp(Integer.parseInt(ports[1])), hostBinding);

            CreateContainerResponse containerResponse = dockerClient.createContainerCmd(imageName)
                    .withHostConfig(
                            HostConfig.newHostConfig()
                                    .withPortBindings(portBindings)
                    )
                    .exec();

            dockerClient.startContainerCmd(containerResponse.getId()).exec();
            containerId = containerResponse.getId();
        } catch (Exception e) {
            log.error("Error setting up the Docker container: {}", e.getMessage(), e);
        }
    }


    private void tearDownContainer() {
        if (dockerClient != null) {
            dockerClient.waitContainerCmd(containerId).exec(new WaitContainerResultCallback()).awaitStatusCode();
            dockerClient.removeContainerCmd(containerId).exec();
        }
    }
}

