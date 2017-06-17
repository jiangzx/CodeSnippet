package pkg.demo.common.datasource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Data;

@Configuration
@PropertySource("classpath:config/datasource.properties")
@ConfigurationProperties(prefix = "ds")
@Data
public class DataSourceBean {
	
	private String driverClassName;
	private String url;
	private String username;
	private String password;
	private String initialSize;
	private String maxActive;
	private String maxWait;

	private String minIdle;
	private String validationQuery;
	private String testOnBorrow;
	private String testWhileIdle;
	private String testOnReturn;
	private String poolPreparedStatements;
	private String maxOpenPreparedStatements;
	private String filters;

}
