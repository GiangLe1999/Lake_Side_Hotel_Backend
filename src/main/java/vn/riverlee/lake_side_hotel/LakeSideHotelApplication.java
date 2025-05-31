package vn.riverlee.lake_side_hotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LakeSideHotelApplication {
	public static void main(String[] args) {
		SpringApplication.run(LakeSideHotelApplication.class, args);
	}
}
