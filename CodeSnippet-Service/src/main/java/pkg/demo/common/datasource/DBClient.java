package pkg.demo.common.datasource;

import lombok.Data;

@Data
public class DBClient {

	String dbDriver = "com.mysql.jdbc.Driver";
	String dbUrl;
	String dbUser;
	String dbPass;

	public DBClient(String dbUrl, String dbUser, String dbPass) {
		super();
		this.dbUrl = dbUrl;
		this.dbUser = dbUser;
		this.dbPass = dbPass;
	}

	public DBClient(String dbDriver, String dbUrl, String dbUser, String dbPass) {
		super();
		this.dbDriver = dbDriver;
		this.dbUrl = dbUrl;
		this.dbUser = dbUser;
		this.dbPass = dbPass;
	}
}
