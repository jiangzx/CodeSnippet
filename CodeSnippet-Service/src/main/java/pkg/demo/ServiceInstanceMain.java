package pkg.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.ulisesbocchio.jasyptspringboot.environment.EncryptableEnvironment;

@EnableEurekaClient
@EnableScheduling
@SpringBootApplication
public class ServiceInstanceMain {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceInstanceMain.class);

	public static void main(String[] args) {
		new SpringApplicationBuilder(ServiceInstanceMain.class)
				.environment(new EncryptableEnvironment(new StandardEnvironment())).bannerMode(Mode.OFF).run(args);
		LOGGER.info("--------CodeSnippet-Service :: Started--------");

	}
}
