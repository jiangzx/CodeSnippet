package pkg.demo.config;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter4;
/**
 * MessageConveter
 * @author zhajiang
 *
 */
@Configuration
public class MessageConverterConfig {
	
	@Bean
	public HttpMessageConverters customHttpConverters() {
		
		FastJsonHttpMessageConverter4 fastJsonConverter = new FastJsonHttpMessageConverter4();
		List<MediaType> supportedMediaTypes = new ArrayList<MediaType>();
		supportedMediaTypes.add(MediaType.TEXT_HTML);
		supportedMediaTypes.add(MediaType.APPLICATION_XML);
		supportedMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
		
		FastJsonConfig fastJsonConfig = new FastJsonConfig();
		fastJsonConfig.setCharset(StandardCharsets.UTF_8); 
		fastJsonConfig.setSerializerFeatures(
				SerializerFeature.WriteMapNullValue,
				SerializerFeature.SkipTransientField,
				SerializerFeature.WriteDateUseDateFormat,
				SerializerFeature.WriteNullStringAsEmpty,
				SerializerFeature.WriteNullListAsEmpty,
				SerializerFeature.WriteNullBooleanAsFalse,
				SerializerFeature.QuoteFieldNames,
				SerializerFeature.DisableCircularReferenceDetect
		);
		fastJsonConfig.setSerializeFilters(new ValueFilter() {
            @Override
            public Object process(Object object, String name, Object value) {
            	if(object instanceof String && value == null){
            		return StringUtils.EMPTY;
            	}else{
            		return value;
            	}
            }
        });
		fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
		fastJsonConverter.setFastJsonConfig(fastJsonConfig);
		fastJsonConverter.setSupportedMediaTypes(supportedMediaTypes);
		
		StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(); 
		supportedMediaTypes = new ArrayList<MediaType>();
		supportedMediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
		stringConverter.setSupportedMediaTypes(supportedMediaTypes);
		
		return new HttpMessageConverters(fastJsonConverter, stringConverter);
	}
}
