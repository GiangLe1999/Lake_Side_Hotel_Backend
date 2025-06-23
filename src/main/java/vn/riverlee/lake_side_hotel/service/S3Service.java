package vn.riverlee.lake_side_hotel.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Value("${aws.s3.bucketUrl}") // https://lake-side-hotel-spring-boot.s3.ap-southeast-1.amazonaws.com/
    private String bucketUrl;

    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    // Tải file lên S3 và trả về key
    public String uploadFile(MultipartFile multipartFile) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + Objects.requireNonNull(multipartFile.getOriginalFilename());
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        amazonS3.putObject(new PutObjectRequest(bucketName, fileName, multipartFile.getInputStream(), metadata));
        return fileName;
    }

    // Tải nhiều file lên S3 và trả về danh sách key
    public List<String> uploadMultipleFiles(List<MultipartFile> multipartFiles) throws IOException {
        List<String> fileNames = new ArrayList<>();

        for (MultipartFile multipartFile : multipartFiles) {
            if (multipartFile == null || multipartFile.isEmpty()) {
                continue; // Bỏ qua file null hoặc rỗng
            }
            String fileName = System.currentTimeMillis() + "_" + Objects.requireNonNull(multipartFile.getOriginalFilename());
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(multipartFile.getSize());
            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, multipartFile.getInputStream(), metadata));
            fileNames.add(fileName);
        }

        return fileNames;
    }

    // Xóa file trên S3 bằng URL hoặc key
    public String deleteFile(String fileUrlOrKey) {
        String objectKey;

        // Kiểm tra nếu là URL đầy đủ hay chỉ là object key
        if (fileUrlOrKey.startsWith("https://") || fileUrlOrKey.startsWith("http://")) {
            objectKey = getObjectKeyFromUrl(fileUrlOrKey);
        } else {
            objectKey = fileUrlOrKey; // Đã là object key
        }

        amazonS3.deleteObject(bucketName, objectKey);
        return objectKey;
    }

    // Xóa nhiều file trên S3 bằng URL hoặc key
    public List<String> deleteMultipleFiles(List<String> fileUrlsOrKeys) {
        List<String> results = new ArrayList<>();

        for (String fileUrlOrKey : fileUrlsOrKeys) {
            if (fileUrlOrKey == null || fileUrlOrKey.trim().isEmpty()) {
                continue; // Bỏ qua key/URL rỗng
            }

            try {
                String objectKey;

                // Kiểm tra nếu là URL đầy đủ hay chỉ là object key
                if (fileUrlOrKey.startsWith("https://") || fileUrlOrKey.startsWith("http://")) {
                    objectKey = getObjectKeyFromUrl(fileUrlOrKey);
                } else {
                    objectKey = fileUrlOrKey; // Đã là object key
                }

                amazonS3.deleteObject(bucketName, objectKey);
                results.add(objectKey);

            } catch (IllegalArgumentException e) {
                // Log lỗi và bỏ qua file không hợp lệ
                System.err.println("Không thể xóa file: " + fileUrlOrKey + " - " + e.getMessage());
            }
        }

        return results;
    }

    // Extract Object Key from S3 URL
    private String getObjectKeyFromUrl(String fileUrl) {
        if (!fileUrl.startsWith(bucketUrl)) {
            throw new IllegalArgumentException("Invalid S3 URL: " + fileUrl);
        }
        return fileUrl.substring(bucketUrl.length());
    }
}