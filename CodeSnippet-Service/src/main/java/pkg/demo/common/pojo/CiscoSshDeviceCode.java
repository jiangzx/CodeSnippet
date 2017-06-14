package pkg.demo.common.pojo;

public class CiscoSshDeviceCode {

	// create ASR configuration job
	public static final Integer JOB_CREATE_SUCCESS = 1201;
	public static final Integer JOB_REQUEST_CHECK_TSVIP_EMPTY = 1202;
	public static final Integer JOB_CREATE_ERROR = 1203;
	public static final Integer JOB_RETRY_REQUEST_SUCCESS = 1204;
	public static final Integer JOB_RETRY_REQUEST_ERROR = 1205;
	public static final Integer JOB_CREATE_REQUEST_ERROR = 1206;

	public static final Integer JOB_STATUS_TODO = 1101;
	public static final Integer JOB_STATUS_RUNNING = 1102;
	public static final Integer JOB_STATUS_SUCCESS = 1103;
	public static final Integer JOB_STATUS_FAIL = 1104;

	//
	public static final Integer ASR_CONFIG_SUCCESS = 1105;
	public static final Integer ASR_CONFIG_NUMBER_USE_BY_TD = 1106;	
	public static final Integer ASR_CONFIG_FAIL_FOR_UNCONNECT_ASR = 1107;
	public static final Integer ASR_CONFIG_TODO = 1108;
	public static final Integer ASR_CONFIG_FAIL = 1109;
	public static final Integer ASR_CONFIG_REQUEST_TD_FAIL = 1110;
	public static final Integer ASR_CONFIG_FAIL_FOR_UNKNOWN_ADDR = 1111;
	public static final Integer ASR_CONFIG_FAIL_FOR_UNREACHABLE_ADDR = 1112;
	public static final Integer ASR_CONFIG_FAIL_FOR_CONNECT_REFUSED = 1113;
	public static final Integer ASR_CONFIG_FAIL_FOR_NO_AUTH = 1114;
	public static final Integer ASR_CONFIG_FAIL_FOR_UNKNOWN_HOST = 1115;
	public static final Integer ASR_CONFIG_PATTERN_NOT_FOUND = 1116;
	
	//=================================
	
	public static final Integer ASR_CONFIG_SUCCESS_CONNECTION_ASR = 1301;
	
	public static final Integer ASR_CONFIG_FAIL_UNKONE_EXCEPTION_ASR = 1302;
	public static final Integer ASR_CONFIG_EXEC_COMMAND_EXCEPTION = 1303;
	public static final Integer ASR_CONFIG_ENABLE_PASS_WRONG = 1304;
	public static final Integer ASR_CONFIG_CONNE_PASS_WRONG = 1305;
	
	
	public static Integer convertAsrConfigCode(Integer operationCode){
		if(ASR_CONFIG_PATTERN_NOT_FOUND.equals(operationCode) || ASR_CONFIG_FAIL_UNKONE_EXCEPTION_ASR.equals(operationCode) || ASR_CONFIG_EXEC_COMMAND_EXCEPTION.equals(operationCode)){
			return ASR_CONFIG_FAIL;
		}else if(ASR_CONFIG_ENABLE_PASS_WRONG.equals(operationCode) || ASR_CONFIG_CONNE_PASS_WRONG.equals(operationCode)){
			return ASR_CONFIG_FAIL_FOR_UNCONNECT_ASR;
		}
		return operationCode;
	}
	
	public static String codeConvertMessage(Integer configCodeFail){
		String msg = null;
		
		if (Integer.compare(configCodeFail, CiscoSshDeviceCode.ASR_CONFIG_FAIL_FOR_UNKNOWN_HOST) == 0) {
			msg = "Host name is incorrect";
		} else if (Integer.compare(configCodeFail, CiscoSshDeviceCode.ASR_CONFIG_FAIL_FOR_UNKNOWN_ADDR) == 0) {
			msg = "IP address is illegel";
		} else if (Integer.compare(configCodeFail, CiscoSshDeviceCode.ASR_CONFIG_FAIL_FOR_UNREACHABLE_ADDR) == 0) {
			msg = "IP address is unreachable";
		} else if(Integer.compare(configCodeFail, CiscoSshDeviceCode.ASR_CONFIG_FAIL_FOR_CONNECT_REFUSED) == 0){
			msg = "IP address is refused to connect";
		} else if (Integer.compare(configCodeFail, CiscoSshDeviceCode.ASR_CONFIG_ENABLE_PASS_WRONG) == 0) {
			msg = "Enable password is incorrect";
		} else if (Integer.compare(configCodeFail, CiscoSshDeviceCode.ASR_CONFIG_EXEC_COMMAND_EXCEPTION) == 0) {
			msg = "SSH command error";
		} else if (Integer.compare(configCodeFail, CiscoSshDeviceCode.ASR_CONFIG_FAIL_FOR_NO_AUTH) == 0) {
			msg = "Auth fail";
		} else if (Integer.compare(configCodeFail, CiscoSshDeviceCode.ASR_CONFIG_FAIL_FOR_UNCONNECT_ASR) == 0) {
			msg = "Unknown connection error";
		}else{
			msg = "Undefined exception";
		}
		return msg;
	}
}
