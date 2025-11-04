# FileX - PAT Documentation Requirements

## 1.1.1 Externally Sourced Code

This section declares all external code, libraries, and AI-assisted development used in the FileX application.

### External Libraries and Dependencies

| Library/Dependency | Version | Purpose | Source | Percentage Used |
|-------------------|---------|---------|--------|-----------------|
| Apache POI | 5.2.4 | Reading and writing Microsoft Office documents (DOCX, XLSX) | https://poi.apache.org/ | ~15% |
| Apache PDFBox | 2.0.29 | PDF document creation and manipulation | https://pdfbox.apache.org/ | ~10% |
| zip4j | 2.11.5 | ZIP archive creation with AES encryption support | https://github.com/srikanth-lingala/zip4j | ~5% |
| JavaFX | 17.0.2 | User interface framework for GUI components | https://openjfx.io/ | ~25% |
| SQLite JDBC | 3.42.0.0 | Database connectivity and management | https://github.com/xerial/sqlite-jdbc | ~8% |
| OpenCSV | 5.8 | CSV file parsing and generation | http://opencsv.sourceforge.net/ | ~3% |
| imgscalr | 4.2 | Image processing and scaling operations | https://github.com/rkalla/imgscalr | ~2% |

**Total External Code Usage**: Approximately 68% (libraries and frameworks)
**Original Code**: Approximately 32% (application logic, UI controllers, database layer)

### AI-Assisted Code Development

The following sections of code were developed with assistance from AI tools (GitHub Copilot):

| File/Class | Method/Section | AI Assistance Type | Description |
|------------|----------------|-------------------|-------------|
| FileConversionHandler.java | convertDocxToPdf() | Code suggestion | AI helped structure the PDFBox content stream operations and text positioning logic |
| FileConversionHandler.java | convertXlsxToCsv() | Code completion | AI suggested the StringBuilder approach for row data assembly and the switch statement for cell type handling |
| MainUIController.java | confirmOverwrite() | Algorithm suggestion | AI recommended using CountDownLatch and AtomicBoolean for thread-safe user confirmation dialogs |
| SettingsManager.java | applyFileNamingConvention() | Code pattern | AI suggested the switch-case structure and DateTimeFormatter pattern for timestamp generation |
| ZipHandler.java | zipFolder() | Parameter configuration | AI helped identify the correct encryption method configuration for AES encryption in zip4j |
| DatabaseManager.java | initialize() | SQL syntax | AI assisted with the CREATE TABLE IF NOT EXISTS SQL statement structure |
| MainUIController.java | setupPasswordToggle() | Binding logic | AI suggested the bidirectional binding approach for password field visibility toggle |

**Declaration**: While AI tools provided suggestions and code completion assistance, all code was reviewed, understood, and customized by the developer to fit the specific requirements of this application. AI-assisted sections constitute approximately 15% of the custom code written.

---

## 1.1.2 Explanation of Critical Algorithms

### Algorithm 1: File Format Detection and Conversion Routing

**Purpose**: This algorithm determines the appropriate conversion method based on source file extension and target format selection.

**Location**: `FileConversionHandler.java`, method `convertFile()`

**Explanation**:

The conversion routing algorithm is the central decision-making component of the FileX application. It serves as the primary dispatcher that analyzes file characteristics and routes conversion requests to specialized handlers.

**How it works**:

1. **Input Analysis Phase**: The algorithm receives three parameters - a source file object, a target file object, and a target format string. It begins by extracting the source filename and converting both the filename and target format to lowercase to ensure case-insensitive comparison.

2. **File Type Identification**: The algorithm uses string pattern matching on the file extension to categorize the source file into one of three main categories: document files (PDF, DOCX), spreadsheet files (CSV, XLSX), or image files (JPG, PNG, WEBP).

3. **Conversion Path Determination**: Using a series of conditional statements, the algorithm creates a decision tree that maps valid source-target format combinations. For each combination, it identifies the specific conversion method required. For example, if the source ends with ".docx" and the target format is "pdf", it routes to the DOCX-to-PDF converter.

