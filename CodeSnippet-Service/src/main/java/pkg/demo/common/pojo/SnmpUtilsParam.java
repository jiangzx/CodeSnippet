package pkg.demo.common.pojo;

import java.util.concurrent.TimeUnit;

import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.smi.OID;

public class SnmpUtilsParam {

	private String secName;
	private String host;
	private String port = "161";
	private int secLevel = SecurityLevel.AUTH_PRIV;
	private OID authProto = AuthMD5.ID;
	private OID privProto = PrivDES.ID;
	private String authPass;
	private String privPass;
	private long timeout = TimeUnit.SECONDS.toMillis(3);
	private int retryTimes = 1;

	public SnmpUtilsParam(String secName, String host, String port, int secLevel, OID authProto, OID privProto, String authPass, String privPass) {
		super();
		this.secName = secName;
		setHost(host);
		this.port = port;
		this.secLevel = secLevel;
		this.authProto = authProto;
		this.privProto = privProto;
		this.authPass = authPass;
		this.privPass = privPass;
	}

	public SnmpUtilsParam(String secName, String host, String authPass, String privPass) {
		super();
		this.secName = secName;
		setHost(host);
		this.authPass = authPass;
		this.privPass = privPass;
	}

	public SnmpUtilsParam() {
	}

	public String getSecName() {
		return secName;
	}

	public void setSecName(String secName) {
		this.secName = secName;
	}

	public String getAuthPass() {
		return authPass;
	}

	public void setAuthPass(String authPass) {
		this.authPass = authPass;
	}

	public String getPrivPass() {
		return privPass;
	}

	public void setPrivPass(String privPass) {
		this.privPass = privPass;
	}

	public OID getAuthProto() {
		return authProto;
	}

	public void setAuthProto(OID authProto) {
		this.authProto = authProto;
	}

	public OID getPrivProto() {
		return privProto;
	}

	public void setPrivProto(OID privProto) {
		this.privProto = privProto;
	}

	public int getSecLevel() {
		return secLevel;
	}

	public void setSecLevel(int secLevel) {
		this.secLevel = secLevel;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public int getRetryTimes() {
		return retryTimes;
	}

	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}

	public enum SnmpUtilEnum {
		VO(1), OV(2);

		private int index;

		SnmpUtilEnum(int idx) {
			index = idx;
		}

		public int getIndex() {
			return index;
		}
	}
}
