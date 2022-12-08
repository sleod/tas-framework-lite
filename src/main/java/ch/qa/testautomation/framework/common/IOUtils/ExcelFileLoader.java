package ch.qa.testautomation.framework.common.IOUtils;

import ch.qa.testautomation.framework.exception.ApollonBaseException;
import ch.qa.testautomation.framework.exception.ApollonErrorKeys;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;


/**
 * tool class for reading Excel files with .xls, .xlsx
 */
public class ExcelFileLoader {

    /**
     * load Excel file with path
     *
     * @param path to be read
     * @return workbook of Excel
     */
    public static Workbook loadExcelFile(String path) {
        Workbook workbook = null;
        try {
            FileInputStream excelFile = new FileInputStream(path);
            if (path.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(excelFile);
            } else {
                workbook = new HSSFWorkbook(excelFile);
            }
        } catch (IOException ex) {
            throw new ApollonBaseException(ApollonErrorKeys.IOEXCEPTION_GENERAL, ex, "Exception while load Excel file: " + path);
        }
        return workbook;
    }

    /**
     * @param workbook    excel
     * @param sheetName   sheet name case-sensitive
     * @param startColumn startNow x vector position (default 0)
     * @param startRow    startNow y vector position (default 0)
     * @return list of rows in sheet
     */
    public static List<Map<String, Object>> readExcelTable(Workbook workbook, String sheetName, int startColumn, int startRow) {
        if (workbook == null) {
            throw new ApollonBaseException(ApollonErrorKeys.NULL_EXCEPTION, "Excel Sheet");
        }
        List<Map<String, Object>> table = new LinkedList<>();
        Sheet dataTypeSheet = workbook.getSheet(sheetName);
        Iterator<Row> rowIterator = dataTypeSheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row currentRow = rowIterator.next();
            int rowNumber = currentRow.getRowNum();
            if (rowNumber < startRow) {
                continue;
            }
            Map<String, Object> row = fetchRow(currentRow, startColumn);
            if (!row.isEmpty()) {
                table.add(row);
            }
        }
        return table;
    }

    /**
     * fetch row content into a map
     *
     * @param currentRow  row to be fetched
     * @param startColumn index of start column
     * @return row in map
     */
    private static Map<String, Object> fetchRow(Row currentRow, int startColumn) {
        LinkedList<String> keySet = new LinkedList<>();
        Map<String, Object> row = new LinkedHashMap<>();
        for (Cell currentCell : currentRow) {
            int columnIndex = currentCell.getColumnIndex();
            if (columnIndex < startColumn) {
                continue;
            }
            Object value = null;
            switch (currentCell.getCellType()) {
                case STRING:
                    value = currentCell.getStringCellValue();
                    break;
                case NUMERIC:
                    value = currentCell.getNumericCellValue();
                    break;
                case BOOLEAN:
                    value = currentCell.getBooleanCellValue();
                    break;
                case FORMULA:
                    value = currentCell.getCellFormula();
                    break;
                case BLANK:
                case _NONE:
                case ERROR:
                    break;
            }
            if (currentRow.getRowNum() == 0) {//first line for column name
                if (value == null) {
                    throw new ApollonBaseException(ApollonErrorKeys.NULL_EXCEPTION, "Column Name");
                }
                keySet.add(String.valueOf(value));
            } else {
                if (columnIndex < keySet.size()) {
                    String colName = keySet.get(columnIndex);
                    row.put(colName, value);
                }
            }
        }
        return row;
    }
}
