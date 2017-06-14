package pkg.demo.common.pojo;

/**
 * Plugin for excel export
 *
 * <pre>
 *
 * 	Help to convert cell value to what you want against the methodName parameter matched with the property in the Export Object
 *
 * </pre>
 *
 * @author zhajiang
 *
 */
public interface ExportFieldCellConverter {
	public Object convert(String methodName, Object value);
}
