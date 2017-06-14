package pkg.demo.common.utils;

import static net.sf.expectit.filter.Filters.removeColors;
import static net.sf.expectit.filter.Filters.removeNonPrintable;
import static net.sf.expectit.matcher.Matchers.regexp;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.Security;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import net.sf.expectit.ExpectIOException;
import net.sf.expectit.matcher.Matcher;
import pkg.demo.common.pojo.CiscoSshClientExpectRunner;
import pkg.demo.common.pojo.CiscoSshClientParams;
import pkg.demo.common.pojo.CiscoSshDeviceCode;
import pkg.demo.common.pojo.CustomException;
import pkg.demo.config.SystemConstConfig;

/**
 * Cisco SSH client for asr devices
 * 
 * @author zhajiang
 *
 */
public class CiscoSshClientUtils {
	// private ThreadLocal<Expect> threadLocal = new ThreadLocal<Expect>();
	private static final Logger logger = LoggerFactory.getLogger(CiscoSshClientUtils.class);
	private CiscoSshClientParams credentials;
	private Session session;
	private Channel channel;
	private boolean connected;
	private final static String PASSWORD_PROMPT = "(?i)password: ";
	private final static String ENABLE_SUPERUSER = "enable"; 
	private final static String DISABLE_OUTPUT_BUFFERING = "terminal length 0";
	private final static String ERROR = "ERROR_MSG";
	
	public CiscoSshClientUtils(CiscoSshClientParams credentials) throws CustomException {
		Security.insertProviderAt(new BouncyCastleProvider(), 1);
		this.credentials = credentials;
		int retryTimes = credentials.getRetryTimes();
		Map<String,CustomException> error = Maps.newHashMap();
		while (retryTimes > 0) {
			connected = initSession(error);
			if (connected) {
				break;
			}
			retryTimes--;
		}
		if (retryTimes == 0) {
			String hostname = credentials.getHostname();
			int tryTimes = credentials.getRetryTimes();
			String stackTrace = ExceptionUtils.getStackTrace(error.get(ERROR));
			int err_code = error.get(ERROR).getCode();
			String err_mess = "SSH can't be able to connect to [" + hostname + "] after reconnected for " + tryTimes + " times " + SystemConstConfig.DISPLAY_TAG + " + Caused by " + stackTrace;
			logger.error(err_mess);
			throw new CustomException(err_mess, err_code);
		}
		logger.info("SSH has been connected to [{}]",credentials.getHostname());
	}

	private boolean initSession(Map<String,CustomException> error) {
		JSch jSch = new JSch();
		Properties config = new Properties();
		//If this line isn't present, every host must be in known_hosts 
		config.put("StrictHostKeyChecking", "no");
		config.put("PreferredAuthentications", "password");
		config.put("UserKnownHostsFile", "/dev/null");
		config.put("compression.s2c", "zlib,none");
		config.put("compression.c2s", "zlib,none");
		try {
			session = jSch.getSession(credentials.getUsername(), credentials.getIpAddr());
			session.setConfig(config);
			session.setPort(credentials.getPort());
			session.setTimeout((int)TimeUnit.MILLISECONDS.convert(credentials.getSocketTimeout(), TimeUnit.SECONDS));
			session.setPassword(credentials.getPassword());
			session.connect();
			ifAbsentCreateExpect();
			usesEnable(credentials.getHostname(), credentials.getEnablePasswd());
			return true;
		} catch (CustomException  e) {
			close();
			error.put(ERROR, e);
		} catch (JSchException e) {
			close();
			Throwable t = e.getCause();
			String merr = e.getMessage();
			CustomException ce = new CustomException(ExceptionUtils.getStackTrace(e), CiscoSshDeviceCode.ASR_CONFIG_FAIL_FOR_UNCONNECT_ASR);
			if(t instanceof UnknownHostException){
				ce = new CustomException(ExceptionUtils.getStackTrace(e), CiscoSshDeviceCode.ASR_CONFIG_FAIL_FOR_UNKNOWN_ADDR);
			}else if(!Strings.isNullOrEmpty(merr) && merr.indexOf("Connection refused") > -1){
				ce = new CustomException(ExceptionUtils.getStackTrace(e), CiscoSshDeviceCode.ASR_CONFIG_FAIL_FOR_CONNECT_REFUSED);
			}else if(!Strings.isNullOrEmpty(merr) && merr.indexOf("Auth fail") > -1){
				ce = new CustomException(ExceptionUtils.getStackTrace(e), CiscoSshDeviceCode.ASR_CONFIG_FAIL_FOR_NO_AUTH);
			}else if(!Strings.isNullOrEmpty(merr) && merr.indexOf("timeout") > -1){
				ce = new CustomException(ExceptionUtils.getStackTrace(e), CiscoSshDeviceCode.ASR_CONFIG_FAIL_FOR_UNREACHABLE_ADDR);
			}
			error.put(ERROR, ce);
		}
		return false;
	}

	private void ifAbsentCreateExpect() throws CustomException {
		// if goes here indicates that the remote server is being connected, or
		// it captured the connection exception from upstream.
		// no need to check connection state.[connected]
		try {
			if (null == channel || channel.isClosed() || channel.isEOF()) {
				channel = session.openChannel("shell");
				channel.connect();
			}
		} catch (JSchException e) {
			CustomException ce = new CustomException(ExceptionUtils.getStackTrace(e),CiscoSshDeviceCode.ASR_CONFIG_FAIL_FOR_UNCONNECT_ASR);
			throw ce;
		}
	}
	