4. **Handler Invocation**: Once the appropriate conversion path is identified, the algorithm delegates the actual conversion work to the specialized handler method. Each handler is responsible for the low-level operations of reading the source format and writing to the target format.

5. **Error Management**: The algorithm employs a try-catch structure to gracefully handle I/O exceptions that may occur during conversion. If any step fails, the algorithm catches the exception, logs an error message, and returns false to indicate failure. If the source-target combination is not supported, the algorithm immediately returns false without attempting conversion.

6. **Success Indication**: Upon successful completion of all conversion steps, the algorithm returns true to signal that the file has been successfully converted.

**Why this approach**: This centralized routing algorithm provides a single point of control for all conversion operations, making the system easier to maintain and extend. The decision tree structure allows for clear separation of concerns where each conversion type has its own specialized handler, promoting code reusability and reducing complexity.

---

### Algorithm 2: Database Record Persistence

**Purpose**: This algorithm manages the storage and retrieval of conversion history records in the SQLite database.

**Location**: `ConversionHistoryDAO.java`, methods `insertRecord()` and `getAllRecords()`

**Explanation**:

The database persistence algorithm is responsible for maintaining a complete history of all file conversions performed by the application, providing users with the ability to track their conversion activities.

**How it works**:

1. **Connection Establishment**: The algorithm begins by establishing a connection to the SQLite database using the JDBC driver. The connection is managed through a try-with-resources block to ensure proper resource cleanup.

2. **Record Insertion Process**: When saving a new conversion record, the algorithm uses a prepared statement to prevent SQL injection attacks. It constructs an INSERT statement with placeholders for six fields: source path, target path, source format, target format, success status, and timestamp. Each field from the ConversionRecord object is bound to its corresponding placeholder using type-specific setter methods.

3. **Data Retrieval Process**: When fetching all records, the algorithm executes a SELECT query that retrieves all columns from the conversion_history table, ordered chronologically by timestamp. It iterates through the result set, constructing ConversionRecord objects from each row by extracting data using column names and appropriate getter methods.

4. **Object Reconstruction**: For each database row, the algorithm creates a new ConversionRecord object, carefully converting database types to Java types. For example, SQL timestamps are converted to LocalDateTime objects, and SQL booleans are converted to Java boolean primitives.

5. **Collection Building**: All retrieved records are accumulated into an ArrayList, which is returned to the caller. This provides a convenient in-memory collection that can be easily displayed in the user interface or processed further.

6. **Error Handling**: Throughout the process, the algorithm catches SQLException instances that may occur due to database access errors, connection failures, or constraint violations. Errors are logged to the standard error stream with descriptive messages.

**Why this approach**: Using prepared statements with parameterized queries provides security against SQL injection while maintaining good performance. The try-with-resources pattern ensures that database connections and statements are always properly closed, preventing resource leaks. Storing conversion history allows users to track their work and provides an audit trail for troubleshooting.

---

### Algorithm 3: Settings Management with Persistence

**Purpose**: This algorithm handles application settings persistence using a properties file, ensuring user preferences are maintained across application sessions.

**Location**: `SettingsManager.java`, methods `loadSettings()` and `saveSettings()`

**Explanation**:

The settings management algorithm implements a persistent configuration system that allows users to customize application behavior according to their preferences.

**How it works**:

1. **Singleton Pattern Implementation**: The algorithm uses the Singleton design pattern to ensure only one instance of SettingsManager exists throughout the application lifecycle. This prevents conflicts and ensures consistent settings access. The getInstance() method checks if an instance exists and creates one if necessary, using synchronized access for thread safety.

2. **Initialization Phase**: When the SettingsManager is first created, it automatically invokes the loadSettings() method. This method checks for the existence of a settings.properties file in the application directory. If the file exists, it reads the file contents into a Properties object using a FileInputStream.

