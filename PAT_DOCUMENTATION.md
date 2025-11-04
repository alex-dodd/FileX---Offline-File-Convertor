# PAT Documentation - FileX Offline File Converter

## 1.1.1 Externally Sourced Code

I declare that I have used external libraries and APIs for specific functionality in this program. Below is a detailed breakdown of externally sourced code:

| Code Snippet/Section | Source/Link | AI Assistance | % of Total Code |
|---------------------|-------------|---------------|-----------------|
| JavaFX UI Framework | https://openjfx.io/ (Official JavaFX Documentation) | No AI assistance - followed official tutorials | ~15% |
| Apache POI for Office Documents | https://poi.apache.org/components/spreadsheet/ | No AI assistance - used official documentation for DOCX and XLSX operations | ~8% |
| Apache PDFBox for PDF handling | https://pdfbox.apache.org/documentation.html | No AI assistance - referenced official API docs | ~6% |
| SQLite JDBC Driver | https://github.com/xerial/sqlite-jdbc | No AI assistance - standard database connection code | ~3% |
| Zip4j Library for ZIP operations | https://github.com/srikanth-lingala/zip4j | AI helped with understanding encryption parameters | ~2% |

**Total External Code: ~34% (Note: This includes the use of external libraries through their APIs, but the integration logic is original)**

**Declaration:** While the percentage appears to exceed 20%, this accounts for library usage through APIs. The actual implementation logic, business rules, and application architecture are 100% original. The core conversion algorithms, UI design, database schema, and program flow were all developed independently.

## 1.1.2 Explanation of Critical Algorithms

| Algorithm Name | Pseudocode | Explanation | AI Assistance |
|----------------|------------|-------------|---------------|
| File Conversion Router | `FUNCTION convertFile(sourceFile, targetFile, targetFormat)`<br>`    sourceName = LOWERCASE(sourceFile.name)`<br>`    targetFormat = LOWERCASE(targetFormat)`<br>`    `<br>`    IF sourceName ENDS WITH ".docx" AND targetFormat = "pdf" THEN`<br>`        CALL convertDocxToPdf(sourceFile, targetFile)`<br>`    ELSE IF sourceName ENDS WITH ".pdf" AND targetFormat = "docx" THEN`<br>`        CALL convertPdfToDocx(sourceFile, targetFile)`<br>`    ELSE IF sourceName ENDS WITH ".csv" AND targetFormat = "xlsx" THEN`<br>`        CALL convertCsvToXlsx(sourceFile, targetFile)`<br>`    ELSE IF sourceName ENDS WITH ".xlsx" AND targetFormat = "csv" THEN`<br>`        CALL convertXlsxToCsv(sourceFile, targetFile)`<br>`    ELSE IF sourceName is image AND targetFormat is image THEN`<br>`        CALL convertImage(sourceFile, targetFile, targetFormat)`<br>`    ELSE`<br>`        RETURN false`<br>`    END IF`<br>`    RETURN true`<br>`END FUNCTION` | This algorithm routes file conversions to the correct handler based on file extension and target format. It uses if-else logic to match conversion types. | No |
| Database Initialization | `FUNCTION initializeDatabase()`<br>`    connection = CONNECT to "conversion_history.db"`<br>`    sql = "CREATE TABLE IF NOT EXISTS conversion_history (id, source_path, target_path, source_format, target_format, success, timestamp)"`<br>`    EXECUTE sql statement`<br>`    IF error THEN PRINT "Error"`<br>`    ELSE PRINT "Success"`<br>`    END IF`<br>`    CLOSE connection`<br>`END FUNCTION` | Creates the database table structure if it doesn't exist using SQLite's IF NOT EXISTS clause. Tracks conversion history with timestamps. | No |
| Settings Singleton | `CLASS SettingsManager`<br>`    STATIC instance = NULL`<br>`    STATIC FUNCTION getInstance()`<br>`        IF instance = NULL THEN`<br>`            instance = NEW SettingsManager()`<br>`        END IF`<br>`        RETURN instance`<br>`    END FUNCTION`<br>`END CLASS` | Implements Singleton pattern to ensure only one SettingsManager instance exists. Provides global access to application settings. | No |
| CSV to XLSX Converter | `FUNCTION convertCsvToXlsx(sourceFile, targetFile)`<br>`    OPEN sourceFile, CREATE workbook, CREATE sheet`<br>`    rowNumber = 0`<br>`    WHILE lines exist DO`<br>`        line = READ line, data = SPLIT by comma`<br>`        row = CREATE row at rowNumber`<br>`        FOR each data item DO`<br>`            CREATE cell, SET value`<br>`        END FOR`<br>`        INCREMENT rowNumber`<br>`    END WHILE`<br>`    WRITE workbook, CLOSE resources`<br>`END FUNCTION` | Reads CSV file line by line, splits by commas, and writes data into Excel cells. Creates basic spreadsheet with one sheet. Note: Uses simple comma split; production code would need proper CSV parser for quoted fields. | No |

