package pkg.demo.config;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.catalina.filters.RemoteAddrFilter;
import org.apache.catalina.valves.RemoteAddrValve;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import pkg.demo.common.spring.LocalDateConverter;
import pkg.demo.common.spring.LocalDateTimeConverter;
import pkg.demo.common.spring.SpringAopAspect;

@Configuration
public class SpringWebConfig extends WebMvcConfigurerAdapter implements ServletContextInitializer, EmbeddedServletContainerCustomizer {
	
	@Value("${eura.remoteIps.allow}")
	private String remoteIpsAllow;
	
	@Value("${eura.remoteIps.deny}")
	private String remoteIpsDeny;

	@Autowired
	private SpringAopAspect springAopAspect;
	
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**");
	}

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		super.configurePathMatch(configurer);
		// configurer.setUseSuffixPatternMatch(false);
	}

	@Override
	public void customize(ConfigurableEmbeddedServletContainer container) {
		TomcatEmbeddedServletContainerFactory tomcatContainer = (TomcatEmbeddedServletContainerFactory) container;
		RemoteAddrValve remoteAddrValve = new RemoteAddrValve();
		remoteAddrValve.setAllow(remoteIpsAllow);
		remoteAddrValve.setDeny(remoteIpsDeny);
		tomcatContainer.addContextValves(remoteAddrValve);
		setLocationForStaticAssets(container);
		seErrorPage(container);

	}

	private void setLocationForStaticAssets(ConfigurableEmbeddedServletContainer container) {
		File root = new File("src/main/resources/static/");
		if (root.exists() && root.isDirectory()) {
			container.setDocumentRoot(root);
		}
	}

	private void seErrorPage(ConfigurableEmbeddedServletContainer container) {
		ErrorPage notFoundError = new ErrorPage(HttpStatus.NOT_FOUND, "/404.html");
		container.addErrorPages(notFoundError);
	}

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Bean
	public FilterRegistrationBean remoteAddressFilter() {
		FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
		RemoteAddrFilter filter = new RemoteAddrFilter();
		filter.setAllow(remoteIpsAllow);
		filterRegistrationBean.setFilter(filter);
		filterRegistrationBean.addUrlPatterns("/*");
		return filterRegistrationBean;
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		super.addInterceptors(registry);
		registry.addWebRequestInterceptor(springAopAspect);
	}
	
	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new LocalDateConverter("yyyy-MM-dd"));
		registry.addConverter(new LocalDateTimeConverter("yyyy-MM-dd HH:mm:ss"));
	}

}
