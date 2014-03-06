package uma.util

import uma.util.Ulog;

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook


class XlsUtil {
    static String cell2Str(Cell cell) {
        def result;
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                result = cell.getRichStringCellValue().getString();
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    result = cell.getDateCellValue() + "";
                } else {
                    result = cell.getNumericCellValue();
                }
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                result = cell.getBooleanCellValue() + "";
                break;
            case Cell.CELL_TYPE_FORMULA:
                result = cell.getCellFormula();
                break;
            default:
                break;
        }
        return result;
    }

    /** １行目の項目をキーにしたリストを返却 */
    static List<Map> cell2ListMap(String fname) {
        List<Map> list = new ArrayList<Map>();

        InputStream inp = new FileInputStream(fname);
        String fileExtn = GetFileExtension(fname);
        Workbook wb_xssf; //Declare XSSF WorkBook
        Workbook wb_hssf; //Declare HSSF WorkBook
        Sheet sheet = null; // sheet can be used as common for XSSF and HSSF WorkBook
        if (fileExtn.equalsIgnoreCase("xlsx")) {
            wb_xssf = new XSSFWorkbook(inp);
//            Log.debug("xlsx=" + wb_xssf.getSheetName(0));
            sheet = wb_xssf.getSheetAt(0);
        }
        if (fileExtn.equalsIgnoreCase("xls")) {
            POIFSFileSystem fs = new POIFSFileSystem(inp);
            wb_hssf = new HSSFWorkbook(fs);
            Ulog.info("xls=" + wb_hssf.getSheetName(0));
            sheet = wb_hssf.getSheetAt(0);
        }
        Iterator rows = sheet.rowIterator(); // Now we have rows ready from the sheet

        List colList = new ArrayList(); // 何番目が何のカラム名かのリスト
        while (rows.hasNext()){
            Row row = (Row) rows.next();
            int rowNum = row.getRowNum();
            Iterator cells = row.cellIterator();
            Map map = new HashMap(); // key:カラム名 value:Cell値
            if(rowNum == 0){
               colList = new ArrayList();
            }
            while (cells.hasNext()){
                Cell cell = (Cell) cells.next();
                if(cell.columnIndex>6){break;}
                def result = cell2Str(cell);
                if(rowNum == 0){
                    colList.add(result);
                } else {
                    map.put(colList.get(cell.columnIndex), result);
                }
            }
//            Log.debug "$colList";
//            Log.debug "▲$map";
            if(rowNum != 0){
                list.add(map);
            }
        }
        return list;
    }

    private static String GetFileExtension(String fname2) {
        String fileName = fname2;
        String fname = "";
        String ext = "";
        int mid = fileName.lastIndexOf(".");
        fname = fileName.substring(0, mid);
        ext = fileName.substring(mid + 1, fileName.length());
        return ext;
    }
}
