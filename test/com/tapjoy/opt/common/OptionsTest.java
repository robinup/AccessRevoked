package com.tapjoy.opt.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.junit.Test;

public class OptionsTest {
	private static Logger logger = Logger.getLogger(OptionsTest.class);

	@Test
	public void optionTest() {
		String[] args = new String[] { "--production=y",
				"--database=production-main", "--algorithms", "101", "280" };

		try {
			// create Options object
			Options options = new Options();

			options.addOption("p", "production", true,
					"production or not (y/n)");

			options.addOption("d", "database", true,
					"database going to be used");

			@SuppressWarnings("static-access")
			Option option = OptionBuilder.withLongOpt("algorithms").hasArgs()
					.withDescription("algorithms to be executed").create('a');
			options.addOption(option);

			CommandLineParser parser = new PosixParser();
			CommandLine cmd = parser.parse(options, args);

			assertTrue(cmd.hasOption("production"));
			assertEquals("y", cmd.getOptionValue("production"));

			assertTrue(cmd.hasOption("database"));
			assertEquals("production-main", cmd.getOptionValue("database"));

			assertTrue(cmd.hasOption("algorithms"));
			Object[] algorithms = cmd.getOptionValues("algorithms");
			assertEquals("101", algorithms[0]);
			assertEquals("280", algorithms[1]);
		} catch (ParseException exp) {
			logger.fatal("Unexpected exception:" + exp.getMessage());
		}
	}
}
