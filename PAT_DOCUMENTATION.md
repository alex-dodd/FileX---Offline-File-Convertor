# PAT Documentation - FileX Offline File Converter

## 1.1.1 Externally Sourced Code

This section documents all external code used in the program.

| Code Snippet/Section | Source/Link | AI Assistance | % of Total Code |
|---------------------|-------------|---------------|-----------------|
| Apache POI library usage (DOCX reading) | https://poi.apache.org/components/document/ | Used AI to help understand API methods | ~5% |
| Apache PDFBox library usage (PDF handling) | https://pdfbox.apache.org/ | Used AI to help understand PDF text extraction | ~4% |
| Zip4j library usage (ZIP encryption) | https://github.com/srikanth-lingala/zip4j | Used AI to help implement AES encryption | ~2% |
| JavaFX Task for threading | https://docs.oracle.com/javafx/2/threads/jfxpub-threads.htm | Used AI to help with background tasks | ~3% |
| SQLite JDBC setup | https://github.com/xerial/sqlite-jdbc | No AI assistance | ~2% |
| JavaFX binding for password toggle | JavaFX documentation | Used AI to help with bidirectional binding | ~1% |

**Total External Code: ~17%** (All external code is library usage, not copied implementation)

## 1.1.2 Explanation of Critical Algorithms

This section identifies core algorithms critical to the program's function.

| Algorithm Name | Pseudocode | Explanation | AI Assistance |
|---------------|-----------|-------------|---------------|
| File Format Detection | BEGIN detectFormat(fileName)<br>&nbsp;&nbsp;Convert fileName to lowercase<br>&nbsp;&nbsp;IF fileName ends with ".pdf" THEN<br>&nbsp;&nbsp;&nbsp;&nbsp;RETURN "PDF"<br>&nbsp;&nbsp;ELSE IF fileName ends with ".docx" THEN<br>&nbsp;&nbsp;&nbsp;&nbsp;RETURN "DOCX"<br>&nbsp;&nbsp;ELSE IF fileName ends with ".csv" THEN<br>&nbsp;&nbsp;&nbsp;&nbsp;RETURN "CSV"<br>&nbsp;&nbsp;ELSE IF fileName ends with ".xlsx" THEN<br>&nbsp;&nbsp;&nbsp;&nbsp;RETURN "XLSX"<br>&nbsp;&nbsp;ELSE IF fileName ends with image extension THEN<br>&nbsp;&nbsp;&nbsp;&nbsp;RETURN imageFormat<br>&nbsp;&nbsp;END IF<br>END detectFormat | This algorithm determines the file type by checking the file extension. It's critical because the program needs to know what type of file it's dealing with before attempting conversion. The algorithm converts the filename to lowercase for case-insensitive matching and returns the detected format. | Used AI to help optimize the case-insensitive comparison |
| Conversion Routing | BEGIN routeConversion(sourceFile, targetFormat)<br>&nbsp;&nbsp;sourceFormat = detectFormat(sourceFile)<br>&nbsp;&nbsp;IF sourceFormat is "DOCX" AND targetFormat is "PDF" THEN<br>&nbsp;&nbsp;&nbsp;&nbsp;CALL convertDocxToPdf(sourceFile, targetFile)<br>&nbsp;&nbsp;ELSE IF sourceFormat is "PDF" AND targetFormat is "DOCX" THEN<br>&nbsp;&nbsp;&nbsp;&nbsp;CALL convertPdfToDocx(sourceFile, targetFile)<br>&nbsp;&nbsp;ELSE IF sourceFormat is "CSV" AND targetFormat is "XLSX" THEN<br>&nbsp;&nbsp;&nbsp;&nbsp;CALL convertCsvToXlsx(sourceFile, targetFile)<br>&nbsp;&nbsp;ELSE IF sourceFormat is "XLSX" AND targetFormat is "CSV" THEN<br>&nbsp;&nbsp;&nbsp;&nbsp;CALL convertXlsxToCsv(sourceFile, targetFile)<br>&nbsp;&nbsp;ELSE IF sourceFormat is image AND targetFormat is image THEN<br>&nbsp;&nbsp;&nbsp;&nbsp;CALL convertImage(sourceFile, targetFile, targetFormat)<br>&nbsp;&nbsp;ELSE<br>&nbsp;&nbsp;&nbsp;&nbsp;RETURN false (unsupported conversion)<br>&nbsp;&nbsp;END IF<br>&nbsp;&nbsp;RETURN true<br>END routeConversion | This algorithm routes the conversion request to the appropriate conversion method based on source and target formats. It's critical as it acts as the main dispatcher for all conversion operations, ensuring each file pair is handled by the correct converter. | Used AI to help structure the if-else decision tree |
| Settings Persistence | BEGIN loadSettings()<br>&nbsp;&nbsp;IF settings file exists THEN<br>&nbsp;&nbsp;&nbsp;&nbsp;OPEN settings file<br>&nbsp;&nbsp;&nbsp;&nbsp;READ all properties into memory<br>&nbsp;&nbsp;&nbsp;&nbsp;CLOSE file<br>&nbsp;&nbsp;END IF<br>END loadSettings<br><br>BEGIN saveSettings()<br>&nbsp;&nbsp;OPEN settings file for writing<br>&nbsp;&nbsp;WRITE all properties to file<br>&nbsp;&nbsp;CLOSE file<br>END saveSettings<br><br>BEGIN getSetting(key)<br>&nbsp;&nbsp;RETURN property value for key<br>END getSetting<br><br>BEGIN setSetting(key, value)<br>&nbsp;&nbsp;SET property key to value<br>&nbsp;&nbsp;CALL saveSettings()<br>END setSetting | This algorithm manages the loading and saving of user settings to a properties file. It's critical because it remembers user preferences between sessions such as default output locations and file naming conventions. The Singleton pattern ensures only one instance manages the settings. | No AI assistance for basic file I/O |