3. **Property Loading**: The Java Properties class handles the parsing of key-value pairs from the file. Each setting is stored as a string key with a corresponding string value. The algorithm reads all properties at once, making them available in memory for quick access throughout the application session.

4. **Setting Access**: When application components need to read a setting, they call getter methods like getDefaultOutputLocation() or getOverwriteExistingFiles(). These methods retrieve the string value from the Properties object and perform type conversion when necessary (e.g., parsing "true"/"false" strings to boolean values).

5. **Setting Modification**: When users change settings through the UI, setter methods are called. Each setter updates the in-memory Properties object and immediately calls saveSettings() to persist the change to disk. This ensures settings are not lost if the application crashes.

6. **Persistence Mechanism**: The saveSettings() method opens a FileOutputStream to the settings file and uses the Properties.store() method to write all current settings. The store() method formats the properties as key=value pairs with a timestamp comment, creating a human-readable configuration file.

7. **Default Values**: The algorithm provides sensible defaults for settings that haven't been explicitly set. For example, getOverwriteExistingFiles() returns false if the property is missing, and getShowConfirmationDialogs() returns true, implementing a safe-by-default philosophy.

**Why this approach**: The Properties file format is simple, human-readable, and well-supported by Java. The Singleton pattern prevents settings conflicts while providing global access. Immediate persistence on every change ensures settings are never lost. This approach balances simplicity with reliability, making it ideal for a desktop application.

---

### Algorithm 4: Asynchronous File Conversion with Progress Tracking

**Purpose**: This algorithm performs file conversions in background threads to prevent UI freezing while providing progress feedback to users.

**Location**: `MainUIController.java`, method `handleConvertFile()` and inner Task classes

**Explanation**:

The asynchronous conversion algorithm ensures the application remains responsive during potentially long-running file conversion operations.

**How it works**:

1. **Input Validation**: Before starting any background work, the algorithm validates all user inputs. It checks that a source file has been selected, a target format has been chosen, and all required fields are populated. If any validation fails, an error dialog is displayed and the operation is cancelled.

2. **UI Preparation**: The algorithm makes the progress bar visible and sets the status label to indicate that conversion is starting. This provides immediate feedback to the user that their request has been acknowledged.

3. **Task Creation**: A JavaFX Task object is created, which represents a background operation. The Task class provides built-in support for progress tracking, cancellation, and result handling. The conversion logic is placed inside the Task's call() method, which will execute on a background thread.

4. **Path Resolution**: Within the background task, the algorithm determines the output location. If the user specified an output location, it uses that. Otherwise, it checks for a default output location in settings. If neither is available, it defaults to the same directory as the source file. This cascading fallback logic ensures the conversion can always proceed.

5. **Filename Generation**: The algorithm applies the user's preferred file naming convention (original name, with timestamp, or with _converted suffix) to generate the final output filename. This allows users to customize how converted files are named without risking overwrites.

6. **Overwrite Confirmation**: If the target file already exists, the algorithm checks the user's overwrite preference setting. If automatic overwriting is disabled, it displays a confirmation dialog using a thread-safe pattern with CountDownLatch and AtomicBoolean to communicate the user's choice back to the background thread.

7. **Conversion Execution**: The actual file conversion is performed by calling the appropriate method on the FileConversionHandler. This operation happens entirely in the background thread, preventing any UI freezing.

8. **Completion Handling**: The Task provides succeeded() and failed() callback methods that run on the UI thread. On success, the algorithm hides the progress bar, displays a success message, and optionally logs the conversion to the history database. On failure, it displays an error dialog with the exception message.

9. **Thread Management**: The algorithm creates a new Thread object, passes the Task to it, and starts the thread. JavaFX handles the thread lifecycle and ensures callbacks run on the correct thread.

**Why this approach**: This algorithm follows JavaFX best practices for background operations. By using Task, the code gains automatic progress tracking, clean separation between UI and background work, and thread-safe callbacks. This prevents the common problem of frozen interfaces during long operations while maintaining a responsive user experience.

