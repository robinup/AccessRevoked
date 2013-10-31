package com.tapjoy.opt.vertica_score.offerGenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.json.simple.parser.ParseException;

import com.tapjoy.opt.cache.OfferListCache;
import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.EmptyRankedScoreException;
import com.tapjoy.opt.common.Row;
import com.tapjoy.opt.config.OverallConfig;
import com.tapjoy.opt.offerlist.OfferList;
import com.tapjoy.opt.offerlist.OfferListWithref;
import com.tapjoy.opt.util.S3;
import com.tapjoy.opt.vertica_score.config.Configuration;
import com.tapjoy.opt.vertica_score.config.ConfigurationAudition;
import com.tapjoy.opt.vertica_score.entity.RankedOfferKey;

public class OptOfferFileWriter implements ColumnDef {
	private static Logger logger = Logger.getLogger(OptOfferFileWriter.class);

	public static void outputFile(Connection conn,
			Map<RankedOfferKey, List<Row>> rankedOfferMap,
			Map<String, Row> allOfferMap, Date current, HashSet<String> offerListKeys) throws SQLException,
			IOException, EmptyRankedScoreException, ParseException, java.text.ParseException {
		logger.debug("outputFile<");
		
		//System.out.println("LeiTest -- outputFile -- Date: " + current.toGMTString());		

		// output ranking file
		Iterator<Entry<RankedOfferKey, List<Row>>> offerListIter = rankedOfferMap
				.entrySet().iterator();
		while (offerListIter.hasNext()) {
			Entry<RankedOfferKey, List<Row>> offerRowsEntry = offerListIter
					.next();
			RankedOfferKey rankedOfferKey = offerRowsEntry.getKey();

			String algorithm = rankedOfferKey.algorithm;

			@SuppressWarnings("unused")
			String content = null;
			String segment = rankedOfferKey.segment;

			boolean enabled = true;
			if ((rankedOfferKey.os.equals(Configuration.OS.IOS))
					|| (rankedOfferKey.os.equals(Configuration.OS.ANDROID))) {
				Map<Integer, Double> auditionShowMap = ConfigurationAudition.getAuditionMap(segment, algorithm);
				
				RankOfferGenerator rankOfferGenerator = new RankOfferGenerator(
						rankedOfferKey, rankedOfferMap, auditionShowMap,
						allOfferMap, enabled, segment);
				System.out.printf("rank offer generator called - LJ\n");
				
				List<Row> offerlist = rankOfferGenerator.ranking(conn);
				
				if (Configuration.OUTPUT_AS_FILE) {
					outputAsFile(current, offerlist, rankedOfferKey, enabled);
				}
				
				if (Configuration.OUTPUT_TO_CACHE) {
					outputAsCachedOfferlist(offerlist, rankedOfferKey, enabled);
				}
				
				offerListKeys.add(rankedOfferKey.toKeyString());
				
			}
		}

		logger.debug("outputFile>");
	}
	
	
	private static void outputAsCachedOfferlist(List<Row> offerlist, RankedOfferKey rankedOfferKey, boolean enabled) {
		OfferList cacheofferlist;

		cacheofferlist = new OfferListWithref(rankedOfferKey.toKeyString(), offerlist, enabled, "VerticaScore");
		OfferListCache.getInstance().store(cacheofferlist.getKey(), cacheofferlist, true);


	}


	private static void outputAsFile(Date current, List<Row> offerlist, RankedOfferKey rankedOfferKey, boolean enabled) 
			throws SQLException, EmptyRankedScoreException, IOException, ParseException, java.text.ParseException{

		RankOfferJson offerJson = new RankOfferJson(rankedOfferKey, enabled);
		String content = offerJson.toJsonString(offerlist);
		
		String targetFile = rankedOfferKey.toKeyString();
		if (content == null) {
			logger.error("The content of " + targetFile + " is empty!");
			return;
		}
		
		String out_dir = RankingUtil.getOptOutputDir(current);
		String fullPath = out_dir + "/" + targetFile;
		File file = new File(fullPath);
		File parent_directory = file.getParentFile();
		if (null != parent_directory) {
			parent_directory.mkdirs();
		}
		
		logger.debug("writing file to::" + fullPath);
		Writer writer = new OutputStreamWriter(new FileOutputStream(fullPath), "UTF-8");
		BufferedWriter bufferWriter = new BufferedWriter(writer);
		String segment = rankedOfferKey.segment;
		bufferWriter.write(content);
		bufferWriter.close();
		
		logger.debug("Done with optimization write file:" + fullPath
				+ " for " + segment);

		if (Configuration.LOAD) {
			S3 s3 = new S3();

			String bucketName = OverallConfig.getS3BucketName();
			String key = OverallConfig.getS3BucketKey();

			File fh = new File(fullPath);
			s3.uploadFile(fh, bucketName, key + fh.getName());

			logger.debug("Done with uploading file:" + fullPath
					+ " to S3 with bucket:" + bucketName + "/" + key);
		}
	}
	
}
