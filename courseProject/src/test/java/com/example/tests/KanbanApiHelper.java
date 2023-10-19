package com.example.tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class KanbanApiHelper {

    private static final String API_AUTH_HEADER = "X-API-Auth";
    private static final String API_URL = ConfigReader.getLocalAppUrl();

    public static int getProjectIdByName(String projectName) {
        // Ваш код для отримання ID проекту за назвою через API
        String endpoint = "/jsonrpc.php";
        String getProjectByNameRequest = "{ " +
                "\"jsonrpc\": \"2.0\", " +
                "\"method\": \"getProjectByName\", " +
                "\"id\": " + System.currentTimeMillis() + ", " +
                "\"params\": { " +
                "\"name\": \"" + projectName + "\" " +
                "} " +
                "}";

        int projectId = RestAssured.given()
                .contentType(ContentType.JSON)
                .header(API_AUTH_HEADER, getAuthorizationHeader())
                .body(getProjectByNameRequest)
                .post(API_URL + endpoint)
                .then()
                .statusCode(200)
                .extract().path("result.id");

        return projectId;
    }

    public static int createProject(String projectName, boolean isActive) {
        // code for project creation via API
        // ...
        return 0;
    }

    public static void removeProject(int projectId) {
        // code for project deleting via API
        // ...
    }

    public static int getTaskIdByTitle(String title) {
        // get taskID via API
        // ...
        return 0;
    }

    public static int createTask(String title, String description) {
        // create task via API
        // ...
        return 0;
    }
    public static int getOrCreateProjectId(String projectName) {
        int existingProjectId = getProjectIdByName(projectName);
        return existingProjectId != 0 ? existingProjectId : createProject(projectName, true);
    }

    private static String getAuthorizationHeader() {
        // get authorization title
        // ...
        return "Bearer YOUR_ACCESS_TOKEN";
    }
}