---

## 1.1.3 Advanced Techniques

### Technique 1: Multithreading with JavaFX Tasks

**Description**: The application implements multithreading to perform file conversion operations asynchronously without freezing the user interface. This ensures the application remains responsive even during lengthy conversion processes.

**Implementation Location**: `MainUIController.java`, methods `handleConvertFile()` and `handleConvertToZip()`

**Pseudocode (IEB Standard)**:

```
BEGIN handleConvertFile
    // Input validation phase
    sourceFile ← GET text from sourceFileField
    targetFormat ← GET selected item from targetFormatBox
    outputLocation ← GET text from outputLocationField
    
    IF sourceFile is empty THEN
        DISPLAY error message "Please select a source file"
        RETURN
    END IF
    
    IF targetFormat is empty THEN
        DISPLAY error message "Please select a target format"
        RETURN
    END IF
    
    // Prepare UI for conversion
    SET conversionProgressBar visibility to TRUE
    SET statusLabel text to "Converting..."
    
    // Create background task
    CREATE conversionTask as new Task
    BEGIN Task.call() method
        sourceFileObj ← CREATE File object from sourceFile path
        
        // Determine output path with fallback logic
        IF outputLocation is not empty THEN
            outputPath ← outputLocation
        ELSE
            defaultOutput ← GET default output location from settings
            IF defaultOutput is not empty THEN
                outputPath ← defaultOutput
            ELSE
                outputPath ← GET parent directory of sourceFileObj
            END IF
        END IF
        
        // Generate target filename
        fileName ← GET name from sourceFileObj
        baseName ← EXTRACT filename without extension from fileName
        targetExtension ← CONCATENATE "." with lowercase targetFormat
        finalFileName ← APPLY naming convention to baseName and targetExtension
        targetFileObj ← CREATE File object from outputPath and finalFileName
        
        // Check for existing file
        IF targetFileObj exists AND user confirms overwrite is FALSE THEN
            UPDATE UI with "Conversion cancelled by user"
            RETURN NULL
        END IF
        
        // Perform the conversion
        CALL conversionHandler.convertFile(sourceFileObj, targetFileObj, targetFormat)
        RETURN NULL
    END Task.call()
    
    // Define success handler
    BEGIN Task.succeeded() method
        SET conversionProgressBar visibility to FALSE
        SET statusLabel text to "Conversion completed successfully!"
        
        IF settings specify to log successful conversions THEN
            CREATE conversionRecord with source and target details
            CALL historyDAO.addConversionRecord(conversionRecord)
        END IF
    END Task.succeeded()
    
    // Define failure handler
    BEGIN Task.failed() method
        SET conversionProgressBar visibility to FALSE
        SET statusLabel text to "Conversion failed"
        DISPLAY error dialog with exception message
    END Task.failed()
    
    // Start the background thread
    CREATE new Thread with conversionTask
    START the thread
END handleConvertFile
```

**Benefits**: This technique prevents UI freezing during long operations, improves user experience, allows multiple conversions to be queued, and provides clean separation between UI and business logic.

---

### Technique 2: Database Operations with SQLite

**Description**: The application uses SQLite database to persist conversion history records, allowing users to track all file conversions performed. This includes complex table creation, parameterized queries, and result set processing.

**Implementation Location**: `DatabaseManager.java` and `ConversionHistoryDAO.java`

**Pseudocode (IEB Standard)**:

