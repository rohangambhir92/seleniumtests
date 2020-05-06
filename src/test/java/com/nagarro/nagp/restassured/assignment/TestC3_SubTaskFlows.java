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

public class TestC3_SubTaskFlows extends BaseClass {
	static ExtentTest test;
	public static int listId;
	public static int revision;
	public static long taskId;
	public static int subtaskId;

	final static Logger Log = Logger.getLogger(TestC3_SubTaskFlows.class);

	@BeforeMethod
	public static void setup() {
		PropertyConfigurator.configure(System.getProperty("user.dir") + "\\log\\log4j.properties");
	}

	@BeforeClass
	public static void setupSubTasks() {
		RestAssured.baseURI = baseURI;
		Response res = given().log().headers().header("Content-Type", "application/json")
				.header("X-Client-ID", xClientID).header("X-Access-Token", accessToken).log().body()
				.body("{\r\n" + "  \"title\": \"TestR\"\r\n" + "}").when().post("/api/v1/lists").then().statusCode(201)
				.log().status().extract().response();

		JsonPath js = Utilities.rawToJson(res);
		listId = js.get("id.value");

		Response res2 = given().log().headers().header("Content-Type", "application/json")
				.header("X-Client-ID", xClientID).header("X-Access-Token", accessToken).log().body()
				.body("{\r\n" + "  \"list_id\":" + listId + ",\r\n" + "  \"title\": \"TaskR1\"\r\n" + "}").when()
				.post("/api/v1/tasks").then().statusCode(201).log().status().extract().response();

		JsonPath js2 = Utilities.rawToJson(res2);
		taskId = js2.get("id.value");

	}

	@Test(dataProvider = "DataProviderAddSubTask")
	public static void TC09_addSubTask(String subTaskName) {
		test = extent.createTest("testCase09_addSubTask");
		Log.info("Starting test for creating/adding a new sub-task with Rest Assured");
		RestAssured.baseURI = baseURI;
		Log.info("Post request for create sub-task | Extracting and storing the response in the Response object");
		Log.info("Sub-Task name is passed with data provider for this test");
		Response res = given().log().headers().header("Content-Type", "application/json")
				.header("X-Client-ID", xClientID).header("X-Access-Token", accessToken).log().body()
				.body("{\r\n" + "  \"task_id\": " + taskId + ",\r\n" + "  \"title\": \"" + subTaskName + "\",\r\n"
						+ "  \"completed\": false\r\n" + "}")
				.when().post("/api/v1/subtasks").then().statusCode(201).log().status().extract().response();

		Log.info("Converting the response into JsonPath object");
		JsonPath js = Utilities.rawToJson(res);
		Log.info("Fetching and storing the id of the newly created sub-task");
		subtaskId = js.get("id.value");
		Log.info("Fetching and storing the revision of the newly created sub-task");
		revision = js.get("revision.value");
		Log.info("Fetching and storing the title of the newly created sub-task");
		String subtaskTitle = js.get("title");
		Log.info("Asserting the expected title in the response");
		Assert.assertEquals(subTaskName, subtaskTitle);

	}

	@Test(dataProvider = "DataProviderUpdateSubTask")
	public static void TC10_updateSubTask(String subTaskNameUpdated) {
		test = extent.createTest("testCase10_updateSubTask");
		Log.info("Starting test for updating the newly created sub-task with Rest Assured");
		RestAssured.baseURI = baseURI;
		Log.info("Patch request for update sub-task | Extracting and storing the response in the Response object");
		Log.info("Updated sub-task name is passed with data provider for this test");
		Response res = given().log().headers().header("Content-Type", "application/json")
				.header("X-Client-ID", xClientID).header("X-Access-Token", accessToken).log().body()
				.body("{\r\n" + "  \"revision\":" + revision + ",\r\n" + "  \"title\": \"" + subTaskNameUpdated
						+ "\"\r\n" + "}")
				.when().patch("/api/v1/subtasks/" + subtaskId).then().statusCode(200).log().status().extract()
				.response();

		Log.info("Converting the response into JsonPath object");
		JsonPath js = Utilities.rawToJson(res);
		Log.info("Fetching and storing the title of the updated sub-task");
		String updatedTitle = js.get("title");
		Log.info("Fetching and storing the revision of the updated sub-task");
		revision = js.get("revision.value");
		Log.info("Asserting the expected title in the response");
		Assert.assertEquals(subTaskNameUpdated, updatedTitle);

	}

	@Test
	public static void TC12_getSubTasks() {
		test = extent.createTest("testCase12_getSubTasks");
		Log.info("Starting test for getting all the sub-tasks with Rest Assured");
		RestAssured.baseURI = baseURI;
		Log.info("Extracting and storing the response in the Response object");
		Log.info("Verifying the response code of the Get rquest");
		Response res = given().log().headers().header("Content-Type", "application/json")
				.header("X-Client-ID", xClientID).header("X-Access-Token", accessToken).when()
				.get("/api/v1/subtasks?task_id=" + taskId).then().statusCode(200).log().status().extract().response();

		Log.info("Printing the sub-tasks");
		System.out.println(res.asString());
	}

	@Test
	public static void TC11_deleteSubTask() {
		test = extent.createTest("testCase11_deleteSubTask");
		Log.info("Starting test for deleting the newly created and updated sub-task with Rest Assured");
		RestAssured.baseURI = baseURI;
		Log.info("Hitting with Delete request and verify the status code in the response");
		given().log().headers().header("Content-Type", "application/json").header("X-Client-ID", xClientID)
				.header("X-Access-Token", accessToken).when().delete("/api/v1/subtasks/" + subtaskId + "?revision=2")
				.then().statusCode(204).log().status();

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

	@DataProvider(name = "DataProviderAddSubTask")
	public Object[] getAddSubTaskDataFromDataprovider() {
		return new Object[] { "TestSubTask1" };
	}

	@DataProvider(name = "DataProviderUpdateSubTask")
	public Object[] getUpdateSubTaskDataFromDataprovider() {
		return new Object[] { "TestSubTask1Updated" };
	}

}