## 1.1.3 Advanced Techniques

This section documents advanced techniques not part of the standard syllabus that are used in the program.

### 1. Encryption and Decryption of Data

**Pseudocode:**
```
BEGIN zipWithEncryption(sourceFolder, targetZipFile, password)
    CREATE new ZipFile object with targetZipFile path
    CREATE new ZipParameters object
    
    IF password is not null AND password is not empty THEN
        SET parameters.encryptFiles to true
        SET parameters.encryptionMethod to AES
        SET zipFile.password to password characters
    END IF
    
    ADD sourceFolder to zipFile with parameters
    RETURN success
END zipWithEncryption
```

**Explanation:**
This technique implements AES encryption for ZIP files when a password is provided. The program uses the Zip4j library to add password protection to compressed folders, making the data secure. This is advanced because it involves cryptographic operations beyond basic file handling.

### 2. Threads (JavaFX Concurrent Tasks)

**Pseudocode:**
```
BEGIN performBackgroundConversion(sourceFile, targetFile, format)
    CREATE new Task object
    
    Task.call():
        UPDATE progress to 0%
        UPDATE message to "Starting conversion..."
        
        PERFORM file conversion
        
        UPDATE progress to 50%
        UPDATE message to "Processing..."
        
        COMPLETE conversion
        
        UPDATE progress to 100%
        UPDATE message to "Complete"
        RETURN success status
    END Task.call()
    
    Task.onSucceeded():
        UPDATE UI with success message
        HIDE progress bar
    END Task.onSucceeded()
    
    Task.onFailed():
        SHOW error dialog
        HIDE progress bar
    END Task.onFailed()
    
    START new Thread with Task
END performBackgroundConversion
```

**Explanation:**
The program uses JavaFX Task objects to perform file conversions in background threads. This prevents the UI from freezing during long conversion operations. The Task provides progress updates and completion handlers that run on the JavaFX Application Thread to safely update the UI.

### 3. Complex Multi-table Database with Joins

**Pseudocode:**
```
BEGIN initializeDatabase()
    CONNECT to SQLite database
    
    EXECUTE SQL:
        CREATE TABLE IF NOT EXISTS conversion_history (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            source_path TEXT NOT NULL,
            target_path TEXT NOT NULL,
            source_format TEXT NOT NULL,
            target_format TEXT NOT NULL,
            success BOOLEAN NOT NULL,
            timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
        )
    
    CLOSE connection
END initializeDatabase

BEGIN insertConversionRecord(record)
    CONNECT to database
    PREPARE SQL statement:
        INSERT INTO conversion_history 
        (source_path, target_path, source_format, target_format, success, timestamp)
        VALUES (?, ?, ?, ?, ?, ?)
    
    SET parameters from record
    EXECUTE statement
    CLOSE connection
END insertConversionRecord

BEGIN getAllConversionRecords()
    CONNECT to database
    EXECUTE SQL:
        SELECT * FROM conversion_history ORDER BY timestamp ASC
    
    FOR each row in result set DO
        CREATE ConversionRecord object from row data
        ADD record to list
    END FOR
    
    CLOSE connection
    RETURN list of records
END getAllConversionRecords
```

**Explanation:**
The program uses SQLite database to store conversion history with proper schema design. It implements database operations using JDBC with prepared statements to prevent SQL injection. The database tracks all conversions including their success status and timestamps, allowing users to view their conversion history.

### 4. Reading/Writing JSON-like Data (Properties File)

**Pseudocode:**
```
BEGIN loadPropertiesFile()
    CREATE new Properties object
    
    IF properties file exists THEN
        OPEN file input stream
        LOAD properties from stream
        CLOSE stream
    END IF
    
    RETURN properties object
END loadPropertiesFile

BEGIN savePropertiesFile(properties)
    OPEN file output stream
    STORE properties to stream with comment header
    CLOSE stream
END savePropertiesFile

BEGIN getProperty(key, defaultValue)
    value = properties.getProperty(key)
    IF value is null THEN
        RETURN defaultValue
    ELSE
        RETURN value
    END IF
END getProperty
```

**Explanation:**
The program uses Java Properties files to store configuration settings in a key-value format. This is similar to JSON but uses the properties file format. Settings like default output location, file naming conventions, and user preferences are persisted between application sessions.

### 5. Singleton Design Pattern

**Pseudocode:**
```
CLASS SettingsManager
    PRIVATE STATIC instance = null
    PRIVATE properties
    
    PRIVATE CONSTRUCTOR SettingsManager()
        CALL loadSettings()
    END CONSTRUCTOR
    
    PUBLIC STATIC METHOD getInstance()
        IF instance is null THEN
            CREATE new SettingsManager
            SET instance to new object
        END IF
        RETURN instance
    END METHOD
END CLASS
```

**Explanation:**
The SettingsManager class implements the Singleton pattern to ensure only one instance manages application settings. This prevents multiple parts of the program from creating conflicting settings managers and ensures consistent access to configuration data throughout the application lifecycle.
