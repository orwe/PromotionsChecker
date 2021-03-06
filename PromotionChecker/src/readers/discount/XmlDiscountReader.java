 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package readers.discount;

import db.entities.Discounts;
import exceptions.ProcessingException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

/**
 *
 * @author Miro
 */
public class XmlDiscountReader implements IDiscountReader {

    private HSSFWorkbook workbook;
    private HSSFSheet sheet;
    private Iterator<Row> rowIterator;
    private int currentRow;
    private int currentSheet;
    
    public XmlDiscountReader(FileInputStream xml) throws IOException {
        workbook = new HSSFWorkbook(xml);
        prepareSheet(0);
    }
    
    public Discounts getNext() {
        if (!hasNext()){
            return null;
        }
        Row row = rowIterator.next();
        currentRow++;
        String name = null;
        try {
            Iterator<Cell> cellIterator = row.cellIterator();
            Cell cell = cellIterator.next();
            if (cell.getCellType() == Cell.CELL_TYPE_BLANK){
                return getNext();
            }
            name = cell.getStringCellValue();
            double percentage = getPercentage(cellIterator.next());
            return new Discounts(name, percentage);
        } catch (Exception ex){
            String message = "SKOROSZYT: " + (currentSheet + 1);
            if (name != null){
                message += " NAZWA PRODUKTU: " + name;
            } else {
                message += " LINIA: " + currentRow;
            }
            throw new RuntimeException(message);
        }
    }
    
    public boolean hasNext() {
        if (rowIterator.hasNext()){
            return true;
        }
        
        if (currentSheet + 1 < workbook.getNumberOfSheets()){
            prepareSheet(currentSheet + 1);
            return hasNext();
        }
        return false;
    }
    
    private void prepareSheet(int number){
        sheet = workbook.getSheetAt(number);
        currentSheet = number;
        rowIterator = sheet.iterator();
        //skip headers
        if (rowIterator.hasNext()){
            checkSheet(rowIterator.next());
            currentRow = 1;
        }
    }

    private double getPercentage(Cell cell) {
        switch(cell.getCellType()){
            case Cell.CELL_TYPE_NUMERIC:
                return percentageFrom(cell.getNumericCellValue());
            case Cell.CELL_TYPE_STRING:
                return percentageFrom(cell.getStringCellValue());
            default:
                throw new RuntimeException();
        }
    }
    
    private double percentageFrom(String value){
        return Double.valueOf(value.trim().replace("%", ""));
    }
    
    private double percentageFrom(double value){
        if (value < 1){
            return value * 100;
        }
        return value;
    }
    
    private void checkSheet(Row row) {
        if (row.getLastCellNum() > 4){
            throw new ProcessingException("Zła ilość kolumn w pliku.");
        }
    }
}