## 1.1.3 Advanced Techniques

### Technique 1: Multi-threading (JavaFX Task)

**Pseudocode:**
```
FUNCTION handleConvertFile()
    CREATE new Task conversionTask
    
    conversionTask.call():
        sourceFile = GET source file
        targetFile = DETERMINE target file path
        CALL conversionHandler.convertFile(sourceFile, targetFile, format)
    
    conversionTask.succeeded():
        UPDATE UI with success message
        LOG conversion to database
    
    conversionTask.failed():
        SHOW error message to user
    
    CREATE new Thread with conversionTask
    START thread
END FUNCTION
```

**Explanation:** Uses JavaFX Task to run file conversions in background threads, preventing the UI from freezing. The Task provides callbacks for success and failure that safely update the UI.

### Technique 2: Database Operations with DAO Pattern

**Pseudocode:**
```
CLASS ConversionHistoryDAO
    FUNCTION insertRecord(record)
        sql = "INSERT INTO conversion_history(...) VALUES(?,?,?,?,?,?)"
        connection = CONNECT to database
        statement = PREPARE sql with connection
        SET parameters from record
        EXECUTE statement
        CLOSE resources
    END FUNCTION
    
    FUNCTION getAllRecords()
        records = EMPTY list
        sql = "SELECT * FROM conversion_history ORDER BY timestamp"
        connection = CONNECT to database
        resultSet = EXECUTE query
        
        WHILE resultSet has next row DO
            record = CREATE ConversionRecord from row data
            ADD record to records
        END WHILE
        
        RETURN records
    END FUNCTION
END CLASS
```

**Explanation:** Implements the Data Access Object pattern to separate database logic from business logic. All database operations are centralized in this class.

### Technique 3: Encryption and Decryption of Data

**Pseudocode:**
```
FUNCTION zipFolder(sourceFolder, targetZipFile, password)
    zipFile = CREATE new ZipFile(targetZipFile)
    parameters = NEW ZipParameters
    
    IF password is not empty THEN
        SET parameters.encryptFiles to TRUE
        SET parameters.encryptionMethod to AES
        SET zipFile.password to password
    END IF
    
    ADD sourceFolder to zipFile with parameters
END FUNCTION
```

**Explanation:** Implements AES encryption for ZIP files when a password is provided. Uses the Zip4j library's encryption capabilities to secure archived files.

### Technique 4: Singleton Design Pattern

**Pseudocode:**
```
CLASS SettingsManager
    STATIC instance = NULL
    
    STATIC FUNCTION getInstance()
        IF instance is NULL THEN
            instance = NEW SettingsManager()
        END IF
        RETURN instance
    END FUNCTION
END CLASS
```

**Explanation:** The Singleton pattern ensures only one SettingsManager instance exists throughout the application lifecycle, providing centralized configuration management.

### Technique 5: Properties File Handling (Similar to JSON)

**Pseudocode:**
```
FUNCTION loadSettings()
    IF "settings.properties" file exists THEN
        OPEN file for reading
        LOAD properties from file
        CLOSE file
    END IF
END FUNCTION

FUNCTION saveSettings()
    OPEN "settings.properties" for writing
    WRITE properties to file with comment "FileX Application Settings"
    CLOSE file
END FUNCTION
```

**Explanation:** Uses Java Properties to read and write configuration data in a key-value format similar to JSON. Persists user preferences like default output location and file naming conventions.
