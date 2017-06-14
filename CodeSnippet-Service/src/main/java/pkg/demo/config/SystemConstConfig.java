package pkg.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:config/systemconst.properties")
public class SystemConstConfig {

	public static final String SUCCESS = "OKOKOK";
	public static final String FAIL = "NONONO";
	public static final String DISPLAY_TAG = "[DISPLAY]";
	public static final String MSG_TAG = "[INFO]";

	// @Getter
	// @Setter
	// @Value("${local.data.center.name}")
	// private String localDatacenter;
	//
	// @Getter
	// @Setter
	// @Value("${local.environment}")
	// private String localEnvironment;

}