```
BEGIN DatabaseManager.initialize
    TRY
        connection ← ESTABLISH database connection
        statement ← CREATE statement from connection
        
        sqlCommand ← "CREATE TABLE IF NOT EXISTS conversion_history (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            source_path TEXT NOT NULL,
            target_path TEXT NOT NULL,
            source_format TEXT NOT NULL,
            target_format TEXT NOT NULL,
            success BOOLEAN NOT NULL,
            timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
        )"
        
        EXECUTE sqlCommand using statement
        DISPLAY "Database initialised successfully"
        
        CLOSE statement
        CLOSE connection
    CATCH SQLException as error
        DISPLAY "Error initialising database: " + error message
    END TRY
END DatabaseManager.initialize

BEGIN ConversionHistoryDAO.insertRecord(record)
    sqlCommand ← "INSERT INTO conversion_history(source_path, target_path, 
                  source_format, target_format, success, timestamp) 
                  VALUES(?,?,?,?,?,?)"
    
    TRY
        connection ← ESTABLISH database connection
        preparedStatement ← PREPARE statement with sqlCommand
        
        // Bind parameters to prevent SQL injection
        SET parameter 1 to record.sourcePath
        SET parameter 2 to record.targetPath
        SET parameter 3 to record.sourceFormat
        SET parameter 4 to record.targetFormat
        SET parameter 5 to record.success
        SET parameter 6 to record.timestamp
        
        EXECUTE preparedStatement update
        
        CLOSE preparedStatement
        CLOSE connection
    CATCH SQLException as error
        DISPLAY "Error inserting record: " + error message
    END TRY
END ConversionHistoryDAO.insertRecord

BEGIN ConversionHistoryDAO.getAllRecords
    records ← CREATE empty list
    sqlCommand ← "SELECT * FROM conversion_history ORDER BY timestamp ASC"
    
    TRY
        connection ← ESTABLISH database connection
        statement ← CREATE statement from connection
        resultSet ← EXECUTE query sqlCommand
        
        WHILE resultSet has next row DO
            id ← GET integer from resultSet column "id"
            sourcePath ← GET string from resultSet column "source_path"
            targetPath ← GET string from resultSet column "target_path"
            sourceFormat ← GET string from resultSet column "source_format"
            targetFormat ← GET string from resultSet column "target_format"
            success ← GET boolean from resultSet column "success"
            timestamp ← GET timestamp from resultSet column "timestamp"
            
            record ← CREATE ConversionRecord with all extracted values
            ADD record to records list
        END WHILE
        
        CLOSE resultSet
        CLOSE statement
        CLOSE connection
    CATCH SQLException as error
        DISPLAY "Error retrieving records: " + error message
    END TRY
    
    RETURN records
END ConversionHistoryDAO.getAllRecords
```

**Benefits**: Provides persistent storage of conversion history, enables data analysis and reporting, supports concurrent access safely, and maintains data integrity through SQL constraints.

---

### Technique 3: Encryption and Decryption of Data (AES Encryption)

**Description**: The application implements AES (Advanced Encryption Standard) encryption for ZIP archives, allowing users to password-protect their compressed files. This uses the zip4j library's encryption capabilities.

**Implementation Location**: `ZipHandler.java`, method `zipFolder()`

**Pseudocode (IEB Standard)**:

```
BEGIN ZipHandler.zipFolder(sourceFolder, targetZipFile, password)
    TRY
        // Initialize ZIP file handler
        zipFile ← CREATE ZipFile object with targetZipFile path
        zipParameters ← CREATE new ZipParameters object
        
        // Configure encryption if password provided
        IF password is not NULL AND password is not empty THEN
            // Enable encryption
            SET zipParameters.encryptFiles to TRUE
            
            // Set encryption method to AES (Advanced Encryption Standard)
            SET zipParameters.encryptionMethod to AES
            
            // Convert password string to character array and set on zipFile
            passwordChars ← CONVERT password to character array
            CALL zipFile.setPassword(passwordChars)
        END IF
        
        // Add the source folder to the ZIP archive
        CALL zipFile.addFolder(sourceFolder, zipParameters)
        
        // Implicitly close and finalize the ZIP file
        
    CATCH IOException as error
        THROW IOException with error message
    END TRY
END ZipHandler.zipFolder

BEGIN ZipHandler.createZip(sourceFolder, targetZipFile, password)
    TRY
        // Delegate to the main zipping method
        CALL zipFolder(sourceFolder, targetZipFile, password)
        
        // Return success
        RETURN TRUE
        
    CATCH IOException as error
        // Log the error
        DISPLAY "ZIP creation failed: " + error message to error stream
        
        // Return failure
        RETURN FALSE
    END TRY
END ZipHandler.createZip
```

