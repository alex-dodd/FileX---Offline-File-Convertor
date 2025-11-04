# Technical Documentation - FileX Offline File Converter

## Section 1.1.1: Externally Sourced Code

| Code Snippet/Section | Source/Link | AI Assistance | % of Total Code |
|---------------------|-------------|---------------|----------------|
| JavaFX Framework | https://openjfx.io/ | No | 15% |
| Apache POI Library | https://poi.apache.org/ | No | 10% |
| Apache PDFBox Library | https://pdfbox.apache.org/ | No | 10% |
| SQLite JDBC Driver | https://github.com/xerial/sqlite-jdbc | No | 5% |
| Zip4j Library | https://github.com/srikanth-lingala/zip4j | No | 5% |

**Note:** All external libraries are used through standard Maven dependencies. The application logic and integration code (approximately 55% of the codebase) is original student work.

## Section 1.1.2: Explanation of Critical Algorithms

| Algorithm Name | Pseudocode (IEB format) | Explanation | AI Assistance |
|---------------|------------------------|-------------|---------------|
| File Conversion Router | BEGIN<br>  INPUT sourceFile, targetFile, targetFormat<br>  sourceName = GET filename from sourceFile<br>  IF sourceName ends with ".docx" AND targetFormat = "pdf" THEN<br>    CALL convertDocxToPdf<br>  ELSE IF sourceName ends with ".pdf" AND targetFormat = "docx" THEN<br>    CALL convertPdfToDocx<br>  ELSE IF sourceName ends with ".csv" AND targetFormat = "xlsx" THEN<br>    CALL convertCsvToXlsx<br>  ELSE IF sourceName ends with ".xlsx" AND targetFormat = "csv" THEN<br>    CALL convertXlsxToCsv<br>  ELSE IF source is image AND target is image format THEN<br>    CALL convertImage<br>  ELSE<br>    RETURN false<br>  END IF<br>  RETURN true<br>END | This algorithm determines which specific conversion method to call based on the source file extension and target format. It acts as a router that directs files to the appropriate converter. | No |
| CSV to XLSX Conversion | BEGIN<br>  OPEN CSV file for reading<br>  CREATE new Excel workbook<br>  CREATE sheet named "Sheet1"<br>  rowNum = 0<br>  WHILE there are lines in CSV file DO<br>    READ line from CSV<br>    SPLIT line by comma into data array<br>    CREATE row at rowNum<br>    FOR each item in data array DO<br>      CREATE cell and SET value to item<br>    END FOR<br>    INCREMENT rowNum<br>  END WHILE<br>  WRITE workbook to target file<br>  CLOSE all files<br>END | This algorithm reads a CSV file line by line, splits each line by commas, and writes the data into an Excel spreadsheet. Each row in the CSV becomes a row in the spreadsheet. | No |
| XLSX to CSV Conversion | BEGIN<br>  OPEN Excel workbook<br>  GET first sheet from workbook<br>  OPEN CSV file for writing<br>  FOR each row in sheet DO<br>    rowData = empty string<br>    FOR each cell in row DO<br>      IF cell type is STRING THEN<br>        ADD cell string value to rowData<br>      ELSE IF cell type is NUMERIC THEN<br>        ADD cell numeric value to rowData<br>      ELSE IF cell type is BOOLEAN THEN<br>        ADD cell boolean value to rowData<br>      END IF<br>      IF not last cell THEN<br>        ADD comma to rowData<br>      END IF<br>    END FOR<br>    WRITE rowData to CSV file<br>    ADD newline<br>  END FOR<br>  CLOSE all files<br>END | This algorithm extracts data from an Excel spreadsheet and writes it to a CSV file. It processes each cell based on its type (text, number, or boolean) and separates values with commas. | No |
| ZIP Folder with Encryption | BEGIN<br>  INPUT sourceFolder, targetZipFile, password<br>  CREATE new ZipFile object for targetZipFile<br>  CREATE ZipParameters<br>  IF password is not empty THEN<br>    SET encrypt files to true<br>    SET encryption method to AES<br>    SET password on zip file<br>  END IF<br>  ADD sourceFolder to zip file with parameters<br>  RETURN success<br>END | This algorithm compresses a folder into a ZIP file with optional AES encryption. If a password is provided, it enables encryption to protect the contents. The zip4j library handles the compression and encryption. | No |

## Section 1.1.3: Advanced Techniques

The following advanced techniques (not part of the standard syllabus) are used in this application:

### 1. Threads
**Pseudocode:**
```
BEGIN Thread Implementation
  CREATE Task object for background operation
  DEFINE call method:
    PERFORM long-running operation
    RETURN result
  END DEFINE
  
  DEFINE succeeded method:
    UPDATE UI with success message
    LOG result to database
  END DEFINE
  
  DEFINE failed method:
    UPDATE UI with error message
    SHOW error dialog
  END DEFINE
  
  CREATE new Thread with Task
  START thread
END
```

**Explanation:** The application uses JavaFX Task and Thread classes to perform file conversions and ZIP operations in the background. This prevents the user interface from freezing during long operations. When a conversion starts, a new thread is created to handle the work while the main thread keeps the UI responsive.

### 2. Encryption and Decryption of Data
**Pseudocode:**
```
BEGIN Encryption
  INPUT sourceFolder, targetZipFile, password
  CREATE ZipFile object
  CREATE ZipParameters
  SET encryptFiles to true
  SET encryptionMethod to AES
  SET password as character array
  ADD folder to zip with encryption
  SAVE encrypted zip file
END
```

**Explanation:** The application uses AES (Advanced Encryption Standard) encryption to protect ZIP files with passwords. When a user selects the encryption option and provides a password, all files in the ZIP are encrypted using the AES algorithm, making them unreadable without the correct password.

### 3. Inheritance
**Pseudocode:**
```
BEGIN Inheritance Structure
  CLASS MainUIController IMPLEMENTS Initializable
    INHERIT initialize method from Initializable
    OVERRIDE initialize method:
      CALL setupTabPane
      CALL setupFileConverter
      CALL setupZipConverter
    END OVERRIDE
  END CLASS
END
```

**Explanation:** The application uses inheritance through interface implementation. The MainUIController class implements the Initializable interface, which requires an initialize method. This allows JavaFX to automatically set up the controller when the UI loads. This follows object-oriented programming principles by defining a contract that controller classes must follow.
