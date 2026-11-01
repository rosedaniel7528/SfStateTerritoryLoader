package utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import models.StateData;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class ExcelDataReader {
	
	private static final DataFormatter formatter = new DataFormatter();

    public static  Map<String, List<StateData>> getCountryStateData(String filePath, String sheetName) {

        Map<String, List<StateData>> countryStateMap = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);

            int rowCount = sheet.getPhysicalNumberOfRows();

            for (int i = 1; i < rowCount; i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                String country = getCellValue(row.getCell(0));
                String region = getCellValue(row.getCell(1));
                String description = getCellValue(row.getCell(2));

                if (country.isEmpty()) continue;

                StateData state = new StateData(region, description);

                countryStateMap
                        .computeIfAbsent(country, k -> new ArrayList<>())
                        .add(state);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return countryStateMap;
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        return formatter.formatCellValue(cell).trim();
    }
}