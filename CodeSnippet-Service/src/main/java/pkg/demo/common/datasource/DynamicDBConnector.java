package pkg.demo.common.datasource;

import org.springframework.stereotype.Component;
/**
 * DynamicDatasource connection and release
 * @author zhajiang
 *
 */
@Component
public class DynamicDBConnector {

	public void connect(String driver, String url, String user, String pass) {
		DBClient client = new DBClient(driver, url, user, pass);
		DBContextHolder.setDataSourceType(client);
	}
	
	public void clear(){
		DBContextHolder.clearDataSourceType();
	}

}