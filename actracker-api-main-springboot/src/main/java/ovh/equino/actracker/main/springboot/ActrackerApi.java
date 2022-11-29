package ovh.equino.actracker.main.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "ovh.equino.actracker.main.springboot.configuration")
public class ActrackerApi {

    public static void main(String[] args) {
        SpringApplication.run(ActrackerApi.class, args);
    }
}
