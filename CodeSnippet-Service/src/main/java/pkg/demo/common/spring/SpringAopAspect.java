package pkg.demo.common.spring;


import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

@Component
public class SpringAopAspect implements WebRequestInterceptor  {
	public final static String TRACKID_KEY = "mylog_indictor";

	@Override
	public void postHandle(WebRequest request, ModelMap model) throws Exception {
		
	}

	@Override
	@lombok.SneakyThrows
	public void afterCompletion(WebRequest request, Exception ex) throws Exception {
		MDC.clear();
	}

	@Override
	@lombok.SneakyThrows
	public void preHandle(WebRequest request) throws Exception {
		MDC.put(TRACKID_KEY, "NA_"+UUID.randomUUID().toString());
	}
	
}
