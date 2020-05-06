package com.nagarro.nagp.restassured.assignment;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
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
import static io.restassured.RestAssured.given;
import java.io.IOException;

public class TestC1_ListFlows extends BaseClass {

	static ExtentTest test;
	public static int listId;
	public static int revision;
	final static Logger Log = Logger.getLogger(TestC1_ListFlows.class);

	@BeforeMethod
	public static void setup() {
		PropertyConfigurator.configure(System.getProperty("user.dir") + "\\log\\log4j.properties");
	}

	@Test(dataProvider = "DataProviderAddList")
	public static void TC01_addList(String listName) {
		test = extent.createTest("testCase01_addList");
		Log.info("Starting test for creating/adding a new list with Rest Assured");
		RestAssured.baseURI = baseURI;
		Log.info("Post request for create list | Extracting and storing the response in the Response object");
		Log.info("List name is passed with data provider for this test");
		Response res = given().log().headers().header("Content-Type", "application/json")
				.header("X-Client-ID", xClientID).header("X-Access-Token", accessToken).log().body()
				.body("{\r\n" + "  \"title\": \"" + listName + "\"\r\n" + "}").when().post("/api/v1/lists").then()
				.statusCode(201).log().status().extract().response();
		Log.info("Converting the response into JsonPath object");
		JsonPath js = Utilities.rawToJson(res);
		Log.info("Fetching and storing the list id of the newly created list");
		listId = js.get("id.value");
		Log.info("Fetching and storig the revision of the newly created list");
		revision = js.get("revision.value");
		Log.info("Fetching and storing the title of the newly created list");
		String listTitle = js.get("title");
		Log.info("Asserting the expected title in the response");
		Assert.assertEquals(listName, listTitle);
	}

	@Test(dataProvider = "DataProviderUpdateList")
	public static void TC02_updateList(String listnameUpdated) {
		test = extent.createTest("testCase02_updateList");
		Log.info("Starting test for updating the newly created list with Rest Assured");
		RestAssured.baseURI = baseURI;
		Log.info("Patch request for update list | Extracting and storing the response in the Response object");
		Log.info("Updated List name is passed with data provider for this test");
		Response res = given().log().headers().header("Content-Type", "application/json")
				.header("X-Client-ID", xClientID).header("X-Access-Token", accessToken).log().body()
				.body("{\r\n" + "  \"revision\":" + revision + ",\r\n" + "  \"title\": \"" + listnameUpdated + "\"\r\n"
						+ "}")
				.when().patch("/api/v1/lists/" + listId).then().statusCode(200).log().status().extract().response();
		Log.info("Converting the response into JsonPath object");
		JsonPath js = Utilities.rawToJson(res);
		Log.info("Fetching and storing the revision of the updated list");
		revision = js.get("revision.value");
		Log.info("Fetching and storing the title of the updated list");
		String updatedTitle = js.get("title");
		Log.info("Asserting the expected title in the response");
		Assert.assertEquals(listnameUpdated, updatedTitle);

	}

	@Test
	public static void TC04_getLists() {
		test = extent.createTest("testCase04_getLists");
		Log.info("Starting test for getting all the lists with Rest Assured");
		RestAssured.baseURI = baseURI;
		Log.info("Extracting and storing the response in the Response object");
		Log.info("Verifying the response code of the Get rquest");
		Response res = given().log().headers().header("Content-Type", "application/json")
				.header("X-Client-ID", xClientID).header("X-Access-Token", accessToken).when().get("/api/v1/lists")
				.then().statusCode(200).log().status().extract().response();

		Log.info("Printing the lists");
		System.out.println(res.asString());

	}

	@Test
	public static void TC03_deleteList() {
		test = extent.createTest("testCase03_deleteList");
		Log.info("Starting test for deleting the newly created and updated list with Rest Assured");
		RestAssured.baseURI = baseURI;
		Log.info("Hitting with Delete request and verify the status code in the response");
		given().log().headers().header("Content-Type", "application/json").header("X-Client-ID", xClientID)
				.header("X-Access-Token", accessToken).when()
				.delete("/api/v1/lists/" + listId + "?revision=" + revision).then().statusCode(204).log().status();
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

	@DataProvider(name = "DataProviderAddList")
	public Object[] getAddListDataFromDataprovider() {
		return new Object[] { "TestList1" };
	}

	@DataProvider(name = "DataProviderUpdateList")
	public Object[] getUpdateListDataFromDataprovider() {
		return new Object[] { "TestList1Updated" };
	}

}
