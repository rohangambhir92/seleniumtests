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

public class TestC4_TaskCommentFlows extends BaseClass {
	static ExtentTest test;
	public static int listId;
	public static long taskId;
	public static int taskCommentId;

	final static Logger Log = Logger.getLogger(TestC4_TaskCommentFlows.class);

	@BeforeMethod
	public static void setup() {
		PropertyConfigurator.configure(System.getProperty("user.dir") + "\\log\\log4j.properties");
	}

	@BeforeClass
	public static void setupTaskComments() {
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

	@Test(dataProvider = "DataProviderAddComment")
	public static void TC13_addTaskComment(String taskComment) {
		test = extent.createTest("testCase13_addTaskComment");
		Log.info("Starting test for creating/adding a new task comment with Rest Assured");
		RestAssured.baseURI = baseURI;
		Log.info("Post request for create task comment | Extracting and storing the response in the Response object");
		Log.info("Task comment is passed with data provider for this test");
		Response res = given().log().headers().header("Content-Type", "application/json")
				.header("X-Client-ID", xClientID).header("X-Access-Token", accessToken).log().body()
				.body("{\r\n" + "  \"task_id\":" + taskId + ",\r\n" + "  \"text\": \"" + taskComment + "\"\r\n" + "}")
				.when().post("/api/v1/task_comments").then().statusCode(201).log().status().extract().response();

		Log.info("Converting the response into JsonPath object");
		JsonPath js = Utilities.rawToJson(res);
		Log.info("Fetching and storing the id of the newly created task comment");
		taskCommentId = js.get("id.value");
		Log.info("Fetching and storing the text of the newly created task comment");
		String commentText = js.get("text");
		Log.info("Asserting the expected comment in the response");
		Assert.assertEquals(taskComment, commentText);

	}

	@Test
	public static void TC14_getSpecificComment() {
		test = extent.createTest("testCase14_getSpecificComment");
		Log.info("Starting test for getting a specific comment with Rest Assured");
		RestAssured.baseURI = baseURI;
		Log.info("Extracting and storing the response in the Response object");
		Log.info("Verifying the response code of the Get rquest");
		Response res = given().log().headers().header("Content-Type", "application/json")
				.header("X-Client-ID", xClientID).header("X-Access-Token", accessToken).when()
				.get("/api/v1/task_comments/" + taskCommentId).then().statusCode(200).log().status().extract()
				.response();

		Log.info("Printing the task comment");
		System.out.println(res.asString());

	}

	@Test
	public static void TC15_getCommentsFromList() {
		test = extent.createTest("testCase15_getCommentsFromList");
		Log.info("Starting test for getting all comments from a list with Rest Assured");
		RestAssured.baseURI = baseURI;
		Log.info("Extracting and storing the response in the Response object");
		Log.info("Verifying the response code of the Get rquest");
		Response res = given().log().headers().header("Content-Type", "application/json")
				.header("X-Client-ID", xClientID).header("X-Access-Token", accessToken).when()
				.get("/api/v1/task_comments?list_id=" + listId).then().statusCode(200).log().status().extract()
				.response();

		Log.info("Printing the task comments");
		System.out.println(res.asString());

	}

	@Test
	public static void TC16_getCommentsFromTask() {
		test = extent.createTest("testCase16_getCommentsFromTask");
		Log.info("Starting test for getting all comments from a task with Rest Assured");
		RestAssured.baseURI = baseURI;
		Log.info("Extracting and storing the response in the Response object");
		Log.info("Verifying the response code of the Get rquest");
		Response res = given().log().headers().header("Content-Type", "application/json")
				.header("X-Client-ID", xClientID).header("X-Access-Token", accessToken).when()
				.get("/api/v1/task_comments?task_id=" + taskId).then().statusCode(200).log().status().extract()
				.response();

		Log.info("Printing the task comments");
		System.out.println(res.asString());

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

	@DataProvider(name = "DataProviderAddComment")
	public Object[] getTaskCommentDataFromDataprovider() {
		return new Object[] { "Comment_RA" };
	}

}
