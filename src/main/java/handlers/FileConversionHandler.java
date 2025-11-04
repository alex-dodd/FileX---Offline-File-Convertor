package handlers;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream; //NOTE TO SELF, CAN'T IMPORT ALL USING '*', DOES NOT WORK FOR SOME REASON
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

/**
 * This class handles all the file conversion operations.
 * It's bascially the engine of my application, doing all the heavy lifting and other hardd work.
 * It will support document, spreadsheet, and image format conversions.
 */
public class FileConversionHandler {

    // A Static block to register additional image formats
    static {
        // USing TwelveMonkeys ImageIO plugins that are loaded automatically through ServiceLoader
        // This enables WEBP support and enhanced format handling
    }

    /**
     * This converts a DOCX file to a PDF file.(Also used to support DOC until I learned that DOCX is the modern alternative)
     * I'm using Apache POI to read the DOCX and PDFBox to write the PDF.
     * @param sourceFile The source DOCX file.
     * @param targetFile The target PDF file.
     */
    public void convertDocxToPdf(File sourceFile, File targetFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(sourceFile);
            XWPFDocument document = new XWPFDocument(fis);
            PDDocument pdfDocument = new PDDocument()) {

            // Create a new page in the PDF
            PDPage page = new PDPage();
            pdfDocument.addPage(page);

            // Write the content to the PDF
            try (PDPageContentStream contentStream = new PDPageContentStream(pdfDocument, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.TIMES_ROMAN, 12);
                contentStream.setLeading(14.5f);
                contentStream.newLineAtOffset(25, 725);

                // Extract text from the DOCX and write it to the PDF
                for (var para : document.getParagraphs()) {
                    contentStream.showText(para.getText());
                    contentStream.newLine();
                }

                contentStream.endText();
            }

            // Save the new PDF document
            pdfDocument.save(targetFile);
        }
    }

    /**
     * IT converts a PDF file to a DOCX file.
     * This implementation extracts text from the PDF and writes it to a new DOCX document.
     * @param sourceFile The source PDF file.
     * @param targetFile The target DOCX file.
     */
    public void convertPdfToDocx(File sourceFile, File targetFile) throws IOException {
        try (PDDocument pdfDocument = PDDocument.load(sourceFile);
            XWPFDocument docxDocument = new XWPFDocument();
            FileOutputStream fos = new FileOutputStream(targetFile)) {

            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(pdfDocument);

            // Going to add the extracted text to the DOCX document
            docxDocument.createParagraph().createRun().setText(text);

            docxDocument.write(fos);
        }
    }

    /**
     * Converts a CSV file to an XLSX file.
     * Im using Apache POI to create an Excel workbook from CSV data.
     * @param sourceFile The source CSV file.
     * @param targetFile The target XLSX file.
     */
    public void convertCsvToXlsx(File sourceFile, File targetFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(sourceFile));
            Workbook workbook = new XSSFWorkbook();
            FileOutputStream fos = new FileOutputStream(targetFile)) {

            Sheet sheet = workbook.createSheet("Sheet1");
            String line;
            int rowNum = 0;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(","); // A somewhat simple comma split, learned it's more robust to use a CSV parser for production
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < data.length; i++) {
                    row.createCell(i).setCellValue(data[i]);
                }
            }
            workbook.write(fos);
        }
    }

    /**
     * This converts an XLSX file to a CSV file.
     * It extracts data from the first sheet of an Excel workbook and writes it to a CSV file.
     * @param sourceFile The source XLSX file.
     * @param targetFile The target CSV file.
     */
    public void convertXlsxToCsv(File sourceFile, File targetFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(sourceFile);
            Workbook workbook = new XSSFWorkbook(fis);
            FileWriter fw = new FileWriter(targetFile)) {

            Sheet sheet = workbook.getSheetAt(0); // Gets the first sheet
            for (Row row : sheet) {
                Iterator<Cell> cellIterator = row.cellIterator();
                StringBuilder rowData = new StringBuilder();
                for (Cell cell : row) {
                    switch (cell.getCellType()) {
                        case STRING -> rowData.append(cell.getStringCellValue());
                        case NUMERIC -> rowData.append(cell.getNumericCellValue());
                        case BOOLEAN -> rowData.append(cell.getBooleanCellValue());
                        default -> rowData.append("");
                    }
                    if (cellIterator.hasNext()) {
                        rowData.append(",");
                    }
                }
                fw.append(rowData.toString());
                fw.append("\n");
            }
        }
    }

    /**
     * Converts an image file from one format to another.
     * This is a generic method that can handle JPG, PNG, and WEBP conversions.
     * @param sourceFile The source image file.
     * @param targetFile The target image file.
     * @param targetFormat The target format (e.g., "jpg", "png", "webp").
     */
    public void convertImage(File sourceFile, File targetFile, String targetFormat) throws IOException {
        BufferedImage image = ImageIO.read(sourceFile);
        if (image == null) {
            throw new IOException("Could not read image from file: " + sourceFile.getAbsolutePath());
        }
        ImageIO.write(image, targetFormat, targetFile);
    }

    /**
     * My main conversion method that chanels to the appropriate specific converter.
     * This method determines the conversion type and calls the right handler.
     * @param sourceFile The source file to convert.
     * @param targetFile The target file to create.
     * @param targetFormat The target format (PDF, DOCX, CSV, XLSX, JPG, PNG, WEBP).
     * @return true if conversion was successful, false otherwise.
     */
    public boolean convertFile(File sourceFile, File targetFile, String targetFormat) {
        try {
            String sourceName = sourceFile.getName().toLowerCase();
            String targetFormatLower = targetFormat.toLowerCase();
            
            // Document conversions
            if (sourceName.endsWith(".docx") && targetFormatLower.equals("pdf")) {
                convertDocxToPdf(sourceFile, targetFile);
            } else if (sourceName.endsWith(".pdf") && targetFormatLower.equals("docx")) {
                convertPdfToDocx(sourceFile, targetFile);
            }
            // Spreadsheet conversions
            else if (sourceName.endsWith(".csv") && targetFormatLower.equals("xlsx")) {
                convertCsvToXlsx(sourceFile, targetFile);
            } else if (sourceName.endsWith(".xlsx") && targetFormatLower.equals("csv")) {
                convertXlsxToCsv(sourceFile, targetFile);
            }
            // Image conversions
            else if ((sourceName.endsWith(".jpg") || sourceName.endsWith(".jpeg") || sourceName.endsWith(".png") || sourceName.endsWith(".webp")) &&(targetFormatLower.equals("jpg") || targetFormatLower.equals("jpeg") || targetFormatLower.equals("png") || targetFormatLower.equals("webp"))) {
                convertImage(sourceFile, targetFile, targetFormatLower);
            } else {
                // Unsupported conversion
                return false;
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("Conversion failed: " + e.getMessage());
            return false;
        }
    }
}