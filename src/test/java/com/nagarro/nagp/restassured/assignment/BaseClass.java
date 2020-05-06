package com.nagarro.nagp.restassured.assignment;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

public class BaseClass {
	static ExtentHtmlReporter htmlReporter;
	static ExtentReports extent;
	static String accessToken = "8fc31dd16829f0aa328a4350828b88b7520a4f800a27bdfeb90813372f8e";
	static String baseURI = "https://a.wunderlist.com";
	static String xClientID = "82323e7d5be29b645e93";

	@BeforeSuite
	public void initializeSetup() {
		htmlReporter = new ExtentHtmlReporter(System.getProperty("user.dir") + "\\ExtentReportTestResults.html");
		extent = new ExtentReports();
		extent.attachReporter(htmlReporter);
		htmlReporter.config().setDocumentTitle("NAGP 2019 | Rest Assured Assignment");
		htmlReporter.config().setReportName("Extent Report Test Cases - Rest Assured");
		htmlReporter.config().setTheme(Theme.STANDARD);
	}

	@AfterSuite
	public void publishReport() {
		extent.flush();
	}

}