**Benefits**: Protects sensitive data with industry-standard AES encryption, allows users to secure their compressed files, provides optional encryption (user choice), and integrates seamlessly with the application's workflow.

---

### Technique 4: Singleton Design Pattern

**Description**: The application uses the Singleton pattern for the SettingsManager class to ensure only one instance manages application settings throughout the entire application lifecycle. This prevents settings conflicts and ensures consistency.

**Implementation Location**: `SettingsManager.java`

**Pseudocode (IEB Standard)**:

```
CLASS SettingsManager
    // Private static instance variable
    PRIVATE STATIC instance ← NULL
    
    // Private properties object to store settings
    PRIVATE properties ← CREATE new Properties object
    
    // Private constructor prevents external instantiation
    PRIVATE CONSTRUCTOR SettingsManager()
        CALL loadSettings()
    END CONSTRUCTOR
    
    // Public static method to get the single instance
    PUBLIC STATIC METHOD getInstance()
        // Thread-safe instance creation
        SYNCHRONIZED
            IF instance is NULL THEN
                instance ← CREATE new SettingsManager object
            END IF
        END SYNCHRONIZED
        
        RETURN instance
    END METHOD
    
    PRIVATE METHOD loadSettings()
        settingsFile ← CREATE File object for "settings.properties"
        
        IF settingsFile exists THEN
            TRY
                fileInputStream ← OPEN settingsFile for reading
                
                // Load all properties from file
                CALL properties.load(fileInputStream)
                
                CLOSE fileInputStream
            CATCH IOException as error
                DISPLAY "Error loading settings: " + error message
            END TRY
        END IF
    END METHOD
    
    PUBLIC METHOD saveSettings()
        TRY
            fileOutputStream ← OPEN "settings.properties" for writing
            
            // Store all properties to file with comment
            CALL properties.store(fileOutputStream, "FileX Application Settings")
            
            CLOSE fileOutputStream
        CATCH IOException as error
            DISPLAY "Error saving settings: " + error message
        END TRY
    END METHOD
    
    PUBLIC METHOD getDefaultOutputLocation()
        RETURN properties.getProperty("defaultOutputLocation")
    END METHOD
    
    PUBLIC METHOD setDefaultOutputLocation(path)
        CALL properties.setProperty("defaultOutputLocation", path)
        CALL saveSettings()
    END METHOD
    
    // Additional getter and setter methods for other settings...
    
END CLASS

// Usage example in other classes:
settingsManager ← CALL SettingsManager.getInstance()
defaultPath ← CALL settingsManager.getDefaultOutputLocation()
```

**Benefits**: Ensures single source of truth for application settings, prevents conflicts from multiple instances, provides global access point, implements lazy initialization, and includes thread-safe instance creation.

---

### Technique 5: Complex File I/O with Multiple Format Support

**Description**: The application performs complex file input/output operations across multiple formats including DOCX, PDF, XLSX, CSV, and various image formats. This involves reading binary files, parsing document structures, and writing to different formats.

**Implementation Location**: `FileConversionHandler.java`

**Pseudocode (IEB Standard)**:

