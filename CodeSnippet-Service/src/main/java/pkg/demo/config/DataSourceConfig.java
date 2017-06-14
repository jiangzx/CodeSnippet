package pkg.demo.config;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import pkg.demo.common.datasource.DBSelector;


@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = DataSourceConfig.BASE_PACKAGES,sqlSessionFactoryRef = "defaultSqlSessionFactory")
public class DataSourceConfig {
	
	public static final String BASE_PACKAGES = "pkg.demo.dao";
	public static final String MAPPER_XML_PATH = "classpath:mybatis/base/*.xml";
	public static final String CFG_XML_PATH = "classpath:mybatis-config.xml";

	@Bean(name = "defaultDataSource")
	@Primary
	@ConfigurationProperties(locations = "classpath:config/datasource.properties", prefix = "ds")
	public DataSource defaultDataSource() {
		return new BasicDataSource();
	}
	
	@Bean(name = "otherDataSource")
	public DataSource dataSource() throws SQLException {
		DBSelector selector = new DBSelector();
		selector.setDefaultTargetDataSource(defaultDataSource());
		selector.afterPropertiesSet();
		return selector;
	}

    @Bean(name = "defaultSqlSessionFactory")
	@Primary
	public SqlSessionFactory sqlSessionFactory(@Qualifier("defaultDataSource") DataSource dataSource) throws Exception {
		SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
		bean.setDataSource(dataSource);
		bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(MAPPER_XML_PATH));
		bean.setConfigLocation(new PathMatchingResourcePatternResolver().getResource(CFG_XML_PATH));
		return bean.getObject();
	}
	
	@Bean(name = "defaultTransactionManager")
	@Primary
	public PlatformTransactionManager transactionManager(@Qualifier("defaultDataSource") DataSource dataSource) throws SQLException {
		return new DataSourceTransactionManager(dataSource);
	}

}