	private Expect getExcept() throws CustomException{
		Expect expect = null;
		try {
			ifAbsentCreateExpect();
			if (channel.isConnected()) {
				expect = new ExpectBuilder().withOutput(channel.getOutputStream())
						.withInputs(channel.getInputStream(), channel.getExtInputStream())
						.withInputFilters(removeColors(), removeNonPrintable())
						.withTimeout(credentials.getChanReadTimeout(), TimeUnit.SECONDS).withExceptionOnFailure().build();
			}
		} catch (IOException e) {
			CustomException ce = new CustomException(ExceptionUtils.getStackTrace(e),CiscoSshDeviceCode.ASR_CONFIG_FAIL_FOR_UNCONNECT_ASR);
			throw ce;
		}
		return expect;
	}
	
	private void usesEnable(String hostname, String enablePass) throws CustomException {
		String mode = "";
		Expect expect = getExcept();
		try {
			mode = expect.expect(regexp("(?i)" + hostname + "[#>]")).getInput();
		} catch (IOException e) {
			CustomException ce = new CustomException("Wrong expression : " +  "(?i)" + hostname + "[#>]" + SystemConstConfig.DISPLAY_TAG + ExceptionUtils.getStackTrace(e), CiscoSshDeviceCode.ASR_CONFIG_FAIL_FOR_UNKNOWN_HOST);
			throw ce;
		}finally{
			closeExpect(expect);
		}
		try {
			if(mode.lastIndexOf(">") > -1){
			    writeAndGet(ENABLE_SUPERUSER,PASSWORD_PROMPT);
			    writeAndGet(enablePass,"#");
			}
			writeAndGet(DISABLE_OUTPUT_BUFFERING,"#");
		} catch (CustomException e) {
			CustomException ce = new CustomException(ExceptionUtils.getStackTrace(e),CiscoSshDeviceCode.ASR_CONFIG_ENABLE_PASS_WRONG);
			throw ce;
		}
	}
	
	public String writeAndGet(String cmd, String pattern) throws CustomException {
		return writeAndGet(cmd, credentials.getChanReadTimeout(), regexp(pattern), null);
	}
	
	public String writeAndGet(String cmd, long timeout, String pattern) throws CustomException {
		return writeAndGet(cmd, timeout, regexp(pattern), null);
	}
	
	public void writeNoResponse(String cmd) throws CustomException{
		Expect expect = getExcept();
		try {
			expect.sendLine(cmd);
		} catch (IOException e) {
			String err_mess = "Command[" + cmd + "] wrong on " + credentials.getHostname() + SystemConstConfig.DISPLAY_TAG + ":" + ExceptionUtils.getStackTrace(e);
			CustomException ce = new CustomException(err_mess ,CiscoSshDeviceCode.ASR_CONFIG_EXEC_COMMAND_EXCEPTION);
			throw ce;
		}finally{
			closeExpect(expect);
		}
	}
	
	public String writeAndGet(String cmd, long timeout, Matcher<?> matcher, CiscoSshClientExpectRunner runner) throws CustomException {
		Expect expect = getExcept();
		// threadLocal.set(expect);
		try {
			expect.sendLine(cmd);
			long before = System.currentTimeMillis();
			String result = expect.withTimeout(timeout, TimeUnit.SECONDS).expect(matcher).getInput();
			if(runner != null){
				result = runner.run(expect, result);
			}
			long after = System.currentTimeMillis();
			long period = (after - before)/1000;
			if(period >= 20){
				logger.info("SSH exec command [{}] on [{}] with [{}] sec and get result: {}", new Object[]{cmd, credentials.getHostname(), period, result});
			}
			return result;
		} catch (IOException IoEX) {
			StringBuilder sbErr = new StringBuilder();
			// print buffer remaining if error occurs
			if(IoEX instanceof ExpectIOException){
				String buffer = ((ExpectIOException) IoEX).getInputBuffer();
				sbErr.append("[Buffer Remaining]").append(buffer).append("\n");
			}
			sbErr.append("Command[" + cmd + "] wrong on " + credentials.getHostname() + SystemConstConfig.DISPLAY_TAG + ":" + ExceptionUtils.getStackTrace(IoEX));
			logger.error(sbErr.toString());
			CustomException ce = new CustomException(sbErr.toString() ,CiscoSshDeviceCode.ASR_CONFIG_EXEC_COMMAND_EXCEPTION);
			throw ce;
		}finally{
			closeExpect(expect);
		}
	}
	
	public void close() {
		if (null != channel && (!channel.isClosed() || channel.isConnected())) {
			channel.disconnect();
		}
		if (null != session && session.isConnected()) {
			session.disconnect();
		}
	}
	
	private void closeExpect(Expect expect){
		if(expect != null){
			try {
				expect.close();
			} catch (IOException e) {
				logger.error("Except close failed >> {}",ExceptionUtils.getStackTrace(e));
			}
		}
	}

//	public ThreadLocal<Expect> getThreadLocal() {
//		return threadLocal;
//	}
//
//	public void setThreadLocal(ThreadLocal<Expect> threadLocal) {
//		this.threadLocal = threadLocal;
//	}
}
