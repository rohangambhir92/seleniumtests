package com.nagarro.nagp.restassured.assignment;

import static io.restassured.RestAssured.given;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.nagarro.nagp.restassured.assignment.utils.Utilities;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class TestC2_TaskFlows extends BaseClass {
	static ExtentTest test;
	public static int listId;
	public static int revision;
	public static long taskId;

	final static Logger Log = Logger.getLogger(TestC2_TaskFlows.class);

	@BeforeMethod
	public static void setup() {
		PropertyConfigurator.configure(System.getProperty("user.dir") + "\\log\\log4j.properties");
	}

	@BeforeClass
	public static void setupTasks() {
		RestAssured.baseURI = baseURI;
		Response res = given().log().headers().header("Content-Type", "application/json")
				.header("X-Client-ID", xClientID).header("X-Access-Token", accessToken).log().body()
				.body("{\r\n" + "  \"title\": \"TestR\"\r\n" + "}").when().post("/api/v1/lists").then().statusCode(201)
				.log().status().extract().response();

		JsonPath js = Utilities.rawToJson(res);
		listId = js.get("id.value");
	}

	@Test(dataProvider = "DataProviderAddTask")
	public static void TC05_addTask(String taskName) {
		test = extent.createTest("testCase05_addTask");
		Log.info("Starting test for creating/adding a new task with Rest Assured");
		RestAssured.baseURI = baseURI;
		Log.info("Post request for create task | Extracting and storing the response in the Response object");
		Log.info("Task name is passed with data provider for this test");
		Response res = given().log().headers().header("Content-Type", "application/json")
				.header("X-Client-ID", xClientID).header("X-Access-Token", accessToken).log().body()
				.body("{\r\n" + "  \"list_id\":" + listId + ",\r\n" + "  \"title\": \"" + taskName + "\"\r\n" + "}")
				.when().post("/api/v1/tasks").then().statusCode(201).log().status().extract().response();

		Log.info("Converting the response into JsonPath object");
		JsonPath js = Utilities.rawToJson(res);
		Log.info("Fetching and storing the task id of the newly created task");
		taskId = js.get("id.value");
		Log.info("Fetching and storing the revision of the newly created task");
		revision = js.get("revision.value");
		Log.info("Fetching and storing the title of the newly created task");
		String taskTitle = js.get("title");
		Log.info("Asserting the expected title in the response");
		Assert.assertEquals(taskName, taskTitle);

	}

	@Test(dataProvider = "DataProviderUpdateTask")
	public static void TC06_updateTask(String taskNameUpdated) {
		test = extent.createTest("testCase06_updateTask");
		Log.info("Starting test for updating the newly created task with Rest Assured");
		RestAssured.baseURI = baseURI;
		Log.info("Patch request for update task | Extracting and storing the response in the Response object");
		Log.info("Updated task name is passed with data provider for this test");
		Response res = given().log().headers().header("Content-Type", "application/json")
				.header("X-Client-ID", xClientID).header("X-Access-Token", accessToken).log().body()
				.body("{\r\n" + "  \"revision\":" + revision + ",\r\n" + "  \"title\": \"" + taskNameUpdated + "\"\r\n"
						+ "}")
				.when().patch("/api/v1/tasks/" + taskId).then().statusCode(200).log().status().extract().response();
		Log.info("Converting the response into JsonPath object");
		JsonPath js = Utilities.rawToJson(res);
		Log.info("Fetching and storing the revision of the updated task");
		revision = js.get("revision.value");
		Log.info("Fetching and storing the title of the updated task");
		String updatedTitle = js.get("title");
		Log.info("Asserting the expected title in the response");
		Assert.assertEquals(taskNameUpdated, updatedTitle);

	}

	@Test
	public static void TC08_getTasks() {
		test = extent.createTest("testCase08_getTasks");
		Log.info("Starting test for getting all the tasks with Rest Assured");
		RestAssured.baseURI = baseURI;
		Log.info("Extracting and storing the response in the Response object");
		Log.info("Verifying the response code of the Get rquest");
		Response res = given().log().headers().header("Content-Type", "application/json")
				.header("X-Client-ID", xClientID).header("X-Access-Token", accessToken).when()
				.get("/api/v1/tasks?list_id=" + listId).then().statusCode(200).log().status().extract().response();

		Log.info("Printing the tasks");
		System.out.println(res.asString());
	}

	@Test
	public static void TC07_deleteTask() {
		test = extent.createTest("testCase07_deleteTask");
		Log.info("Starting test for deleting the newly created and updated task with Rest Assured");
		RestAssured.baseURI = baseURI;
		Log.info("Hitting with Delete request and verify the status code in the response");
		given().log().headers().header("Content-Type", "application/json").header("X-Client-ID", xClientID)
				.header("X-Access-Token", accessToken).when()
				.delete("/api/v1/tasks/" + taskId + "?revision=" + revision).then().statusCode(204).log().status();

	}

	@AfterMethod
	public void getResult(ITestResult result) throws IOException {

		if (result.getStatus() == ITestResult.FAILURE) {
			Log.info(
					"Marking the status as FAIL for the failed test and setting the color to Red in the extent report");
			test.log(Status.FAIL, MarkupHelper.createLabel(result.getName() + " FAILED ", ExtentColor.RED));
			test.fail(result.getThrowable());
		} else if (result.getStatus() == ITestResult.SUCCESS) {
			Log.info(
					"Marking the status as PASS for the passed test and setting the color to Green in the extent report");
			test.log(Status.PASS, MarkupHelper.createLabel(result.getName() + " PASSED ", ExtentColor.GREEN));
		} else {
			Log.info(
					"Marking the status as SKIP for the skipped test and setting the color to Orange in the extent report");
			test.log(Status.SKIP, MarkupHelper.createLabel(result.getName() + " SKIPPED ", ExtentColor.ORANGE));
			test.skip(result.getThrowable());
		}
	}

	@DataProvider(name = "DataProviderAddTask")
	public Object[] getAddTaskDataFromDataProvider() {
		return new Object[] { "TestTask1" };
	}

	@DataProvider(name = "DataProviderUpdateTask")
	public Object[] getUpdateTaskDataFromDataProvider() {
		return new Object[] { "TestTask1Updated" };
	}

}