```
BEGIN FileConversionHandler.convertDocxToPdf(sourceFile, targetFile)
    TRY
        // Open input DOCX file
        fileInputStream ← OPEN sourceFile for reading
        docxDocument ← CREATE XWPFDocument from fileInputStream
        
        // Create new PDF document
        pdfDocument ← CREATE new PDDocument
        
        // Create a page in the PDF
        page ← CREATE new PDPage
        CALL pdfDocument.addPage(page)
        
        // Open content stream for writing to page
        contentStream ← CREATE PDPageContentStream for pdfDocument and page
        
        // Set up text writing
        CALL contentStream.beginText()
        CALL contentStream.setFont(TIMES_ROMAN, size 12)
        CALL contentStream.setLeading(14.5)
        CALL contentStream.newLineAtOffset(25, 725)
        
        // Extract and write text from DOCX
        paragraphs ← GET all paragraphs from docxDocument
        FOR EACH paragraph IN paragraphs DO
            text ← GET text from paragraph
            CALL contentStream.showText(text)
            CALL contentStream.newLine()
        END FOR
        
        CALL contentStream.endText()
        
        // Close streams and save
        CLOSE contentStream
        CALL pdfDocument.save(targetFile)
        
        // Close documents
        CLOSE pdfDocument
        CLOSE docxDocument
        CLOSE fileInputStream
        
    CATCH IOException as error
        THROW IOException with error message
    END TRY
END FileConversionHandler.convertDocxToPdf

BEGIN FileConversionHandler.convertCsvToXlsx(sourceFile, targetFile)
    TRY
        // Open CSV file for reading
        bufferedReader ← OPEN sourceFile with BufferedReader
        
        // Create Excel workbook
        workbook ← CREATE new XSSFWorkbook
        sheet ← CREATE sheet named "Sheet1" in workbook
        
        // Process CSV line by line
        rowNum ← 0
        WHILE bufferedReader has more lines DO
            line ← READ next line from bufferedReader
            data ← SPLIT line by comma delimiter
            
            // Create row in Excel sheet
            row ← CREATE row at rowNum in sheet
            
            // Add each cell data
            FOR i FROM 0 TO length of data DO
                cell ← CREATE cell at position i in row
                CALL cell.setCellValue(data[i])
            END FOR
            
            INCREMENT rowNum
        END WHILE
        
        // Save Excel file
        fileOutputStream ← OPEN targetFile for writing
        CALL workbook.write(fileOutputStream)
        
        // Close all resources
        CLOSE fileOutputStream
        CLOSE workbook
        CLOSE bufferedReader
        
    CATCH IOException as error
        THROW IOException with error message
    END TRY
END FileConversionHandler.convertCsvToXlsx

BEGIN FileConversionHandler.convertImage(sourceFile, targetFile, targetFormat)
    TRY
        // Read source image into memory
        bufferedImage ← READ image from sourceFile
        
        IF bufferedImage is NULL THEN
            THROW IOException "Could not read image from file"
        END IF
        
        // Write image in target format
        success ← WRITE bufferedImage to targetFile as targetFormat
        
        IF success is FALSE THEN
            THROW IOException "Could not write image to target format"
        END IF
        
    CATCH IOException as error
        THROW IOException with error message
    END TRY
END FileConversionHandler.convertImage
```

**Benefits**: Supports multiple document formats, handles binary and text files appropriately, preserves data integrity during conversion, provides unified interface for different conversions, and uses industry-standard libraries for reliable format handling.

---

### Technique 6: Dynamic UI Component Binding

**Description**: The application uses JavaFX property binding to synchronize UI components dynamically, such as bidirectional text field binding for password visibility toggling and visibility binding for conditional component display.

**Implementation Location**: `MainUIController.java`, method `setupPasswordToggle()`

**Pseudocode (IEB Standard)**:

