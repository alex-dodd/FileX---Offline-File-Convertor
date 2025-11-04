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

### Algorithm 1: File Conversion Router

**Purpose:** Determines the appropriate conversion method based on file extensions and routes to the correct handler.

**Pseudocode:**
```
FUNCTION convertFile(sourceFile, targetFile, targetFormat)
    sourceName = LOWERCASE(sourceFile.name)
    targetFormat = LOWERCASE(targetFormat)
    
    IF sourceName ENDS WITH ".docx" AND targetFormat = "pdf" THEN
        CALL convertDocxToPdf(sourceFile, targetFile)
    ELSE IF sourceName ENDS WITH ".pdf" AND targetFormat = "docx" THEN
        CALL convertPdfToDocx(sourceFile, targetFile)
    ELSE IF sourceName ENDS WITH ".csv" AND targetFormat = "xlsx" THEN
        CALL convertCsvToXlsx(sourceFile, targetFile)
    ELSE IF sourceName ENDS WITH ".xlsx" AND targetFormat = "csv" THEN
        CALL convertXlsxToCsv(sourceFile, targetFile)
    ELSE IF sourceName is image AND targetFormat is image THEN
        CALL convertImage(sourceFile, targetFile, targetFormat)
    ELSE
        RETURN false
    END IF
    
    RETURN true
END FUNCTION
```

**Explanation:** This algorithm acts as the main controller for file conversions. It checks the source file extension and target format, then calls the appropriate specialized conversion method. This uses a simple if-else chain to match conversion types.

**AI Assistance:** No AI assistance used.

### Algorithm 2: Database Schema Initialization

**Purpose:** Creates the database table structure if it doesn't exist, ensuring data persistence.

**Pseudocode:**
```
FUNCTION initializeDatabase()
    connection = CONNECT to "conversion_history.db"
    
    sql = "CREATE TABLE IF NOT EXISTS conversion_history (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        source_path TEXT NOT NULL,
        target_path TEXT NOT NULL,
        source_format TEXT NOT NULL,
        target_format TEXT NOT NULL,
        success BOOLEAN NOT NULL,
        timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
    )"
    
    EXECUTE sql statement
    
    IF error THEN
        PRINT "Error initializing database"
    ELSE
        PRINT "Database initialized successfully"
    END IF
    
    CLOSE connection
END FUNCTION
```

**Explanation:** This algorithm ensures the database table exists before any operations. It uses SQLite's "IF NOT EXISTS" clause to avoid errors if the table is already present. The schema tracks conversion history with timestamps.

**AI Assistance:** No AI assistance used.

### Algorithm 3: Settings Manager Singleton Pattern

**Purpose:** Ensures only one instance of SettingsManager exists and provides global access to application settings.

**Pseudocode:**
```
CLASS SettingsManager
    STATIC instance = NULL
    properties = NEW Properties object
    
    PRIVATE CONSTRUCTOR SettingsManager()
        CALL loadSettings()
    END CONSTRUCTOR
    
    STATIC FUNCTION getInstance()
        IF instance = NULL THEN
            instance = NEW SettingsManager()
        END IF
        RETURN instance
    END FUNCTION
    
    FUNCTION loadSettings()
        IF "settings.properties" exists THEN
            READ properties from file
        END IF
    END FUNCTION
    
    FUNCTION saveSettings()
        WRITE properties to "settings.properties"
    END FUNCTION
END CLASS
```

**Explanation:** This implements the Singleton design pattern to manage application settings. It ensures settings are loaded once and shared across the application, preventing conflicts from multiple instances.

**AI Assistance:** No AI assistance used.

### Algorithm 4: CSV to XLSX Conversion

**Purpose:** Reads CSV data and converts it into an Excel spreadsheet format.

**Pseudocode:**
```
FUNCTION convertCsvToXlsx(sourceFile, targetFile)
    OPEN sourceFile for reading
    CREATE new Excel workbook
    CREATE sheet named "Sheet1"
    
    rowNumber = 0
    WHILE there are lines to read FROM sourceFile DO
        line = READ next line
        data = SPLIT line by comma
        
        row = CREATE new row at rowNumber
        FOR i = 0 TO LENGTH(data) - 1 DO
            cell = CREATE cell at position i
            SET cell value to data[i]
        END FOR
        
        INCREMENT rowNumber
    END WHILE
    
    WRITE workbook to targetFile
    CLOSE all resources
END FUNCTION
```

**Explanation:** This algorithm reads a CSV file line by line, splits each line by commas, and writes the data into Excel cells. It creates a basic spreadsheet structure with one sheet.

**AI Assistance:** No AI assistance used.

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
