package pkg.demo.config;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import pkg.demo.common.utils.HttpClientUtils;

@Configuration
public class RestTemplateConfig {

	@LoadBalanced
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.customizers(new ProxyCustomizer()).build();
	}

	static class ProxyCustomizer implements RestTemplateCustomizer {
		@Override
		public void customize(RestTemplate restTemplate) {

			try {
				CloseableHttpClient httpClient = HttpClientUtils.getInstance();
				restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
			} catch (KeyManagementException | NoSuchAlgorithmException e) {
				e.printStackTrace();
			}

		}

	}

}
