package ch.sleod.testautomation.framework.common.IOUtils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static ch.sleod.testautomation.framework.common.logging.SystemLogger.error;

/**
 * tool class for reading excel files with .xls, .xlsx
 */
public class ExcelFileLoader {

    /**
     * load excel file with path
     *
     * @param path to be read
     * @return workbook of excel
     */
    public static Workbook loadExcelFile(String path) {
        Workbook workbook = null;
        try {
            FileInputStream excelFile = new FileInputStream(new File(path));
            if (path.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(excelFile);
            } else {
                workbook = new HSSFWorkbook(excelFile);
            }
        } catch (IOException e) {
            error(e);
        }
        return workbook;
    }

    /**
     * @param workbook    excel
     * @param sheetName   sheet name case sensitive
     * @param startColumn startNow x vector position (default 0)
     * @param startRow    startNow y vector position (default 0)
     * @return list of rows in sheet
     */
    public static List<Map<String, Object>> readExcelTable(Workbook workbook, String sheetName, int startColumn, int startRow) {
        if (workbook == null) {
            throw new RuntimeException("Given Excel Sheet is null!");
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
                    break;
                case _NONE:
                    break;
                case ERROR:
                    break;
            }
            if (currentRow.getRowNum() == 0) {//first line for column name
                if (value == null) {
                    throw new RuntimeException("Column name should not be null!");
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
