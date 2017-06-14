package pkg.demo.common.utils;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;

/**
 * HttpClientUtils
 *
 * @author zhajiang
 *
 */
public class HttpClientUtils {

	public static CloseableHttpClient getInstance() throws KeyManagementException, NoSuchAlgorithmException {

		final int DEFAULT_POOL_SIZE = 200;
		final int DEFAULT_TIMEOUT = 90 * 1000;

		ConnectionKeepAliveStrategy connectionKeepAliveStrategy = new ConnectionKeepAliveStrategy() {
			@Override
			public long getKeepAliveDuration(HttpResponse httpResponse, HttpContext httpContext) {
				return 20 * 1000;
			}
		};

		// Set HttpSSL
		final SSLConnectionSocketFactory sslsf;
		try {
			sslsf = new SSLConnectionSocketFactory(SSLContext.getDefault(), NoopHostnameVerifier.INSTANCE);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}

		final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
				.register("http", PlainConnectionSocketFactory.getSocketFactory()).register("https", sslsf).build();

		PoolingHttpClientConnectionManager clientConnManager = new PoolingHttpClientConnectionManager(registry);
		clientConnManager.setMaxTotal(DEFAULT_POOL_SIZE);
		clientConnManager.setDefaultMaxPerRoute(DEFAULT_POOL_SIZE / 20);
		clientConnManager.setValidateAfterInactivity(10);

		final RequestConfig config = RequestConfig.custom().setConnectTimeout(DEFAULT_TIMEOUT).setConnectionRequestTimeout(DEFAULT_TIMEOUT)
				.setSocketTimeout(DEFAULT_TIMEOUT).build();
		final CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).setSSLSocketFactory(sslsf)
				.setKeepAliveStrategy(connectionKeepAliveStrategy).setConnectionManager(clientConnManager).build();

		return client;
	}

}