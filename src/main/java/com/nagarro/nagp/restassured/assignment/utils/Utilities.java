package com.nagarro.nagp.restassured.assignment.utils;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class Utilities {

	public static JsonPath rawToJson(Response res) {
		String resString = res.asString();
		JsonPath js = new JsonPath(resString);
		return js;

	}

}
