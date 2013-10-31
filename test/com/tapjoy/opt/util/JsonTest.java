package com.tapjoy.opt.util;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

public class JsonTest {

	@Test
	public void readJsonString() {
		JSONParser parser = new JSONParser();
		 
		try {
			Object obj = parser.parse(new StringReader("{\r\n        \"key\":\"101.1.iOS.US..itouch\",\r\n  " +
					"      \"enabled\":\"true\",\r\n        \"offers\":\r\n        [\r\n      " +
					"{\"rank_index\":\"1\",  \"offer_id\":\"6e7b5c92-0698-4803-9979-f8a4259d6036\", " +
					" \"rank_score\":\"998.0822387880552\",  \"offer_name\":\"Hello Kitty Cafe! HD\", " +
					" \"offer_type\":\"App\",  \"offer_bid\":\"50\",  \"show_rate\":\"1\", " +
					"\"show_rate_new\":\"1\",  \"partner_balance\":\"677093\" },\r\n" +
					"{\"rank_index\":\"2\",  " +
					"\"offer_id\":\"e21b91a6-7561-4175-bf0a-7893418cfef3\",  \"rank_score\":\"991.6105393980191\",  " +
					"\"offer_name\":\"Invite friends to textPlus Free Calls\",  \"offer_type\":\"ActionOffer\", " +
					" \"offer_bid\":\"20\",  \"show_rate\":\"1\", \"show_rate_new\":\"1\",  \"partner_balance\":\"10644\" },\r\n" +
					"{\"rank_index\":\"3\",  \"offer_id\":\"015eb792-28f0-42df-8a8a-86fcf8e6c988\",  \"rank_score\":\"556.9977261204389\", " +
					" \"offer_name\":\"Arcane Empires\",  \"offer_type\":\"App\",  \"offer_bid\":\"150\",  " +
					"\"show_rate\":\"1\", \"show_rate_new\":\"1\",  \"partner_balance\":\"903351\" },\r\n{\"rank_index\":\"4\", " +
					" \"offer_id\":\"697194f1-4453-468d-bd6b-5b504b06f878\",  \"rank_score\":\"418.13097784353823\", " +
					" \"offer_name\":\"Text Photo - Texting for Instagram & \r\nFacebook\",  \"offer_type\":\"App\", " +
					" \"offer_bid\":\"25\",  \"show_rate\":\"1\", \"show_rate_new\":\"1\",  \"partner_balance\":\"55925\" },\r\n" +
					"  ]\r\n}\r\n"));
	 
			JSONObject jsonObject = (JSONObject) obj;
	 
			String key = (String) jsonObject.get("key");
			assertEquals("101.1.iOS.US..itouch", key); 

			// loop array
			JSONArray offers = (JSONArray) jsonObject.get("offers");
			@SuppressWarnings("unchecked")
			Iterator<JSONObject> iterator = (Iterator<JSONObject>)offers.iterator();
			int i=1;
			while (iterator.hasNext()) {
				assertEquals(""+i, iterator.next().get("rank_index"));
				i++;
			}
	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	 
	}
}
