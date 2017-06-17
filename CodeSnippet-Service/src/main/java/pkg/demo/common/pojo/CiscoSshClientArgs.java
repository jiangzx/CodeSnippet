package pkg.demo.common.pojo;

/**
 * 参数
 *
 * @author zhajiang
 *
 */
public class CiscoSshClientArgs {

	private String hostname;
	private String username;
	private String password;
	private String ipAddr;
	
	private String enablePasswd;

	private int port = 22;
	private int socketTimeout = 60; // Socket
	private int chanReadTimeout = 60; // Channel
	private int retryTimes = 3;

	public CiscoSshClientArgs(String hostname, String ipAddr, String username, String password, String enablePasswd) {
		this.ipAddr = ipAddr;
		this.hostname = hostname;
		this.username = username;
		this.password = password;
		this.enablePasswd = enablePasswd;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getRetryTimes() {
		return retryTimes;
	}

	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}

	public String getEnablePasswd() {
		return enablePasswd;
	}

	public void setEnablePasswd(String enablePasswd) {
		this.enablePasswd = enablePasswd;
	}
	
	public String getIpAddr() {
		return ipAddr;
	}

	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}
	
	public int getSocketTimeout() {
		return socketTimeout;
	}

	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public int getChanReadTimeout() {
		return chanReadTimeout;
	}

	public void setChanReadTimeout(int chanReadTimeout) {
		this.chanReadTimeout = chanReadTimeout;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CiscoSshClientArgs other = (CiscoSshClientArgs) obj;
		if (hostname == null) {
			if (other.hostname != null) {
				return false;
			}
		} else if (!hostname.equals(other.hostname)) {
			return false;
		}
		if (password == null) {
			if (other.password != null) {
				return false;
			}
		} else if (!password.equals(other.password)) {
			return false;
		}
		if (username == null) {
			if (other.username != null) {
				return false;
			}
		} else if (!username.equals(other.username)) {
			return false;
		}
		return true;
	}

}
