package com.tapjoy.opt.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.tapjoy.opt.config.OverallConfig;

public class S3 {
	private static Logger logger = Logger.getLogger(S3.class);
	private AmazonS3Client s3Simple = null;

	/*
	 * private static byte[] salt = { (byte) 0xB8, (byte) 0x9B, (byte) 0xC8,
	 * (byte) 0x82, (byte) 0x56, (byte) 0x96, (byte) 0xD7, (byte) 0x34 };
	 * private static String passPhrase = "pilot-offerpal"; private static int
	 * iterationCount = 19;
	 */

	public S3() {
		AWSCredentials awsCredentials = new BasicAWSCredentials(
				OverallConfig.S3Cred.aws_access_key_id,
				OverallConfig.S3Cred.aws_secret_access_key);
		s3Simple = new AmazonS3Client(awsCredentials);

		/*
		 * try { KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(),
		 * salt, iterationCount); SecretKey key =
		 * SecretKeyFactory.getInstance("PBEWithMD5AndDES")
		 * .generateSecret(keySpec); s3Encrypt = new
		 * AmazonS3EncryptionClient(awsCredentials, new
		 * EncryptionMaterials(key));
		 * 
		 * } catch (InvalidKeySpecException e) { logger.trace(e); } catch
		 * (NoSuchAlgorithmException e) { logger.trace(e); }
		 */

	}

	public AmazonS3Client getS3() {
		// we are not using encryption for now
		// return (s3Encrypt == null ? s3Simple : s3Encrypt);
		return s3Simple;
	}

	public List<Bucket> getListBuckets() {
		return getS3().listBuckets();
	}

	public void uploadFile(File fh, String bucketName, String key) {
		PutObjectRequest req = new PutObjectRequest(bucketName, key, fh);
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(fh.length());
		req.setMetadata(metadata);
		logger.trace("Uploading a new object to S3 from a file:" + fh.getName());

		// adding the retry logic unfortunately
		try {
			getS3().putObject(req);
		} catch (Exception ex) {
			logger.fatal(ex);
			
			//trying one more time
			getS3().putObject(req);			
			logger.debug("retrying the uploading works!");
		}
	}

	/**
	 * Delete an object - Unless versioning has been turned on for your bucket,
	 * there is no way to undelete an object, so use caution when deleting
	 * objects.
	 */
	public void deleteObject(String bucketName, String key) {
		logger.trace("Deleting an object::" + bucketName + "::" + key);

		getS3().deleteObject(bucketName, key);
	}

	/**
	 * Delete a bucket - A bucket must be completely empty before it can be
	 * deleted, so remember to delete any objects from your buckets before you
	 * try to delete them.
	 */
	public void deleteBucket(String bucketName) {
		logger.trace("Deleting a bucket " + bucketName);

		getS3().deleteBucket(bucketName);
	}

	/**
	 * List objects in your bucket by prefix - There are many options for
	 * listing the objects in your bucket. Keep in mind that buckets with many
	 * objects might truncate their results when listing their objects, so be
	 * sure to check if the returned object listing is truncated, and use the
	 * AmazonS3.listNextBatchOfObjects(...) operation to retrieve additional
	 * results. returning file list in bucket/prefix combination
	 * 
	 * @param bucketName
	 * @param prefix
	 * @return
	 */
	public List<String> listBucket(String bucketName, String prefix) {
		List<S3ObjectSummary> listBucketSummary = listBucketSummary(bucketName,
				prefix);

		int prefix_size = prefix.length();
		List<String> keyList = new ArrayList<String>();
		for (S3ObjectSummary objectSummary : listBucketSummary) {
			String keyString = objectSummary.getKey().substring(prefix_size);
			keyList.add(keyString);
		}

		return keyList;
	}

	public List<S3ObjectSummary> listBucketSummary(String bucketName,
			String prefix) {
		ObjectListing current = getS3().listObjects(bucketName, prefix);
		List<S3ObjectSummary> keyList = current.getObjectSummaries();
		ObjectListing next = getS3().listNextBatchOfObjects(current);
		keyList.addAll(next.getObjectSummaries());

		while (next.isTruncated()) {
			current = getS3().listNextBatchOfObjects(next);
			keyList.addAll(current.getObjectSummaries());
			next = getS3().listNextBatchOfObjects(current);
		}
		keyList.addAll(next.getObjectSummaries());

		return keyList;
	}

	public List<String> listBucket(String bucketName) {
		return listBucket(bucketName, "");
	}

	/**
	 * Download an object - When you download an object, you get all of the
	 * object's metadata and a stream from which to read the contents. It's
	 * important to read the contents of the stream as quickly as possibly since
	 * the data is streamed directly from Amazon S3 and your network connection
	 * will remain open until you read all the data or close the input stream.
	 * GetObjectRequest also supports several other options, including
	 * conditional downloading of objects based on modification times, ETags,
	 * and selectively downloading a range of an object.
	 */
	public void downloadFile(String fileName, String bucketName, String key) {
		logger.trace("Downloading an object");
		S3Object object = getS3().getObject(
				new GetObjectRequest(bucketName, key));
		logger.trace("Content-Type: "
				+ object.getObjectMetadata().getContentType());

		InputStream reader = new BufferedInputStream(object.getObjectContent());

		File file = new File(fileName);
		try {
			OutputStream writer = new BufferedOutputStream(
					new FileOutputStream(file));
			int read = -1;
			while ((read = reader.read()) != -1) {
				writer.write(read);
			}

			writer.flush();
			writer.close();
			reader.close();
		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundException!!!", e);
		} catch (IOException e) {
			logger.error("IOException!!!", e);
		}
	}

	/**
	 * downloading all the files from S3 bucket
	 */
	public void downloadAllFiles(String directory, String bucketName,
			String prefix, String dirPrefix) {
		logger.trace("downloadAllFiles<");
		
		File dir = new File(directory);
		if (dir.exists() == false) {
			dir.mkdirs();
		}

		List<S3ObjectSummary> fileNames = listBucketSummary(bucketName, prefix);
		for (S3ObjectSummary file : fileNames) {
			logger.trace("downloading file:" + file.getKey());

			String subDir = "";
			String fileName = file.getKey();

			/*
			 * Ignoring dirPrefix from the S3 key that won't be part of
			 * directory we are creating on client
			 */
			if (dirPrefix != null && dirPrefix.length() > 0
					&& fileName.indexOf(dirPrefix) >= 0) {
				fileName = fileName.substring(fileName.indexOf(dirPrefix)
						+ dirPrefix.length());
			}

			int endIndex = fileName.lastIndexOf("/");
			if (endIndex > 0) {
				subDir = fileName.substring(0, endIndex);

				File subDirFH = new File(directory + "/" + subDir);
				if (subDirFH.exists() == false) {
					subDirFH.mkdirs();
				}

				fileName = fileName.substring(endIndex);
			}
			downloadFile(directory + "/" + subDir + "/" + fileName, bucketName,
					file.getKey());
		}

		logger.trace("downloadAllFiles>");
	}

	/**
	 * download all files under the bucket
	 * 
	 * @param directory
	 * @param bucketName
	 */
	public void downloadAllFiles(String directory, String bucketName) {
		downloadAllFiles(directory, bucketName, "", "");
	}
}
