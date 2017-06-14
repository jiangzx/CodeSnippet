package pkg.demo.common.pojo;

import org.apache.commons.lang3.StringUtils;

import pkg.demo.config.SystemConstConfig;

public class CustomException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private Integer code;
	private String localMessage;

	public CustomException(String message) {
		super(message);
		this.localMessage = message;
	}

	public CustomException(String message, Throwable throwable) {
		super(message, throwable);
		this.localMessage = message;
	}

	public CustomException(String message, Integer code) {
		super(message);
		this.code = code;
		this.localMessage = message;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getLocalMessage() {
		return localMessage;
	}

	public void setLocalMessage(String localMessage) {
		this.localMessage = localMessage;
	}

	public static String convertPlainResponse(String message, boolean before) {
		if (StringUtils.isNotEmpty(message)) {
			int index = message.indexOf(SystemConstConfig.DISPLAY_TAG);
			if (index > -1) {
				if(before){
					message = message.substring(0, index);
				}else{
					message = message.substring(index + SystemConstConfig.DISPLAY_TAG.length());
				}
			}
		}
		return SystemConstConfig.MSG_TAG + message;
	}

}