package pkg.demo.common.datasource;
/**
 * Datasource Context
 * @author zhajiang
 *
 */
public class DBContextHolder {
	private static final ThreadLocal<DBClient> contextHolder = new ThreadLocal<DBClient>();

	public static void setDataSourceType(DBClient client) {
		contextHolder.set(client);
	}

	public static DBClient getDataSourceType() {
		return contextHolder.get();
	}

	public static void clearDataSourceType() {
		contextHolder.remove();
	}

}
