package Utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
public class Negative_Data_Extractor {
    // Method to read data from Excel file and return as Object[][]
    public static Object[][] ExcelData(String sheetName) {
        Object[][] data = null;
        try (FileInputStream fis = new FileInputStream("src/test/java/TestData/Autochek_Address_Modification_Negative-api-data.xlsx");
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            int rowCount = sheet.getPhysicalNumberOfRows();
            int colCount = sheet.getRow(0).getPhysicalNumberOfCells();

            // Create the data array
            data = new Object[rowCount - 1][colCount];

            // Start from the second row (index 1)
            for (int i = 1; i < rowCount; i++) {
                for (int j = 0; j < colCount; j++) {
                    // Read cell value and store in the array
                    data[i - 1][j] = formatCellValue(sheet.getRow(i).getCell(j));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
    // Method to format cell value
    private static Object formatCellValue(Cell cell) {
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }
}
