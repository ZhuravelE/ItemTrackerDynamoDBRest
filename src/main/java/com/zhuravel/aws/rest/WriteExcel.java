package com.zhuravel.aws.rest;

import com.zhuravel.aws.rest.model.FileAttributeItem;
import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Evgenii Zhuravel created on 25.10.2022
 */
@Component
public class WriteExcel {

    private WritableCellFormat timesBoldUnderline;
    private WritableCellFormat times;

    public java.io.InputStream exportExcel( List<FileAttributeItem> list) {
        try {
            return write(list);

        } catch(WriteException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public java.io.InputStream write( List<FileAttributeItem> list) throws IOException, WriteException {
        java.io.OutputStream os = new java.io.ByteArrayOutputStream() ;
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));

        WritableWorkbook workbook = Workbook.createWorkbook(os, wbSettings);
        workbook.createSheet("FileAttribute Item Report", 0);
        WritableSheet excelSheet = workbook.getSheet(0);
        createLabel(excelSheet) ;
        int size = createContent(excelSheet, list);

        workbook.write();
        workbook.close();

        java.io.ByteArrayOutputStream stream = new java.io.ByteArrayOutputStream();
        stream = (java.io.ByteArrayOutputStream)os;
        byte[] myBytes = stream.toByteArray();
        java.io.InputStream is = new java.io.ByteArrayInputStream(myBytes) ;
        return is ;
    }

    private void createLabel(WritableSheet sheet) throws WriteException {
        WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);

        times = new WritableCellFormat(times10pt);

        times.setWrap(true);

        WritableFont times10ptBoldUnderline = new WritableFont(WritableFont.TIMES, 10, WritableFont.BOLD, false, UnderlineStyle.SINGLE);
        timesBoldUnderline = new WritableCellFormat(times10ptBoldUnderline);

        timesBoldUnderline.setWrap(true);
        CellView cv = new CellView();
        cv.setFormat(times);
        cv.setFormat(timesBoldUnderline);
        cv.setAutosize(true);

        addCaption(sheet, 0, "Writer");
        addCaption(sheet, 1, "Date");
        addCaption(sheet, 2, "Guide");
        addCaption(sheet, 3, "Description");
        addCaption(sheet, 4, "Status");
    }

    private int createContent(WritableSheet sheet, List<FileAttributeItem> list) throws WriteException {

        int size = list.size() ;

        for (int i = 0; i < size; i++) {
            FileAttributeItem wi = list.get(i);

            String name = wi.getFilename();
            Long fileSize = wi.getSize();
            String date = wi.getDate();
            String url = wi.getUrl();

            addLabel(sheet, 0, i+2, name);
            addLabel(sheet, 1, i+2, date);
            addLabel(sheet, 2, i+2, fileSize.toString());
            addLabel(sheet, 3, i+2, url);
        }
        return size;
    }

    private void addCaption(WritableSheet sheet, int column, String s) throws WriteException {
        Label label;
        label = new Label(column, 0, s, timesBoldUnderline);
        int cc = countString(s);
        sheet.setColumnView(column, cc);
        sheet.addCell(label);
    }

    private void addLabel(WritableSheet sheet, int column, int row, String s) throws WriteException {
        Label label;
        label = new Label(column, row, s, times);
        int cc = countString(s);
        if (cc > 200)
            sheet.setColumnView(column, 150);
        else
            sheet.setColumnView(column, cc+6);

        sheet.addCell(label);
    }

    private int countString (String ss) {
        int count = 0;
        for(int i = 0; i < ss.length(); i++) {
            if(ss.charAt(i) != ' ')
                count++;
        }
        return count;
    }
}
