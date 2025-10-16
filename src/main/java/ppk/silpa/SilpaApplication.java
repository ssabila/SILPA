package ppk.silpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ppk.silpa.config.FileStorageProperties;

@SpringBootApplication
@EnableConfigurationProperties({FileStorageProperties.class})
public class SilpaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SilpaApplication.class, args);
	}

}