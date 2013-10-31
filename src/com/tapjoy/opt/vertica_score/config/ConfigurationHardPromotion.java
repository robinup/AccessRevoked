package com.tapjoy.opt.vertica_score.config;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationHardPromotion implements ConfigurationSegment {
	public static Map<String, String[]> hardPromotionMap = new HashMap<String, String[]>();

	public static ConfigurationHardPromotion myConfiguration = new ConfigurationHardPromotion();

	static {
		String[] hard_promo = { "fa8616b8-2ae3-4fa3-8ec6-d757ad539434",
				"5978b3a2-a370-4821-a499-024c98f4d4e6",
				"c96619ea-4c75-41e7-8421-ea6c966c6b63",
				"ae55a6a1-e490-47bc-b360-678d906d835b",
				"fc9b2627-4a5f-4614-8454-3d78215399c8",
				"c1729ebc-3da8-4765-b45f-a7d55498ccc3",
				"8e22e8f1-8a17-40bb-b8be-04f6bd1e8a86",
				"36f35fe0-e57c-4e94-b7e8-dfb1dbacd5f5",
				"38fa7950-3b8c-497e-a85c-6b247cfcc821",
				"2ec7b476-94aa-4f4d-8ca6-e659a0ae73ad",
				"a7e12fec-5016-4a55-b270-65feacdb832c",
				"12b7ea33-8fde-4297-bae9-b7cb444897dc",
				"d7c95870-3a79-4643-bac5-c96c1a94f634",
				"15e1a61f-8a8c-4413-9719-7e221f64ec9c",
				"2932dfc3-ef7a-4555-8e31-e2a5e00f7bc1",
				"1c83868d-b83c-417a-8a90-52e07467ea6e",
				"218b84b1-bfb0-48a1-99a9-33240be1e383",
				"52743235-5b62-44b4-ac59-c3032be1e097",
				"edd3b9e5-0d23-4e5c-8608-362ddfe63c95",
				"659ef323-f6f7-46b1-90f0-a9e87509bee7",
				"5059fa7d-6b73-4dae-ac0a-711630a11384",
				"01602483-e691-4325-8e57-f9a33ec57fc0",
				"dc35bf60-33f1-42a8-9bf1-023539f13158",
				"350049dc-84bd-4208-a07a-cd5d1021f63b",
				"26e9e056-9c09-4cd1-aac9-6eb132cfa51a",
				"2c37e76d-b245-413a-9fd8-2a05b4785444",
				"615ec44c-11fa-463c-ac95-2c4757a88783",
				"56d4ca5d-3ce8-41f3-bae8-9567000862fb",
				"c2adcc84-43d6-4966-a62b-fb287fd22712",
				"91399a2f-1289-443f-bfc4-0b238426b893" };

		hardPromotionMap.put(OPT_GOW_GLOBAL + "_" + Configuration.OS.IOS, hard_promo);

		String[] self_promo_android = { "922dbe73-c39f-4e8b-8897-2956ee621f34",
				"d9bbd7c5-a1c6-4f52-9a5b-7e397bbad7c8",
				"b3a02eaa-17a0-4fc0-bdf9-ac27a2bdc5d8",
				"80ae257a-99f0-4f4e-add6-22c2a6dc1ea8",
				"133e37c3-eb56-481f-a5b9-a5ccdab1c385",
				"2550e15b-9ba0-4058-83a0-c4e301b6ecfb",
				"9249d41b-7244-468c-b9e7-a6a416ca49d2",
				"8dc63889-d563-4118-8a84-7795e403d34a",
				"995ec864-0564-4ca7-a5b4-596f680ae6fe",
				"ee1bea83-381c-4355-b4cc-cd038c2cef19",
				"31cd3eed-fbc5-4c58-b189-3422b792b829",
				"a9907b0f-dc52-4fba-a07b-7459f6fb46bc",
				"1e8c593e-2225-4360-b737-1e9747883f5d",
				"1cde318c-9920-4b6f-9e02-c8e4e1168b2a" };

		hardPromotionMap.put(OPT_GOW_GLOBAL + "_" + Configuration.OS.ANDROID, self_promo_android);

		hardPromotionMap.put(OPT_GOW_COUNTRY + "_" + Configuration.OS.ANDROID, self_promo_android);

		hardPromotionMap.put(OPT_GOW_APP + "_" + Configuration.OS.ANDROID, self_promo_android);

		hardPromotionMap.put(Configuration.OS.ANDROID, self_promo_android);
	}
}
