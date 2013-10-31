package com.tapjoy.opt.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

public class CommandExecutor {
	public static void executeCommand(String command, Logger logger) throws IOException, InterruptedException {
	System.out.println("LeiTest -- CMD:" + command);	
		String line = null;

		Process process = Runtime.getRuntime().exec(command);
		process.waitFor();
		if (process.exitValue() != 0) {
			logger.debug("audition process return with error code:"
					+ process.exitValue());
		}

		InputStream stdout = process.getInputStream();
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stdout));
		while ((line = reader.readLine()) != null) {
			logger.debug(line);
		}

		InputStream stderr = process.getErrorStream();
		reader = new BufferedReader(new InputStreamReader(stderr));
		while ((line = reader.readLine()) != null) {
			logger.error(line);
		}
		
	}
}
