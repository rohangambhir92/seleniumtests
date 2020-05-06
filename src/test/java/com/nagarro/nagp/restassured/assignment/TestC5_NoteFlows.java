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

public class TestC5_NoteFlows extends BaseClass {
	static ExtentTest test;
	public static int listId;
	public static long taskId;
	public static int noteId;
	public static int revision;

	final static Logger Log = Logger.getLogger(TestC5_NoteFlows.class);

	@BeforeMethod
	public static void setup() {
		PropertyConfigurator.configure(System.getProperty("user.dir") + "\\log\\log4j.properties");
	}

	@BeforeClass
	public static void setupNotes() {
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

	@Test(dataProvider = "DataProviderAddNote")
	public static void TC17_addNote(String note) {
		test = extent.createTest("testCase17_addNote");
		Log.info("Starting test for creating/adding a new note with Rest Assured");
		RestAssured.baseURI = baseURI;
		Log.info("Post request for create note | Extracting and storing the response in the Response object");
		Log.info("Note is passed with data provider for this test");
		Response res = given().log().headers().header("Content-Type", "application/json")
				.header("X-Client-ID", xClientID).header("X-Access-Token", accessToken).log().body()
				.body("{\r\n" + "  \"task_id\":" + taskId + ",\r\n" + "  \"content\": \"" + note + "\"\r\n" + "}")
				.when().post("/api/v1/notes").then().statusCode(201).log().status().extract().response();

		Log.info("Converting the response into JsonPath object");
		JsonPath js = Utilities.rawToJson(res);
		Log.info("Fetching and storing the id of the newly created note");
		noteId = js.get("id.value");
		Log.info("Fetching and storing the revision of the newly created note");
		revision = js.get("revision.value");
		Log.info("Fetching and storing the content of the newly created note");
		String noteContent = js.get("content");
		Log.info("Asserting the expected note content in the response");
		Assert.assertEquals(note, noteContent);

	}

	@Test(dataProvider = "DataProviderUpdateNote")
	public static void TC18_updateNote(String noteUpdated) {
		test = extent.createTest("testCase18_updateNote");
		Log.info("Starting test for updating the newly note with Rest Assured");
		RestAssured.baseURI = baseURI;
		Log.info("Patch request for update note | Extracting and storing the response in the Response object");
		Log.info("Updated note content is passed with data provider for this test");
		Response res = given().log().headers().header("Content-Type", "application/json")
				.header("X-Client-ID", xClientID).header("X-Access-Token", accessToken).log().body()
				.body("{\r\n" + "  \"revision\":" + revision + ",\r\n" + "  \"content\": \"" + noteUpdated + "\"\r\n"
						+ "}")
				.when().patch("/api/v1/notes/" + noteId).then().statusCode(200).log().status().extract().response();

		Log.info("Converting the response into JsonPath object");
		JsonPath js = Utilities.rawToJson(res);
		Log.info("Fetching and storing the revision of the updated note");
		revision = js.get("revision.value");
		Log.info("Fetching and storing the content of the updated note");
		String updatedContent = js.get("content");
		Log.info("Asserting the expected note content in the response");
		Assert.assertEquals(noteUpdated, updatedContent);

	}

	@Test
	public static void TC19_getNotesFromList() {
		test = extent.createTest("testCase19_getNotesFromList");
		Log.info("Starting test for getting all notes from a list with Rest Assured");
		RestAssured.baseURI = baseURI;
		Log.info("Extracting and storing the response in the Response object");
		Log.info("Verifying the response code of the Get rquest");
		Response res = given().log().headers().header("Content-Type", "application/json")
				.header("X-Client-ID", xClientID).header("X-Access-Token", accessToken).when()
				.get("/api/v1/notes?list_id=" + listId).then().statusCode(200).log().status().extract().response();

		Log.info("Printing the notes");
		System.out.println(res.asString());

	}

	@Test
	public static void TC20_getNotesFromTask() {
		test = extent.createTest("testCase20_getNotesFromTask");
		Log.info("Starting test for getting all notes from a task with Rest Assured");
		RestAssured.baseURI = baseURI;
		Log.info("Extracting and storing the response in the Response object");
		Log.info("Verifying the response code of the Get rquest");
		Response res = given().log().headers().header("Content-Type", "application/json")
				.header("X-Client-ID", xClientID).header("X-Access-Token", accessToken).when()
				.get("/api/v1/notes?task_id=" + taskId).then().statusCode(200).log().status().extract().response();

		Log.info("Printing the notes");
		System.out.println(res.asString());

	}

	@Test
	public static void TC21_deleteNote() {
		test = extent.createTest("testCase21_deleteNote");
		Log.info("Starting test for deleting the newly created and updated note with Rest Assured");
		RestAssured.baseURI = baseURI;
		Log.info("Hitting with Delete request and verify the status code in the response");
		given().log().headers().header("Content-Type", "application/json").header("X-Client-ID", xClientID)
				.header("X-Access-Token", accessToken).when()
				.delete("/api/v1/notes/" + noteId + "?revision=" + revision).then().statusCode(204).log().status()
				.extract().response();

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

	@DataProvider(name = "DataProviderAddNote")
	public Object[] getAddNoteDataFromDataprovider() {
		return new Object[] { "Note_RA" };
	}

	@DataProvider(name = "DataProviderUpdateNote")
	public Object[] getUpdateNoteDataFromDataprovider() {
		return new Object[] { "Note_RAUpdated" };
	}

}
