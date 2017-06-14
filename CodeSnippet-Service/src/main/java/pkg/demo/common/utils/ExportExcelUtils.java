package pkg.demo.common.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pkg.demo.common.pojo.ExportFieldCellConverter;


/**
 * support => xlsx simple excel export utils (one or more sheets supported)
 *
 * <pre>
 * +++ for instance :
 *		OutputStream out = response.getOutputStream();
 * 		ExportExcelSimplePlus<SomeBean> bean = new ExportExcelSimplePlus<SomeBean>();
 * 		bean.createSheet('tit1',new String[]{"h1","h2","h3"},collections,out,"yyyy-mm-dd"); // create first  sheet
 * 		bean.createSheet('tit2',new String[]{"h1","h2","h3"},collections,out,"yyyy-mm-dd"); // create second sheet
 * 		bean.export(out);
 *		+++
 * 		more example plz refer to class ExportExcelController.downloadBridgeWebexUrlTsVip
 *
 * </pre>
 *
 * @author zhajiang
 *
 * @param <T>
 */
public class ExportExcelUtils<T> {

	public static final String FILE_SEPARATOR = System.getProperties().getProperty("file.separator");
	public static DecimalFormat df = new DecimalFormat("0");

	private XSSFWorkbook workbook;

	private XSSFCellStyle style;
	private XSSFCellStyle style2;
	private List<String> fields;
	private Field[] namefields;
	private XSSFDrawing patriarch;
	private ExportFieldCellConverter converter;

	private int defSheetWidth = 30;

	private static final Logger logger = LoggerFactory.getLogger(ExportExcelUtils.class);

	public ExportExcelUtils() {
		this.workbook = new XSSFWorkbook();
		TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
		createCellStyle();
		createCellStyle2();
	}

	public ExportExcelUtils(ExportFieldCellConverter converter) {
		this.converter = converter;
		this.workbook = new XSSFWorkbook();
		TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
		createCellStyle();
		createCellStyle2();
	}

	public ExportExcelUtils(XSSFWorkbook workbook) {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
		this.workbook = workbook;
		createCellStyle();
		createCellStyle2();
	}

