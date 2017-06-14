package pkg.demo.common.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ParallelTask;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.bean.ping.PingMode;

public class NetworkUtils {
	private static final Logger logger = LoggerFactory.getLogger(NetworkUtils.class);
	private static String localHostname;
	private static String localHostServerIp;
	public final static String LIVE = "LIVE";
	public final static String UNREACHABLE = "UNREACHABLE";

	public static String getLocalHostServerIp() {
		if (localHostServerIp == null) {
			try {
				InetAddress IP = InetAddress.getLocalHost();
				localHostServerIp = IP.getHostAddress();
			} catch (UnknownHostException e) {
				logger.error("can't get hostname", e);
				localHostServerIp = "N/A";
			}
		}
		return localHostServerIp;
	}

	public static String getLocalHostname() {
		if (localHostname == null) {
			logger.info("Retrieving current hostname");
			try {
				localHostname = InetAddress.getLocalHost().getHostName();
				localHostname = localHostname.split("\\.")[0];
			} catch (UnknownHostException e) {
				logger.error("can't get hostname", e);
				localHostname = "N/A";
			}
			logger.info("Current hostname is " + localHostname);
		}
		return localHostname;
	}

	public static String getRemoteIp() {
		String remoteIp = "N/A";
		try {
			if (RequestContextHolder.getRequestAttributes() != null
					&& ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest() != null) {
				HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
						.getRequest();
				remoteIp = request.getHeader("X-Forwarded-For");
				if (StringUtils.isNotEmpty(remoteIp) && !"unKnown".equalsIgnoreCase(remoteIp)) {
					int index = remoteIp.indexOf(",");
					if (index != -1) {
						return remoteIp.substring(0, index);
					} else {
						return remoteIp;
					}
				}
				remoteIp = request.getHeader("X-Real-IP");
				if (StringUtils.isNotEmpty(remoteIp) && !"unKnown".equalsIgnoreCase(remoteIp)) {
					return remoteIp;
				}
				return request.getRemoteAddr();
			}
		} catch (Exception ex) {
			logger.error("can't get remote ip", ex);
			remoteIp = "N/A";
		}
		return remoteIp;
	}

	public static Map<String, LinkedHashSet<String>> batchPingHosts(List<String> targetHosts, PingMode mode) {
		if (targetHosts == null) {
			return new HashMap<String, LinkedHashSet<String>>();
		}
		if (mode == null) {
			mode = PingMode.PROCESS;
		}
		ParallelClient pc = new ParallelClient();
		ParallelTask task = pc.preparePing().setConcurrency(350).setTargetHostsFromList(targetHosts).setPingMode(mode)
				.setPingNumRetries(1).setPingTimeoutMillis(1500).execute(new ParallecResponseHandler() {
					@Override
					public void onCompleted(ResponseOnSingleTask res, Map<String, Object> responseContext) {

					}
				});
		pc.releaseExternalResources();
		return task.getAggregateResultMap();
	}

}
