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

    // Xóa file trên S3
    public String deleteFile(String fileName) {
        amazonS3.deleteObject(bucketName, fileName);
        return fileName;
    }

    // Xóa nhiều file trên S3
    public List<String> deleteMultipleFiles(List<String> fileNames) {
        List<String> results = new ArrayList<>();

        for (String fileName : fileNames) {
            if (fileName == null || fileName.isEmpty()) {
                continue; // Bỏ qua key rỗng
            }
            amazonS3.deleteObject(bucketName, fileName);
            results.add(fileName);
        }

        return results;
    }
}