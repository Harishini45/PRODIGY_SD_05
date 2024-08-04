package com.example.scrap;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

public class FlipkartScraper {
    private static final int MAX_RETRIES = 3;
    private static final int TIMEOUT = 60000;
    public static void main(String[] args) {
        String url = "https://www.flipkart.com/search?q=laptops";
        String excelFilePath = "FlipkartProducts.xlsx";
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Products");
            int rowNum = 0;
            
            // Create header row
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("Product Name");
            headerRow.createCell(1).setCellValue("Price");
            headerRow.createCell(2).setCellValue("Ratings");
           // headerRow.createCell(3).setCellValue("Reviews");
            
            // Fetch the HTML content
           // Document document = Jsoup.connect(url).get();
            Document document = fetchDocumentWithRetry(url);            
            // Parse the HTML to extract product details
            if (document != null) {
            Elements products = document.select(".yKfJKb"); 
            for (Element product : products) {
                String productName = product.select(".KzDlHZ").text();
                String price = product.select(".Nx9bqj").text(); 
                String ratings = product.select(".Wphh3N").text(); 
               // String reviews = product.select(".hGSR34").text(); 
                
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(productName);
                row.createCell(1).setCellValue(price);
                row.createCell(2).setCellValue(ratings);
              //  row.createCell(3).setCellValue(reviews);
            }
            
            // Write the data to the Excel file
            try (FileOutputStream fileOut = new FileOutputStream(excelFilePath)) {
                workbook.write(fileOut);
            }
        }
            else{
            	 System.out.println("Failed to fetch the document after multiple attempts.");
            }
        }
            	
            	catch (IOException e) {
            
            e.printStackTrace();}
            
    }
    private static Document fetchDocumentWithRetry(String url) {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                return Jsoup.connect(url)
                        .timeout(TIMEOUT)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .referrer("http://www.google.com")
                        .get();
            } catch (SocketTimeoutException e) {
                attempt++;
                System.out.println("Attempt " + attempt + " timed out, retrying...");
            } catch (IOException e) {
                if (e.getMessage().contains("Status=500")) {
                    attempt++;
                    System.out.println("Attempt " + attempt + " received HTTP 500, retrying...");
                } else {
                    e.printStackTrace();
                    break;
                }
            }
        }
        return null;
    }
}