	public void setExclude(List<String> fields) {
		this.fields = fields;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void createSheet(String title, String[] headers, Collection<T> dataset, OutputStream out, String pattern) {
		XSSFSheet sheet = workbook.createSheet(title);
		sheet.setDefaultColumnWidth(this.getDefSheetWidth());

		// image manager
		if (this.patriarch == null) {
			this.patriarch = sheet.createDrawingPatriarch();
		}
		// excel title
		XSSFRow row = sheet.createRow(0);
		for (int i = 0; i < headers.length; i++) {
			XSSFCell cell = row.createCell(i);
			cell.setCellStyle(style);
			XSSFRichTextString text = new XSSFRichTextString(headers[i]);
			cell.setCellValue(text);
		}

		// excel value list
		Iterator<T> it = dataset.iterator();
		int index = 0;

		while (it.hasNext()) {
			index++;
			row = sheet.createRow(index);
			T t = it.next();
			if( t.getClass().equals(ArrayList.class) ) // T List<String>
			{
				int j = 0;
				for(String value: (ArrayList<String>)t)
				{
					try {
						XSSFCell cell = row.createCell(j);
						cell.setCellStyle(style2);
						Pattern p = Pattern.compile("^//d+(//.//d+)?$");
						Matcher matcher = p.matcher(value);
						if (matcher.matches()) {
							// convert digitals to double type
							cell.setCellValue(Double.parseDouble(value));
						} else {
							XSSFRichTextString richString = new XSSFRichTextString(value);
							XSSFFont font3 = workbook.createFont();
							font3.setColor(HSSFColor.BLUE.index);
							richString.applyFont(font3);
							cell.setCellValue(richString);
						}
						j++;
					}catch (Exception e) {
						logger.error("ExportExcelSimplePlus - createSheet: " + e);
					} finally {
						// do something clear
					}
				}
			}
			else
			{
				// get property value defined in class
				if (this.namefields == null) {
					this.namefields = ReflectUtils.getFields(t);
				}
				// cell index
				int j = 0;
				for (Field namefield : this.namefields) {
					String fieldName = namefield.getName();
					if (this.fields != null && this.fields.indexOf(fieldName) != -1) {
						continue;
					}
					XSSFCell cell = row.createCell(j);
					cell.setCellStyle(style2);
					String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
					try {
						Class tCls = t.getClass();
						Method getMethod = tCls.getMethod(getMethodName, new Class[] {});
						Object value = getMethod.invoke(t, new Object[] {});
	
						// add a cell converter
						if (this.converter != null) {
							value = this.converter.convert(getMethodName, value);
						}
	
						// convert value to text value
						String textValue = null;
						if (value instanceof Boolean) { // boolean
							boolean bValue = (Boolean) value;
							textValue = "1";
							if (!bValue) {
								textValue = "0";
							}
						} else if (value instanceof Date) { // date
							Date date = (Date) value;
							SimpleDateFormat sdf = new SimpleDateFormat(pattern);
							textValue = sdf.format(date);
						} else if (value instanceof byte[]) { // images
							// set line-height = 60px;
							row.setHeightInPoints(60);
							// set width = 35.7 * 80
							sheet.setColumnWidth(j, (short) (35.7 * 80));
							// sheet.autoSizeColumn(i);
							byte[] bsValue = (byte[]) value;
							XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 1023, 255, (short) 6, index, (short) 6,
									index);
							anchor.setAnchorType(AnchorType.MOVE_DONT_RESIZE);
							this.patriarch.createPicture(anchor,
									workbook.addPicture(bsValue, HSSFWorkbook.PICTURE_TYPE_JPEG));
						} else { // others
							// convert to simple string
							if (value == null) {
								textValue = "";
							} else {
								textValue = value.toString();
							}
						}
	
						if (textValue != null) {
							Pattern p = Pattern.compile("^//d+(//.//d+)?$");
							Matcher matcher = p.matcher(textValue);
							if (matcher.matches()) {
								// convert digitals to double type
								cell.setCellValue(Double.parseDouble(textValue));
							} else {
								XSSFRichTextString richString = new XSSFRichTextString(textValue);
								XSSFFont font3 = workbook.createFont();
								font3.setColor(HSSFColor.BLUE.index);
								richString.applyFont(font3);
								cell.setCellValue(richString);
							}
						}
	
						// column index increatment
						j++;
	
					} catch (Exception e) {
						logger.error("ExportExcelSimplePlus - createSheet: " + e);
					} finally {
						// do something clear
					}
				}
			}
		}
	}

	public void export(OutputStream out) {
		try {
			workbook.write(out);
			out.flush();
		} catch (IOException e) {
			logger.error("ExportExcelSimplePlus - export: " + e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logger.error("ExportExcelSimplePlus - export: " + e);
				}
			}
		}
	}

	private void createCellStyle() {
		XSSFCellStyle style = workbook.createCellStyle();
		style.setFillForegroundColor(HSSFColor.SKY_BLUE.index);
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		XSSFFont font = workbook.createFont();
		font.setColor(HSSFColor.VIOLET.index);
		font.setFontHeightInPoints((short) 12);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

		style.setFont(font);
		this.style = style;
	}

	private void createCellStyle2() {
		XSSFCellStyle style2 = workbook.createCellStyle();
		style2.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
		style2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		style2.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style2.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style2.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style2.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style2.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		style2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);

		XSSFFont font2 = workbook.createFont();
		font2.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);

		style2.setFont(font2);
		this.style2 = style2;
	}

	public XSSFWorkbook getWorkbook() {
		return workbook;
	}

	public void setWorkbook(XSSFWorkbook workbook) {
		this.workbook = workbook;
	}

	public int getDefSheetWidth() {
		return defSheetWidth;
	}

	public void setDefSheetWidth(int defSheetWidth) {
		this.defSheetWidth = defSheetWidth;
	}

	// ~~~~~~~~~~~ utils ~~~~~~~~~~~~~~

	public static String getCellValue(Cell cell) {
		Object val = "";
		switch (cell.getCellType()) {
			case Cell.CELL_TYPE_STRING:
				val = cell.getStringCellValue();
				break;
			case Cell.CELL_TYPE_NUMERIC:
				val = df.format(cell.getNumericCellValue());
				break;
			case Cell.CELL_TYPE_BOOLEAN:
				val = cell.getBooleanCellValue();
				break;
			default:

		}
		return val.toString();
	}

}
