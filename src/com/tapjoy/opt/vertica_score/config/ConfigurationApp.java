package com.tapjoy.opt.vertica_score.config;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationApp extends ConfigurationLookback {
	public static Map<String, Integer> lookbackDates = new HashMap<String, Integer>();
	public static ConfigurationApp myConfiguration = new ConfigurationApp();

	static {
		// Tiny Farm
		lookbackDates.put("9d6af572-7985-4d11-ae48-989dfc08ec4c", 28);

		// MetalStorm: Wingman
		lookbackDates.put("62a2db03-3d5f-42e1-a906-ee1d08c185c7", 28);

		// Magic Piano
		lookbackDates.put("e34ef85a-cd6d-4516-b5a5-674309776601", 28);

		// Magic Piano (android)
		lookbackDates.put("d531f20d-767e-4dd1-83c6-cb868bcb8d41", 28);

		// textPlus Free Text + Calls
		lookbackDates.put("d4097a5b-971e-4c33-8352-c1250a762f7f", 28);

		// AppDog Web App -- iOS
		lookbackDates.put("8d87c837-0d24-4c46-9d79-46696e042dc5", 28);

		// Draw Something Free -- Android
		lookbackDates.put("2efe982d-c1cf-4eb0-8163-1836cd6d927c", 28);

		// Songify(android)
		lookbackDates.put("0f127143-e23b-46df-9e70-b6e07222d122", 28);

		// Skout
		lookbackDates.put("b7256806-0b7c-4711-9d0b-f58676f8d5eb", 28);

		// Ice Age Village
		lookbackDates.put("17549b59-9bee-4c54-a0e1-77ed203f4f7a", 28);

		// MeetMe - Meet New People
		lookbackDates.put("30c709f6-f62c-4817-9fba-461d5902ed96", 28);

		// Avatar Fight
		lookbackDates.put("39e2a6ce-def1-46f3-b657-c7cd49d3f843", 28);

		// Fairy Farm
		lookbackDates.put("49eca22e-fc65-4fe9-8503-cd3a2e13f58d", 28);

		// Dead Trigger
		lookbackDates.put("4c40cc63-5475-45e3-80a0-945624ba9ead", 28);

		// Battle Nations
		lookbackDates.put("5f047627-9776-4740-bdab-6e372dc7cb75", 28);

		// Empire Defense
		lookbackDates.put("7a0dfb05-2f64-4cbe-97bf-2d4cbee6ff20", 28);

		// Big Win Soccer
		lookbackDates.put("85147815-be18-40be-bf94-fd246d442152", 28);

		// Skout
		lookbackDates.put("91080cd6-735b-4fad-b14b-dc54becf05b3", 28);

		// Family Feud
		lookbackDates.put("931d580a-61ee-4bef-9682-7c8906a3f608", 28);

		// Dead Trigger
		lookbackDates.put("a9f62fa2-1252-4d5a-96ff-8ed3dc05af85", 28);
	};

	public ConfigurationApp() {
		super.init(lookbackDates);
	}
	
	public static boolean isValidCurrency(String currency){
		if (currency == null || lookbackDates.get(currency) == null) {
			return false;
		}
		
		return true;
	}
}
