package ittalents.javaee1.util;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import ittalents.javaee1.models.pojo.Video;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

@Service
public class AmazonClient {
	private static final String SUCCESSFULLY_DELETED = "Successfully deleted";
	
	private Logger logger = LogManager.getLogger(AmazonClient.class);
	
	private AmazonS3 s3client;
	
	@Value("${aws.endpoint.url}")
	private String endpointUrl;
	@Value("${aws.s3.audio.bucket}")
	private String bucketName;
	@Value("${aws.access.key.id}")
	private String accessKey;
	@Value("${aws.access.key.secret}")
	private String secretKey;
	@Value("${aws.region}")
	private String awsRegion;
	
	@PostConstruct
	private void initializeAmazon() {
		AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
		this.s3client = AmazonS3ClientBuilder
				.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withRegion(Regions.EU_CENTRAL_1)
				.withForceGlobalBucketAccessEnabled(true)
				.build();
	}
	
	public String uploadFile(MultipartFile multipartFile, Video video) {
		String fileUrl = "";
		try {
			File file = convertMultiPartToFile(multipartFile);
			String fileName = generateFileName(multipartFile, video);
			fileUrl = endpointUrl + "/" + bucketName + "/" + fileName;
			uploadFileTos3bucket(fileName, file);
			file.delete();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return fileUrl;
	}
	
	private File convertMultiPartToFile(MultipartFile file) throws IOException {
		File convFile = new File(file.getOriginalFilename());
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		fos.close();
		return convFile;
	}
	
	private String generateFileName(MultipartFile multiPart, Video video) {
		return new Date().getTime() + "-" + video.getUploaderId() + "-"
				+ multiPart.getOriginalFilename().replace(" ", "_");
	}
	
	private void uploadFileTos3bucket(String fileName, File file) {
		s3client.putObject(new PutObjectRequest(bucketName, fileName, file)
				.withCannedAcl(CannedAccessControlList.PublicRead));
	}
	
	public String deleteFileFromS3Bucket(String fileUrl) {
		try {
			String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
			s3client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return SUCCESSFULLY_DELETED;
	}
	
}