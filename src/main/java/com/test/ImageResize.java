package com.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;

public class ImageResize implements RequestHandler<S3Event, String> {

	private AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();

	public ImageResize() {}

	// Test purpose only.
	ImageResize(AmazonS3 s3) {
		this.s3 = s3;
	}
	
	
	@Override
	public String handleRequest(S3Event event, Context context) {
		context.getLogger().log("Received event: " + event);

		// Get the object from the event and show its content type
		String bucket = event.getRecords().get(0).getS3().getBucket().getName();
		String key = event.getRecords().get(0).getS3().getObject().getKey();
		try {
		
			
			LambdaLogger logger = context.getLogger();

			logger.log("bucket : " + bucket);
			logger.log("key : " + key);

			logger.log("retion name : " + s3.getRegionName());

			S3Object response = s3.getObject(new GetObjectRequest(bucket, key));

			S3ObjectInputStream s3ObjIs = response.getObjectContent();
			
			byte[] originImageBytes = IOUtils.toByteArray(s3ObjIs);
			
			resizePut(context, bucket, key, originImageBytes, ImageUtil.WIDTH_800, ImageUtil.WIDTH_800_NAME);
			resizePut(context, bucket, key, originImageBytes, ImageUtil.WIDTH_400, ImageUtil.WIDTH_400_NAME);
			resizePut(context, bucket, key, originImageBytes, ImageUtil.WIDTH_160, ImageUtil.WIDTH_160_NAME);
			
			s3ObjIs.close();
			
			return "SUCCESS";
		} catch (Exception e) {
			e.printStackTrace();
			context.getLogger().log(String.format(
					"Error getting object %s from bucket %s. Make sure they exist and"
							+ " your bucket is in the same region as this function.", bucket, key));
			try {
				throw e;
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return "FAILED";
	}
	public void resizePut(Context context, String bucket, String key, byte[] originImageBytes, int targetWidth, String targetKey) throws IOException{
		
		InputStream is = new ByteArrayInputStream(originImageBytes);
		
		resizePut(context, bucket, key, is, targetWidth, targetKey);
		
		is.close();
	
	}
	public void resizePut(Context context, String bucket, String key, InputStream is, int targetWidth, String targetKey) throws IOException{
		
		if(is.markSupported()) {
            is.reset();
		}
		
		String resizeKey = key.replaceAll("_origin", "_"+targetKey);
		InputStream resizeIs =  ImageUtil.resize(key, is, targetWidth, ImageUtil.RATIO);
		byte[] bytes = IOUtils.toByteArray(resizeIs);

		ObjectMetadata metaData = new ObjectMetadata();
		metaData.setContentLength(bytes.length);

		PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, resizeKey,
				new ByteArrayInputStream(bytes), metaData);
		putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);
		s3.putObject(putObjectRequest);

	}

	
}