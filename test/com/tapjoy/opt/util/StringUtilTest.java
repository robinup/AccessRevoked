package com.tapjoy.opt.util;

import java.util.List;

import org.json.simple.JSONObject;
import org.junit.Test;

import com.tapjoy.opt.util.StringUtil;

import static org.junit.Assert.assertEquals;

public class StringUtilTest {
	@Test
	public void stringToList() {
		String formattedString = "[\"A\", \"B\"]";

		List<String> strList = StringUtil
				.formattedStringToList(formattedString);
		assertEquals("A", strList.get(0));
		assertEquals("B", strList.get(1));
	}

	@Test
	public void toLower() {
		String formattedString = "[\"A\", \"B\"]";

		List<String> strList = StringUtil
				.formattedStringToList(formattedString);
		strList = StringUtil.toLowerCase(strList);

		assertEquals("a", strList.get(0));
		assertEquals("b", strList.get(1));
	}

	@Test
	public void replace() {
		String original = "abc\" abc \"def";

		assertEquals("abc abc def", original.replaceAll("\"", ""));
	}

	@Test
	public void jsonEscapte() {
		String original = "abc\" abc \"def";
		String escapedOfferName = JSONObject.escape(original);

		assertEquals("abc\\\" abc \\\"def", escapedOfferName);
	}
}