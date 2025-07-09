package com.sanluan.einvoice.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.text.PDFTextStripper;

import static com.sanluan.einvoice.service.PdfFullElectronicInvoiceService.getFullElectronicInvoice;
import static com.sanluan.einvoice.service.PdfRegularInvoiceService.getRegularInvoice;
import static com.sanluan.einvoice.utils.StringUtils.replace;

/**
 * 专用于处理电子发票识别的类
 * 
 * @author arthurlee
 *
 */

public class PdfInvoiceExtractor {

    public static Invoice extract(File file) throws IOException {

//        PDDocument doc = PDDocument.load(file);
        PDDocument doc = Loader.loadPDF(file);
        if(doc.getPages().getCount()==1){
            PDPage firstPage = doc.getPage(0);
            int pageWidth = Math.round(firstPage.getCropBox().getWidth());
            PDFTextStripper textStripper = new PDFTextStripper();
            textStripper.setSortByPosition(true);
            String fullText = textStripper.getText(doc);
            if (firstPage.getRotation() != 0) {
                pageWidth = Math.round(firstPage.getCropBox().getHeight());
            }
            String allText = replace(fullText).replaceAll("（", "(").replaceAll("）", ")").replaceAll("￥", "¥");
            if(allText.contains("电子发票")){
                // 全票
              return getFullElectronicInvoice(fullText,allText,pageWidth,doc,firstPage);
            }else {
               return getRegularInvoice(fullText,allText,pageWidth,doc,firstPage);
            }
        }else {
            Invoice invoice = new Invoice();
            PDPage firstPage = doc.getPage(doc.getPages().getCount()-1);
            int pageWidth = Math.round(firstPage.getCropBox().getWidth());
            PDFTextStripper textStripper = new PDFTextStripper();
            textStripper.setSortByPosition(true);
            String fullText = textStripper.getText(doc);
            if (firstPage.getRotation() != 0) {
                pageWidth = Math.round(firstPage.getCropBox().getHeight());
            }
            String allText = replace(fullText).replaceAll("（", "(").replaceAll("）", ")").replaceAll("￥", "¥");
            if(allText.contains("电子发票")){
                // 全票
                invoice = getFullElectronicInvoice(fullText,allText,pageWidth,doc,firstPage);
            }else {
                invoice = getRegularInvoice(fullText,allText,pageWidth,doc,firstPage);
            }
            List<Invoice> invoiceList = new ArrayList<>();
            for(int i=0;i<doc.getPages().getCount()-1;i++){
                doc = Loader.loadPDF(file);
                Invoice invoiceTemp = new Invoice();

                PDPage page = doc.getPage(i);
                pageWidth = Math.round(page.getCropBox().getWidth());
                if (page.getRotation() != 0) {
                    pageWidth = Math.round(page.getCropBox().getHeight());
                }
                if(allText.contains("电子发票")){
                    // 全票
                    invoiceTemp = getFullElectronicInvoice(fullText,allText,pageWidth,doc,page);
                }else {
                    invoiceTemp = getRegularInvoice(fullText,allText,pageWidth,doc,page);
                }
                invoiceList.add(invoiceTemp);
            }
            List<Detail> detailList =  new ArrayList<>();
            if(invoiceList.size()>0){
                invoiceList.forEach(invoiceTemp->{
                    detailList.addAll(invoiceTemp.getDetailList());
                });
            }
            List<Detail> detailListTemp = invoice.getDetailList();
            detailList.addAll(detailListTemp);
            invoice.setDetailList(detailList);
            return invoice;
        }
    }


}