```
BEGIN MainUIController.setupPasswordToggle
    // Bind password text field visibility to checkbox state
    // When checkbox is selected, text field shows; when unselected, it hides
    BIND passwordTextField.managedProperty to showPasswordCheckbox.selectedProperty
    BIND passwordTextField.visibleProperty to showPasswordCheckbox.selectedProperty
    
    // Bind password field (masked) visibility to opposite of checkbox state
    // When checkbox is selected, password field hides; when unselected, it shows
    notSelectedProperty ← GET showPasswordCheckbox.selectedProperty.not()
    BIND passwordField.managedProperty to notSelectedProperty
    BIND passwordField.visibleProperty to notSelectedProperty
    
    // Bidirectional binding keeps both fields synchronized
    // Changes in either field automatically update the other
    BIND passwordTextField.textProperty BIDIRECTIONALLY to passwordField.textProperty
END MainUIController.setupPasswordToggle

BEGIN MainUIController.setupZipConverter
    // Initialize visibility states
    SET zipStatusLabel visibility to FALSE
    SET zipProgressBar visibility to FALSE
    SET passwordBox visibility to FALSE
    SET passwordBox managed state to FALSE
    
    // Add listener for encryption checkbox changes
    ADD LISTENER to encryptCheckbox.selectedProperty
        BEGIN listener(observable, oldValue, newValue)
            // Show password box when encryption is enabled
            SET passwordBox visibility to newValue
            SET passwordBox managed state to newValue
            
            // Clear password fields when encryption is disabled
            IF newValue is FALSE THEN
                CALL passwordField.clear()
                CALL passwordTextField.clear()
            END IF
        END listener
END MainUIController.setupZipConverter

BEGIN MainUIController.updateTargetFormatOptions(sourceFile)
    IF sourceFile does not exist THEN
        SET targetFormatBox items to all available formats
        RETURN
    END IF
    
    fileName ← GET name from sourceFile in lowercase
    supportedFormats ← CREATE empty observable list
    
    // Determine supported target formats based on source file type
    IF fileName ends with ".pdf" THEN
        ADD "DOCX" to supportedFormats
    ELSE IF fileName ends with ".docx" THEN
        ADD "PDF" to supportedFormats
    ELSE IF fileName ends with ".csv" THEN
        ADD "XLSX" to supportedFormats
    ELSE IF fileName ends with ".xlsx" THEN
        ADD "CSV" to supportedFormats
    ELSE IF fileName ends with ".webp" THEN
        ADD "JPG" and "PNG" to supportedFormats
    ELSE IF fileName ends with ".jpg" OR fileName ends with ".jpeg" THEN
        ADD "PNG" and "WEBP" to supportedFormats
    ELSE IF fileName ends with ".png" THEN
        ADD "JPG" and "WEBP" to supportedFormats
    ELSE
        ADD all available formats to supportedFormats
    END IF
    
    SET targetFormatBox items to supportedFormats
    CALL targetFormatBox.clearSelection()
END MainUIController.updateTargetFormatOptions
```

**Benefits**: Creates responsive UI that updates automatically, eliminates manual synchronization code, reduces bugs from state management, provides smooth user experience, and follows JavaFX best practices for property binding.

---

### Summary of Advanced Techniques Implementation

| Technique | Complexity Level | Lines of Code | Impact on Application |
|-----------|-----------------|---------------|----------------------|
| Multithreading with JavaFX Tasks | High | ~150 | Critical - Prevents UI freezing |
| Database Operations (SQLite) | Medium | ~120 | High - Enables history tracking |
| AES Encryption | Medium | ~30 | Medium - Security feature |
| Singleton Pattern | Low | ~25 | Medium - Ensures settings consistency |
| Complex File I/O | High | ~200 | Critical - Core functionality |
| Dynamic UI Binding | Medium | ~80 | High - Improved UX |

**Total Advanced Technique Implementation**: Approximately 605 lines of advanced technique code across 6 distinct patterns and methodologies.

---

## Declaration

I, the developer of FileX, declare that:

1. All external code sources have been properly documented in section 1.1.1
2. No more than 20% of the custom application code (excluding frameworks and libraries) comes from external sources or AI assistance
3. All critical algorithms explained in section 1.1.2 are essential to the correct functioning of the application
4. All advanced techniques in section 1.1.3 represent functionality beyond the standard curriculum
5. All pseudocode follows IEB standards and conventions
6. I understand and can explain all code in this application, whether written independently or with assistance

**Date**: November 4, 2025
**Project**: FileX - Offline File Converter
**Version**: 1.0.0
