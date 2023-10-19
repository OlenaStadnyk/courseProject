package com.example.tests.UI;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.example.tests.API.KanbanApiTest;
import com.example.tests.BaseTest;
import com.example.tests.KanbanApiHelper;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.command.WaitContainerResultCallback;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import static org.testng.Assert.assertNotNull;

@Epic("Kanban App Tests")
public class KanbanAppTest extends BaseTest {
    public static final Logger log = LoggerFactory.getLogger(KanbanAppTest.class);
    private static final String CREATE_PROJECT_LINK_SELECTOR = "a[href='/project/create'][class='js-modal-medium'][title='New project']";
    private AtomicInteger baseProjectId = new AtomicInteger(100);
    private AtomicInteger baseTaskId = new AtomicInteger(500);

    @BeforeClass
    @Parameters({"browser"})
    public void setUp(@Optional("chrome") String browser) {
        setBrowser(browser);
        openAppAndLogin();
    }

    private static void setBrowser(String browser) {
        Configuration.browser = browser;
    }

    private void openAppAndLogin() {
        Selenide.open(BASE_URL);
        login();
    }

    @Test(groups = "ui_tests")
    @Description("Creating a project and verifying its details")
    public void createProject() {
        String projectName = "Project One";
        String projectIdentifier = "MYPROJECTONE";

        $(By.cssSelector(CREATE_PROJECT_LINK_SELECTOR)).click();
        $("#form-name").setValue(projectName);
        $("#form-identifier").setValue(projectIdentifier);
        $("[type='submit']").pressEnter();
        // Wait for the success message to appear
        waitForSuccessMessage("Your project has been created successfully.");
    }

    @Test(groups = "ui_tests", dependsOnMethods = "createProject")
    @Description("Creating and closing a task")
    public void createAndCloseTaskUI() {
        String projectName = "Project One";

        int projectId = KanbanApiHelper.getProjectIdByName(projectName);
        if (projectId == 0) {
            projectId = KanbanApiHelper.createProject(projectName, true);
            assertNotNull(projectId, "Failed to create a new project");
        }

        int taskId = Integer.parseInt(KanbanApiTest.createNewTaskAndGetId());

        openUIAndCloseTask(taskId);
        KanbanApiTest.removeTask(taskId);

        verifyTaskIsClosed(taskId);
    }

    private void verifyTaskIsClosed(int taskId) {
        String taskClosedSelector = "#task-summary-" + taskId + " .task-closed";
        $(taskClosedSelector).shouldBe(visible);
        waitForSuccessMessage("Task closed successfully.");
    }

    private void verifyCommentAddedToTask(int taskId, String comment) {
        String commentSelector = "#task-summary-" + taskId + " .comment-text";
        $(commentSelector).shouldBe(visible);
        $(commentSelector).shouldHave(text(comment));
        waitForSuccessMessage("Comment added successfully");
    }

    private void waitForSuccessMessage(String message) {
        $(".alert-success").shouldBe(visible, Duration.ofSeconds(2));
        $(".alert-success").shouldHave(text(message));
        $(".alert-success").should(disappear);
    }

    private void openUIAndCloseTask(int taskId) {
        $(By.id("task-summary-" + taskId)).click();
        $("#close-this-task").click();
        $(".flash.flash_notice").shouldHave(text("Close a task"));
        $("#confirm-yes-button").click();
        waitForSuccessMessage("Task closed successfully.");
    }

    @Test(groups = "ui_tests", dependsOnMethods = "createAndCloseTaskUI")
    @Description("Adding a comment to a task")
    public void addCommentToTaskUI() {
        int projectId = getOrCreateProjectId("Project One");
        int taskId = getOrCreateTaskId("Task Title", "Task Description");

        openUIAndAddComment(taskId, "This is a test comment.");
        verifyCommentAddedToTask(taskId, "This is a test comment.");
    }

    private void openUIAndAddComment(int taskId, String comment) {
        $(By.id("task-summary-" + taskId)).click();
        $("#add-a-comment").click();
        $("#modal-content").shouldBe(visible);
        $("#comment-content").setValue(comment);
        $("#comment-add-form").find("[type='submit']").click();
        waitForSuccessMessage("Comment added successfully");
    }
    @AfterClass
    public void tearDown() {
        tearDownContainer();
    }
    private int getOrCreateProjectId(String projectName) {
        int existingProjectId = KanbanApiTest.getProjectIdByName(projectName);
        return existingProjectId != 0 ? existingProjectId : KanbanApiTest.createProject(projectName, true);
    }
    private int getOrCreateTaskId(String title, String description) {
        int existingTaskId = KanbanApiTest.getTaskIdByTitle(title);
        return existingTaskId != 0 ? existingTaskId : KanbanApiTest.createTask(title, description);
    }
    private void setUpContainer(String imageName, String portBinding, String appUrl) {
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
    }

    private void tearDownContainer() {
        dockerClient.waitContainerCmd(containerId).exec(new WaitContainerResultCallback()).awaitStatusCode();
        dockerClient.removeContainerCmd(containerId).exec();
    }
}
