package com.tapjoy.opt.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3EncryptionClient;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.EncryptionMaterials;
import com.tapjoy.opt.config.OverallConfig;
import com.tapjoy.opt.util.S3;
import com.tapjoy.opt.vertica_score.config.Configuration;

public class S3Test {
	private static Logger logger = Logger.getLogger(S3Test.class);

	@Test
	public void S3Bucket() {

		logger.trace("Entering application.");

		AWSCredentials awsCredentials = new BasicAWSCredentials(
				OverallConfig.S3Cred.aws_access_key_id,
				OverallConfig.S3Cred.aws_secret_access_key);

		AmazonS3Client s3Simple = new AmazonS3Client(awsCredentials);

		Set<String> bucketNameSet = new HashSet<String>();
		for (Bucket bucket : s3Simple.listBuckets()) {
			logger.debug(bucket.getName());
			bucketNameSet.add(bucket.getName());
		}

		assertTrue(bucketNameSet.contains("tj-optimization-audition"));
		assertTrue(bucketNameSet.contains("cached_offer_list"));
	}

	@Test
	public void S3EncryptionLoad() throws InvalidKeySpecException,
			NoSuchAlgorithmException, IOException {
		logger.trace("Entering application.");

		byte[] salt = { (byte) 0xB8, (byte) 0x9B, (byte) 0xC8, (byte) 0x82,
				(byte) 0x56, (byte) 0x96, (byte) 0xD7, (byte) 0x34 };
		String passPhrase = "pilot-offerpal";

		int iterationCount = 19;

		KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt,
				iterationCount);
		SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES")
				.generateSecret(keySpec);

		AWSCredentials awsCredentials = new BasicAWSCredentials(
				OverallConfig.S3Cred.aws_access_key_id,
				OverallConfig.S3Cred.aws_secret_access_key);

		AmazonS3EncryptionClient s3 = new AmazonS3EncryptionClient(
				awsCredentials, new EncryptionMaterials(key));

		Set<String> bucketNameSet = new HashSet<String>();
		for (Bucket bucket : s3.listBuckets()) {
			logger.debug(bucket.getName());
			bucketNameSet.add(bucket.getName());
		}

		assertTrue(bucketNameSet.contains("tj-optimization-audition"));
		assertTrue(bucketNameSet.contains("cached_offer_list"));
	}

	@Test
	public void listBucket() {
		S3 s3 = new S3();
		Set<String> bucketNameSet = new HashSet<String>();
		for (Bucket bucket : s3.getListBuckets()) {
			logger.debug(bucket.getName());
			bucketNameSet.add(bucket.getName());
		}

		assertTrue(bucketNameSet.contains("tj-optimization-audition"));
		assertTrue(bucketNameSet.contains("cached_offer_list"));
	}

	@Test
	public void listBucketFile() {
		S3 s3 = new S3();
		List<String> fileNames = s3.listBucket("tj-optimization-audition");
		Set<String> bucketNameSet = new HashSet<String>();
		for (String filename : fileNames) {
			logger.error("fileName:" + filename);
			bucketNameSet.add(filename);
		}

		assertTrue(bucketNameSet.contains("gen_Android_audition_predict"));
		assertTrue(bucketNameSet.contains("gen_iOS_audition_predict"));
		assertTrue(bucketNameSet.contains("tjm_Android_audition_predict"));
		assertTrue(bucketNameSet.contains("tjm_iOS_audition_predict"));
	}

	@Test
	public void listBucketFileWithPrefix() {
		S3 s3 = new S3();
		List<String> fileNames = s3.listBucket("tj-optimization-audition",
				"tjm_");
		Set<String> bucketNameSet = new HashSet<String>();
		for (String filename : fileNames) {
			logger.error("fileName:" + filename);
			bucketNameSet.add(filename);
		}

		assertTrue(bucketNameSet.contains("Android_audition_predict"));
		assertTrue(bucketNameSet.contains("iOS_audition_predict"));

		assertFalse(bucketNameSet.contains("gen_Android_audition_predict"));
		assertFalse(bucketNameSet.contains("gen_iOS_audition_predict"));
	}

	@Test
	public void downloadFile() {
		S3 s3 = new S3();
		logger.error("downloading file gen_Android_audition_predict");
		String fileName = "test/gen_Android_audition_predict";
		s3.downloadFile(fileName, "tj-optimization-audition",
				"gen_Android_audition_predict");
	}

	@Test
	public void downloadAllFile() {
		S3 s3 = new S3();
		String directory = Configuration.getAuditionDir();

		s3.downloadAllFiles(directory, Configuration.getAuditionS3Bucket());
	}

	@Test
	public void uploadFile() {
		S3 s3 = new S3();
		
		String bucketName = "tj-optimization-test";
		String key = "audition-unittest/";

		File fh = new File("test/com/tapjoy/opt/data/audition/gen_Android_audition_predict");
		s3.uploadFile(fh, bucketName, key+fh.getName());

		fh = new File("test/com/tapjoy/opt/data/audition/gen_iOS_audition_predict");
		s3.uploadFile(fh, bucketName, key+fh.getName());
		
		fh = new File("test/com/tapjoy/opt/data/audition/tjm_Android_audition_predict");
		s3.uploadFile(fh, bucketName, key+fh.getName());
		
		fh = new File("test/com/tapjoy/opt/data/audition/tjm_iOS_audition_predict");
		s3.uploadFile(fh, bucketName, key+fh.getName());
	}

	@Test
	public void uploadFile_appTargettingOne() {
		S3 s3 = new S3();
		
		String bucketName = "tj-optimization-targeting";
		String key = "app/";

		File fh = new File("test/com/tapjoy/opt/data/audition/gen_Android_audition_predict");
		s3.uploadFile(fh, bucketName, key+fh.getName());
	}

	@Test
	public void uploadFile_appTargetting() {
		S3 s3 = new S3();
		
		String bucketName = "tj-optimization-test";
		String key = "app/";

		File fh = new File("test/com/tapjoy/opt/data/audition/gen_Android_audition_predict");
		s3.uploadFile(fh, bucketName, key+fh.getName());
	}
}
