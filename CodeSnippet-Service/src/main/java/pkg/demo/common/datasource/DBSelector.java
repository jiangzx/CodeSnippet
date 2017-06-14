package pkg.demo.common.datasource;

import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.google.common.collect.MapMaker;
/**
 * Datasource selector
 * @author zhajiang
 *
 */
public class DBSelector extends AbstractRoutingDataSource {

	private Map<DBClient, DataSource> map = new MapMaker().makeMap();

	@Override
	protected Object determineCurrentLookupKey() {
		return DBContextHolder.getDataSourceType();
	}

	@Override
	protected DataSource determineTargetDataSource() {
		DBClient client = (DBClient) determineCurrentLookupKey();
		if (map.containsKey(client)) {
			return map.get(client);
		}

		BasicDataSource ds = new BasicDataSource();

		ds.setDriverClassName(client.getDbDriver());
		ds.setUrl(client.getDbUrl());
		ds.setUsername(client.getDbUser());
		ds.setPassword(client.getDbPass());
		ds.setMaxActive(50);
		ds.setMinIdle(5);
		ds.setMaxWait(4000);
		ds.setTestWhileIdle(true);

		ds.setValidationQuery("select 1 from dual");

		map.put(client, ds);

		return ds;

	}

	@Override
	public void afterPropertiesSet() {
	}
}
