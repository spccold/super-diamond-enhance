package com.github.diamond.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.github.diamond.web.model.ModuleDetail;
import com.github.diamond.web.model.ModuleTemplate;

public class ExcelUtils {
    private static final Logger LOGGER                  = LoggerFactory.getLogger(ExcelUtils.class);

    private static final String EXCEL_SHEET_NAME_PREFIX = "on-";

    private static final String TAG0                    = "module_name";
    private static final String TAG1                    = "config_key";
    private static final String TAG2                    = "config_value";
    private static final String TAG3                    = "config_desc";
    private static final String TAG4                    = "config_type";
    private static final String TAG5                    = "visable_type";

    /**
     * 从Excel中获取配置模板信息
     * 
     * @param is
     * @return
     */
    public static List<ModuleTemplate> getTemplateFromExcel(InputStream is) {
        List<ModuleTemplate> templates = new ArrayList<ModuleTemplate>();
        List<ModuleDetail> details = null;
        ModuleTemplate template = null;
        ModuleDetail moduleDetail = null;
        Sheet sheet = null;
        Row row = null;
        try {
            Workbook wb = new XSSFWorkbook(is);
            int sheets = wb.getNumberOfSheets();
            for (int i = 0; i < sheets; i++) {
                sheet = wb.getSheetAt(i);

                if (sheet.getSheetName().startsWith("on")) {
                    int rows = sheet.getPhysicalNumberOfRows();

                    //创建模板
                    template = new ModuleTemplate();
                    template.setModuleName(retrieveDataFromCellType(sheet.getRow(1).getCell(0)));
                    details = new ArrayList<ModuleDetail>();
                    //j = 0为列名,无须读取
                    for (int j = 1; j < rows; j++) {
                        row = sheet.getRow(j);

                        moduleDetail = new ModuleDetail();
                        moduleDetail.setConfigKey(retrieveDataFromCellType(row.getCell(1)));
                        moduleDetail.setConfigValue(retrieveDataFromCellType(row.getCell(2)));
                        moduleDetail.setConfigDesc(retrieveDataFromCellType(row.getCell(3)));
                        moduleDetail.setConfigType(retrieveDataFromCellType(row.getCell(4)));
                        moduleDetail.setVisableType(retrieveDataFromCellType(row.getCell(5)));
                        details.add(moduleDetail);
                    }
                    template.setModuleDetails(details);
                    templates.add(template);
                }
            }
        } catch (Exception e) {
            LOGGER.error("读取Excel失败!", e);
            throw new RuntimeException(e);
        }

        return templates;
    }

    public static Workbook exportExcelFromModuleTemplate(List<ModuleTemplate> templates) {
        int sheetNameSuffixNumber = 0;
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = null;
        Row row = null;
        try {
            List<ModuleDetail> moduleDetails = null;
            if (!CollectionUtils.isEmpty(templates)) {

                for (ModuleTemplate template : templates) {
                    moduleDetails = template.getModuleDetails();
                    if (!CollectionUtils.isEmpty(moduleDetails)) {
                        //row start index
                        int j = 0;
                        sheet = wb.createSheet(EXCEL_SHEET_NAME_PREFIX + (++sheetNameSuffixNumber));
                        //设置sheet的宽度
                        sheet.setColumnWidth(0, 5766);
                        sheet.setColumnWidth(1, 5766);
                        sheet.setColumnWidth(2, 5766);
                        sheet.setColumnWidth(3, 5766);
                        sheet.setColumnWidth(4, 5766);
                        sheet.setColumnWidth(5, 5766);
                        //在sheet中创建一行
                        row = sheet.createRow(0);
                        row.createCell(0).setCellValue(TAG0);
                        row.createCell(1).setCellValue(TAG1);
                        row.createCell(2).setCellValue(TAG2);
                        row.createCell(3).setCellValue(TAG3);
                        row.createCell(4).setCellValue(TAG4);
                        row.createCell(5).setCellValue(TAG5);
                        for (ModuleDetail detail : moduleDetails) {
                            row = sheet.createRow(++j);
                            row.createCell(0).setCellValue(template.getModuleName());
                            row.createCell(1).setCellValue(detail.getConfigKey());
                            row.createCell(2).setCellValue(detail.getConfigValue());
                            row.createCell(3).setCellValue(detail.getConfigDesc());
                            row.createCell(4).setCellValue(detail.getConfigType());
                            row.createCell(5).setCellValue(detail.getVisableType());
                        }
                    }
                }

            }
        } catch (Exception e) {
            LOGGER.error("创建Excel失败!", e);
            throw new RuntimeException(e);
        }
        return wb;
    }

    //just test for poi
    public static Workbook exportExcelFromModuleTemplate2(List<ModuleTemplate> templates) {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet(EXCEL_SHEET_NAME_PREFIX);
        sheet.setColumnWidth(0, 5766);
        sheet.setColumnWidth(1, 5766);
        sheet.setColumnWidth(2, 5766);
        sheet.setColumnWidth(3, 5766);
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue(TAG0);
        row.createCell(1).setCellValue(TAG1);
        row.createCell(2).setCellValue(TAG2);
        row.createCell(3).setCellValue(TAG3);
        return wb;
    }

    private static String retrieveDataFromCellType(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                return NumberToTextConverter.toText(cell.getNumericCellValue());
            case Cell.CELL_TYPE_STRING:
                return cell.getRichStringCellValue().toString();
            default:
                throw new RuntimeException("不支持的数据类型");
        }

    }

    public static void main(String[] args) throws IOException {
        List<ModuleTemplate> templates = getTemplateFromExcel(new FileInputStream(new File(
            "C:\\Users\\Administrator\\Desktop\\redis_module.xlsx")));
        for (ModuleTemplate template : templates) {
            System.out.println(template.getModuleName());
            for (ModuleDetail detail : template.getModuleDetails()) {
                System.out.println(detail);
            }
        }

        OutputStream os = new FileOutputStream(new File("C:\\Users\\Administrator\\Desktop\\cs_export.xlsx"));
        exportExcelFromModuleTemplate(templates).write(os);
        os.close();

    }
}
