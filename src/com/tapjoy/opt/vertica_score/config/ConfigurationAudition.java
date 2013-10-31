package com.tapjoy.opt.vertica_score.config;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationAudition implements ConfigurationSegment {
	public static Map<String, Map<Integer, Double>> auditionShowMap_280 = new HashMap<String, Map<Integer, Double>>();

	public static Map<Integer, Double> getAuditionMap(String segment,
			String algorithm) {
		Map<Integer, Double> map = auditionShowMap_280.get(segment);

		return map;
	}

	static {
		Map<Integer, Double> asr_gen = new HashMap<Integer, Double>();
		// stochastic auditioning slots show ratio for generic offerwall at
		// global level and country level
		for (int pos = 85; pos <= 1000; pos += 10) {
			asr_gen.put(pos, 1.0);
		}

		auditionShowMap_280.put(OPT_GOW_GLOBAL, asr_gen);
		auditionShowMap_280.put(OPT_GOW_COUNTRY, asr_gen);

		Map<Integer, Double> asr_gen_101 = new HashMap<Integer, Double>();
		// stochastic auditioning slots show ratio for generic offerwall at
		// global level and country level
		for (int pos = 85; pos <= 85; pos += 10) {
			asr_gen_101.put(pos, 1.0);
		}

		Map<Integer, Double> asr_gen_app = new HashMap<Integer, Double>();
		// stochastic auditioning slots show ratio for generic offerwall at app
		// level and country+app level
		for (int pos = 50; pos <= 1000; pos += 10) {
			asr_gen_app.put(pos, 1.0);
		}

		auditionShowMap_280.put(OPT_GOW_APP, asr_gen_app);
		auditionShowMap_280.put(OPT_GOW_COUNTRY_APP, asr_gen_app);

		Map<Integer, Double> asr_gen_app_101 = new HashMap<Integer, Double>();
		// stochastic auditioning slots show ratio for generic offerwall at app
		// level and country+app level
		for (int pos = 50; pos <= 50; pos += 10) {
			asr_gen_app_101.put(pos, 1.0);
		}

		Map<Integer, Double> asr_tjm = new HashMap<Integer, Double>();
		// stochastic auditioning slots show ratio for tjm
		for (int pos = 105; pos <= 1000; pos += 10) {
			asr_tjm.put(pos, 1.0);
		}

		auditionShowMap_280.put(OPT_TJM, asr_tjm);

		Map<Integer, Double> asr_tjm_101 = new HashMap<Integer, Double>();
		// stochastic auditioning slots show ratio for generic offerwall at
		// global level and country level
		for (int pos = 45; pos <= 95; pos += 10) {
			asr_tjm_101.put(pos, 1.0);
		}

		Map<Integer, Double> asr_tjm_country = new HashMap<Integer, Double>();
		// stochastic auditioning slots show ratio for tjm at country level
		for (int pos = 105; pos <= 1000; pos += 10) {
			asr_tjm_country.put(pos, 1.0);
		}

		auditionShowMap_280.put(OPT_TJM_COUNTRY, asr_tjm_country);

		Map<Integer, Double> asr_tjm_country_101 = new HashMap<Integer, Double>();
		// stochastic auditioning slots show ratio for tjm at country level
		for (int pos = 30; pos <= 50; pos += 10) {
			asr_tjm_country_101.put(pos, 1.0);
		}
	}
}
