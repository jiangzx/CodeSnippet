package pkg.demo.config;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import pkg.demo.common.datasource.DBSelector;
import pkg.demo.common.datasource.DataSourceBean;

@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = DataSourceConfig.BASE_PACKAGES,sqlSessionFactoryRef = "basicSqlSessionFactory")
public class DataSourceConfig {
	
	public static final String BASE_PACKAGES = "pkg.demo.dao";
	public static final String MAPPER_XML_PATH = "classpath:mybatis/**/*.xml";
	public static final String CFG_XML_PATH = "classpath:mybatis-config.xml";

	@Autowired
	private DataSourceBean dataSource;
	
	@Bean(name = "basicDataSource")
	@Primary
	public DataSource defaultDataSource() {
		return DataSourceBuilder.create().type(BasicDataSource.class)
				.driverClassName(dataSource.getDriverClassName())
				.url(dataSource.getUrl())
				.username(dataSource.getUsername())
				.password(dataSource.getPassword())
				.build();
	}
	
	@Bean(name = "secondDataSource")
	public DataSource dataSource() throws SQLException {
		DBSelector selector = new DBSelector();
		selector.setDefaultTargetDataSource(defaultDataSource());
		selector.afterPropertiesSet();
		return selector;
	}

    @Bean(name = "basicSqlSessionFactory")
	@Primary
	public SqlSessionFactory sqlSessionFactory(@Qualifier("basicDataSource") DataSource dataSource) throws Exception {
		SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
		bean.setDataSource(dataSource);
		bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(MAPPER_XML_PATH));
		bean.setConfigLocation(new PathMatchingResourcePatternResolver().getResource(CFG_XML_PATH));
		return bean.getObject();
	}
	
	@Bean(name = "basicTransactionManager")
	@Primary
	public PlatformTransactionManager transactionManager(@Qualifier("basicDataSource") DataSource dataSource) throws SQLException {
		return new DataSourceTransactionManager(dataSource);
	}

}
