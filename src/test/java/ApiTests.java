import dto.Agile;
import dto.Issue;
import dto.Project;
import dto.User;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utils.CsvDataProvider;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ApiTests {

    private String API_TOKEN;
    private String projectId;
    private List<String> issueId = new ArrayList<>();
    private String agileId;


    @BeforeClass
    public void setUp() throws IOException {

        Properties props = new Properties();
        FileInputStream fis = new FileInputStream("src/test/resources/config.properties");
        props.load(fis);

        API_TOKEN = props.getProperty("ApiToken");
        RestAssured.baseURI = "http://193.233.193.42:9091/api";
        RestAssured.authentication = RestAssured.oauth2(API_TOKEN);

    }

    @DataProvider(name = "issueDataProvider")
    public Object[][] issueDataProvider() {
        return CsvDataProvider.readCsvData("src/test/resources/testdata/issueData.csv");
    }

    @Test(dataProvider = "issueDataProvider", dependsOnMethods = "postProjectTest")
    public void postIssueTest(String summary, String description) {
        Project project = new Project();
        project.setId(projectId);

        Issue newIssue = new Issue();
        newIssue.setProject(project);
        newIssue.setSummary(summary);
        newIssue.setDescription(description);

        Response response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(newIssue)
                .when()
                .post("/issues");

        response.then()
                .log().all()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("$type", equalTo("Issue"));

        issueId.add(response.path("id"));
    }

    @Test
    public void getIssueTest() {

        given()
            .queryParam("fields", "id,summary,$type,customFields(name,id),description,project")
        .when()
            .get("/issues")
        .then()
            .log().all()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(
                    "schema/issues_schema.json"));
    }

    @Test
    public void postProjectTest() {
        User leader = new User();
        leader.setId("2-2");
        leader.setLogin("bronnikov_arseniy");

        Project project = new Project();
        project.setName("Api Project");
        project.setShortName("API");
        project.setLeader(leader);

        Response response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(project)
                .when()
                .post("/admin/projects");

        response.then()
                .log().all()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("$type", equalTo("Project"))
                .extract()
                .response();

        projectId = response.path("id");
    }

    @Test(dependsOnMethods = "postIssueTest")
    public void getIssueByIdTest() {
        given()
                .log().all()
                .queryParam("fields", "id,summary,description,$type,customFields(name,id),description,project")
                .when()
                .get(String.format("/issues/%s", issueId.getFirst()))
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", not(empty()))
                .body("id", notNullValue())
                .body("summary", notNullValue())
                .body("description", notNullValue())
                .body("$type", equalTo("Issue"))
                .body("customFields", not(empty()))
                .body("customFields[0].name", notNullValue())
                .body("customFields[0].$type", notNullValue());
    }

    @Test(dependsOnMethods = "postIssueTest")
    public void postIssueByIdTest() {
        Issue newIssue = new Issue();
        newIssue.setDescription("Changed description");
        newIssue.setSummary("Changed summary");

        Response response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .queryParam("fields", "id,summary,description")
                .body(newIssue)
                .when()
                .post(String.format("/issues/%s", issueId.getFirst()));

        response.then()
                .statusCode(200)
                .log().all()
                .body("id", notNullValue())
                .body("summary", equalTo("Changed summary"))
                .body("description", equalTo("Changed description"));
    }

    @Test(dependsOnMethods = "postIssueByIdTest")
    public void deleteIssueByIdTest() {
        given()
                .log().all()
                .when()
                .delete(String.format("/issues/%s", issueId.getFirst()))
                .then()
                .statusCode(200);
    }

    @Test(dependsOnMethods = "deleteIssueByIdTest")
    public void deleteProjectById() {
        given()
                .log().all()
                .when()
                .delete(String.format("admin/projects/%s", projectId))
                .then()
                .statusCode(200);
    }

    @Test
    public void getAgilesTest() {
        given()
                .log().all()
                .queryParam("fields", "id,name,owner,projects(id,shortName)," +
                        "columnSettings(columns($type,id), " +
                        "field($type,fieldType($type,id), name)),status($type,id,valid)")
                .when()
                .get("/agiles")
                .then()
                .statusCode(200)
                .body(JsonSchemaValidator
                        .matchesJsonSchemaInClasspath("schema/agiles_schema.json"));
    }

    @Test(dependsOnMethods = "postProjectTest")
    public void postAgilesTest() {
        User owner = new User();
        owner.setId("2-2");
        owner.setLogin("bronnikov_arseniy");
        owner.setType("User");

        Project project = new Project();
        project.setId(projectId);
        List<Project> projectList = new ArrayList<>();
        projectList.add(project);

        Agile agile = new Agile();
        agile.setName("Api agile");
        agile.setOwner(owner);
        agile.setProjects(projectList);

        Response response = given()
                .log().all()
                .queryParam("fields", "owner(id,login),id,name")
                .contentType(ContentType.JSON)
                .body(agile)
                .when()
                .post("/agiles");

        response.then()
                .log().all()
                .statusCode(200)
                .body("name", equalTo("Api agile"))
                .body("owner.login", equalTo(owner.getLogin()))
                .body("owner.id", equalTo(owner.getId()))
                .body("id", notNullValue());

        agileId = response.path("id");
    }

    @Test(dependsOnMethods = "postAgilesTest")
    public void deleteAgilesTest() {
        given()
                .when()
                .delete(String.format("/agiles/%s", agileId))
                .then()
                .log().all()
                .statusCode(200);
    }
    
}
