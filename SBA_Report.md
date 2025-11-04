# SBA Report - FileX Offline File Converter

**Student Name:** Alex Dodd  
**Project:** FileX - Offline File Converter  
**Date:** November 2025  
**Grade:** 11 IT PAT

---

## 1. Externally Sourced Code

This section documents all external libraries, frameworks, and code resources used in the FileX project, with confirmation of compliance with the 20% external code limit.

### 1.1 External Libraries Used

The following external libraries and frameworks have been incorporated into this project:

1. **JavaFX (version 17.0.2)**
   - Purpose: Graphical user interface framework
   - Components used: javafx-controls, javafx-fxml, javafx-web
   - Used for: Creating the entire user interface including windows, buttons, text fields, progress bars, and tab navigation

2. **SQLite JDBC Driver (version 3.42.0.0)**
   - Purpose: Database connectivity
   - Used for: Storing and retrieving conversion history records in a local SQLite database

3. **Apache POI (version 5.2.4)**
   - Purpose: Microsoft Office document processing
   - Components used: poi, poi-ooxml, poi-scratchpad
   - Used for: Reading and writing DOCX (Word) and XLSX (Excel) file formats

4. **iText7 (version 7.2.5)**
   - Purpose: PDF operations
   - Used for: Advanced PDF creation capabilities

5. **Apache PDFBox (version 2.0.29)**
   - Purpose: PDF handling
   - Used for: Creating PDF documents from text and extracting text from PDF files

6. **OpenCSV (version 5.8)**
   - Purpose: CSV file processing
   - Used for: Robust CSV reading and writing capabilities

7. **ImgScalr (version 4.2)**
   - Purpose: Image processing
   - Used for: Image format conversion and manipulation

8. **Zip4j (version 2.11.5)**
   - Purpose: ZIP archive handling
   - Used for: Creating ZIP archives with optional AES encryption and password protection

9. **Apache Log4j (version 2.20.0)**
   - Purpose: Logging framework
   - Components used: log4j-core, log4j-api
   - Used for: Application logging and error tracking

10. **JUnit Jupiter (version 5.10.0)**
    - Purpose: Unit testing framework
    - Used for: Testing application functionality

### 1.2 External Code Percentage Calculation

**Total Project Lines of Code (Self-Written):** Approximately 2,727 lines of Java code

**External Library Code:** All external libraries are included via Maven dependencies and are not counted as part of the project's source code. The libraries provide pre-compiled functionality accessed through their APIs.

**Self-Written Code Components:**
- Main application class (App.java)
- File conversion handler (FileConversionHandler.java)
- ZIP handler (ZipHandler.java)
- Database management (DatabaseManager.java, ConversionHistoryDAO.java)
- Settings management (SettingsManager.java)
- UI controllers (MainUIController.java, HistoryUIController.java, SettingsUIController.java, DocumentationUIController.java, DetailsUIController.java)
- Data models (ConversionRecord.java)
- FXML UI layouts (5 files)

**Confirmation:** The use of external libraries through Maven dependencies does not constitute copying external code into the project. All application logic, algorithms, and business processes have been written from scratch. The external libraries are used as tools (similar to using the Java Standard Library), and their inclusion does not exceed the 20% threshold. The project meets IEB requirements for originality.

### 1.3 Code Attribution

All external libraries are properly attributed in the pom.xml Maven configuration file. Each library is used within its license terms (all are open-source with permissive licenses). No code has been copied from external sources; instead, libraries are accessed through their documented public APIs.

---

## 2. Explanation of Critical Algorithms

This section describes the core algorithms that drive the FileX application. All explanations are in plain English without implementation code.

### 2.1 File Format Detection Algorithm

**Purpose:** Determine the type of a selected file to enable appropriate conversion options.

**How it works:**
The file format detection algorithm examines the file's name to identify its extension. When a user selects a source file, the system extracts the filename and converts it to lowercase to ensure case-insensitive matching. The algorithm then checks if the filename ends with recognized extensions such as `.pdf`, `.docx`, `.xlsx`, `.csv`, `.jpg`, `.jpeg`, `.png`, or `.webp`.

Based on the detected format, the algorithm populates a list of compatible target formats. For example, if a PDF file is detected, only DOCX is offered as a conversion target. If a DOCX file is detected, only PDF is available. For spreadsheet files, CSV can convert to XLSX and vice versa. Image files can convert between JPG, PNG, and WEBP formats, with each format showing only compatible alternatives.

The algorithm also handles invalid or unsupported files by displaying all possible formats, allowing the user to attempt conversion while being aware that it may not succeed. This approach provides flexibility while guiding users toward valid conversion paths.

### 2.2 Conversion Pipeline Algorithm

**Purpose:** Orchestrate the file conversion process from source to target format.

**How it works:**
The conversion pipeline is the central algorithm that manages the entire conversion workflow. When a user clicks the "Convert" button, the pipeline begins by validating all required inputs: verifying that a source file has been selected, a target format has been chosen, and that the source file exists on the file system.

The pipeline then determines the output location. If the user has specified an output directory, that location is used. Otherwise, the system checks for a default output location in the user's settings. If no default exists, the converted file is saved in the same directory as the source file.

Next, the pipeline constructs the output filename by extracting the base name from the source file (everything before the file extension) and applying the user's naming convention preference. The naming convention can be set to keep the original name, add a "_converted" suffix, or include a timestamp. The target format's extension is then appended to create the final filename.

Before initiating conversion, the pipeline checks if a file with the target name already exists. If it does, and the user's settings require confirmation, a dialog is displayed asking whether to overwrite the existing file. If the user declines or if the check fails, the conversion is cancelled.

The pipeline then routes the conversion request to the appropriate specialized converter based on the source and target format combination. Document conversions (DOCX to PDF or PDF to DOCX) are handled by document-specific methods, spreadsheet conversions (CSV to XLSX or XLSX to CSV) by spreadsheet methods, and image conversions by image processing methods.

Throughout the process, the pipeline executes on a background thread to prevent the user interface from freezing. Progress is reported back to the UI thread using platform-specific mechanisms, allowing real-time updates of progress bars and status messages.

Finally, when conversion completes, the pipeline records the conversion event in the database history (if logging is enabled) and displays a success or failure message to the user.

### 2.3 Progress Calculation Algorithm

**Purpose:** Provide visual feedback during long-running file conversion operations.

**How it works:**
The progress calculation algorithm tracks conversion operations using JavaFX's Task framework. When a conversion begins, a Task object is created that wraps the conversion operation. This Task provides built-in progress tracking capabilities.

For simple single-file conversions, the progress is displayed as an indeterminate progress bar since the exact completion percentage cannot be easily calculated without detailed file analysis. The indeterminate mode shows an animated progress indicator, informing the user that work is in progress.

For ZIP archive creation, where multiple files may be processed, the progress could theoretically be calculated by tracking the number of files compressed versus the total number of files. However, the current implementation uses indeterminate progress for simplicity.

The algorithm updates status labels in real-time to complement the progress bar. Status messages include "Converting...", "Creating ZIP archive...", "Conversion completed successfully!", or error messages when failures occur. These textual updates provide context that pure progress percentages cannot convey.

The progress reporting mechanism uses JavaFX's Platform.runLater() method to ensure that UI updates occur on the JavaFX Application Thread, which is required for safe UI manipulation from background worker threads.

### 2.4 Error Handling Algorithm

**Purpose:** Gracefully manage errors and exceptions during file operations.

**How it works:**
The error handling algorithm employs a multi-layered approach to catch, process, and communicate errors to the user. At the lowest level, each file operation method (such as PDF reading, DOCX writing, or image conversion) is wrapped in a try-catch block that captures IOExceptions and other specific exceptions.

When an exception is caught, the algorithm logs the error message to the console for debugging purposes. The error is then propagated up to the calling method, which typically returns a boolean success indicator (true for success, false for failure) rather than throwing the exception further.

At the UI controller level, the algorithm monitors the success status returned by conversion operations. If a conversion fails, the controller displays a user-friendly error dialog that explains what went wrong. The error message is simplified from the technical exception message to make it understandable for non-technical users.

For file system errors, such as missing files, permission issues, or insufficient disk space, the algorithm provides specific error messages that guide the user toward resolution. For example, "Source file not found" or "Permission denied when writing to output location".

The algorithm also implements preventive error handling by validating inputs before attempting operations. File existence checks, format compatibility verification, and empty field detection all occur before the conversion process begins, preventing many errors from occurring in the first place.

For critical errors that could corrupt data or leave the application in an inconsistent state, the algorithm uses transaction-like patterns (particularly in database operations) where changes are only committed if the entire operation succeeds.

### 2.5 Database Persistence Algorithm

**Purpose:** Store and retrieve conversion history records reliably.

**How it works:**
The database persistence algorithm manages the storage of conversion records in a local SQLite database. When the application launches, the initialization algorithm checks if the database file exists. If not, it creates a new database file in the application's working directory.

Next, the algorithm executes a schema creation script that defines the conversion_history table structure. The CREATE TABLE IF NOT EXISTS statement ensures that the table is only created if it doesn't already exist, preventing errors on subsequent application launches.

When a conversion completes successfully, the recording algorithm constructs a ConversionRecord object containing the source file path, target file path, source format, target format, success status, and a timestamp. This record is passed to the Data Access Object (DAO).

The DAO algorithm uses a prepared statement to insert the record into the database. Prepared statements prevent SQL injection attacks and handle data type conversions automatically. The algorithm sets each parameter in the SQL statement (source path, target path, formats, success flag, and timestamp) and executes the insertion.

For retrieval operations, the query algorithm executes a SELECT statement that fetches all records from the database, ordered by timestamp in ascending order (oldest first). As each row is retrieved from the result set, the algorithm constructs a ConversionRecord object and adds it to a list.

The database connection algorithm uses try-with-resources statements to ensure that database connections, statements, and result sets are automatically closed after use, preventing resource leaks.

For deletion operations, the clear history algorithm executes a DELETE statement to remove all records and resets the auto-increment sequence to start ID numbering from 1 again on the next insert.

### 2.6 Settings Persistence Algorithm

**Purpose:** Save and load user preferences across application sessions.

**How it works:**
The settings persistence algorithm uses the Java Properties file format to store user preferences as key-value pairs. When the application starts, the loading algorithm checks if a settings.properties file exists in the application's working directory.

If the file exists, the algorithm reads it line by line, parsing each line into a key-value pair. The properties are stored in memory in a Properties object, which acts as a hashtable data structure providing fast lookup by key.

When the user modifies a setting through the Settings UI, the update algorithm immediately writes the change to both the in-memory Properties object and the persistent file. The algorithm uses a synchronized getInstance() method to ensure that only one SettingsManager instance exists throughout the application (Singleton pattern), preventing conflicts from concurrent updates.

The file writing algorithm uses a FileOutputStream to write the Properties object to disk. The Properties.store() method automatically formats the data in the standard properties file format, with each setting on its own line and optional comments.

For settings with default values, the algorithm checks if a key exists in the Properties object. If not, it returns a hardcoded default value rather than null. For example, "overwriteExistingFiles" defaults to false, and "showConfirmationDialogs" defaults to true.

The file naming convention algorithm applies user preferences when generating output filenames. It takes a base filename and extension as input, retrieves the user's naming convention preference, and applies the appropriate transformation. For "Add timestamp", it generates a formatted date-time string and appends it to the filename. For "Add _converted suffix", it inserts "_converted" before the extension. For "Keep original name", it returns the filename unchanged.

---

## 3. Advanced Techniques

This section describes advanced programming techniques used in the FileX project and provides IEB-style pseudocode for implementation.

### 3.1 Multithreading with Worker Threads

**Description:**
Multithreading is used extensively in FileX to prevent the user interface from freezing during long-running file operations. When a user initiates a file conversion or ZIP creation, the operation runs on a background worker thread while the main UI thread remains responsive to user interactions.

The JavaFX Task framework is employed to manage these worker threads. Each Task represents a unit of work that executes asynchronously. Tasks communicate their progress and results back to the UI thread using JavaFX's Platform.runLater() mechanism, which ensures thread-safe UI updates.

This technique allows users to continue interacting with the application (viewing menus, checking settings, or even canceling operations) while conversions are in progress. It exemplifies professional software design by prioritizing user experience.

**IEB-Style Pseudocode:**

```
CLASS ConversionTask INHERITS Task
    PRIVATE sourceFile
    PRIVATE targetFile  
    PRIVATE targetFormat
    PRIVATE conversionHandler
    
    METHOD call() RETURNS success
        BEGIN
            TRY
                // Perform conversion on background thread
                SET result = conversionHandler.convertFile(sourceFile, targetFile, targetFormat)
                RETURN result
            CATCH exception
                // Handle errors
                LOG error message
                RETURN FALSE
            END TRY
        END
    
    METHOD succeeded()
        BEGIN
            // Execute on UI thread after completion
            PLATFORM.runOnUIThread(
                HIDE progressBar
                SET statusLabel.text = "Conversion completed successfully!"
                IF settings.logConversions THEN
                    SAVE conversion record to database
                END IF
            )
        END
    
    METHOD failed()
        BEGIN
            // Execute on UI thread if task fails
            PLATFORM.runOnUIThread(
                HIDE progressBar
                SET statusLabel.text = "Conversion failed"
                SHOW error dialog with exception message
            )
        END
END CLASS

METHOD handleConvertButton()
    BEGIN
        // Validate inputs
        IF sourceFile is empty OR targetFormat is empty THEN
            SHOW error "Please select source file and target format"
            RETURN
        END IF
        
        // Create and configure task
        CREATE task as new ConversionTask(sourceFile, targetFile, targetFormat)
        
        // Bind progress bar to task progress
        BIND progressBar.progress TO task.progress
        
        // Show progress indicators
        SHOW progressBar
        SET statusLabel.text = "Converting..."
        
        // Execute task on background thread
        CREATE thread = new Thread(task)
        START thread
    END
```

**Reference to IEB Guidelines:** This technique demonstrates understanding of concurrent programming, which is an advanced concept in the IEB IT curriculum. It shows proper separation of concerns between UI and business logic.

### 3.2 Database Operations with JDBC

**Description:**
FileX uses JDBC (Java Database Connectivity) to interact with a local SQLite database for storing conversion history. The implementation follows the Data Access Object (DAO) pattern, which separates database logic from business logic.

The database operations include creating tables, inserting records, querying all records, and deleting records. Prepared statements are used to prevent SQL injection vulnerabilities and handle data type conversions. The connection management uses try-with-resources statements to ensure proper resource cleanup.

This technique demonstrates understanding of data persistence, SQL, and secure database programming practices.

**IEB-Style Pseudocode:**

```
CLASS DatabaseManager
    STATIC CONSTANT DB_URL = "jdbc:sqlite:conversion_history.db"
    
    METHOD connect() RETURNS connection
        BEGIN
            SET connection = DriverManager.getConnection(DB_URL)
            RETURN connection
        END
    
    METHOD initialize()
        BEGIN
            TRY WITH connection = connect(), statement = connection.createStatement()
                SET sql = "CREATE TABLE IF NOT EXISTS conversion_history (
                           id INTEGER PRIMARY KEY AUTOINCREMENT,
                           source_path TEXT NOT NULL,
                           target_path TEXT NOT NULL,
                           source_format TEXT NOT NULL,
                           target_format TEXT NOT NULL,
                           success BOOLEAN NOT NULL,
                           timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)"
                
                EXECUTE statement.execute(sql)
                OUTPUT "Database initialized successfully"
            CATCH SQLException
                OUTPUT "Error initializing database"
            END TRY
        END
END CLASS

CLASS ConversionHistoryDAO
    METHOD insertRecord(record)
        BEGIN
            SET sql = "INSERT INTO conversion_history(source_path, target_path, 
                       source_format, target_format, success, timestamp) 
                       VALUES(?,?,?,?,?,?)"
            
            TRY WITH connection = DatabaseManager.connect(),
                     preparedStatement = connection.prepareStatement(sql)
                
                SET preparedStatement.parameter[1] = record.sourcePath
                SET preparedStatement.parameter[2] = record.targetPath
                SET preparedStatement.parameter[3] = record.sourceFormat
                SET preparedStatement.parameter[4] = record.targetFormat
                SET preparedStatement.parameter[5] = record.success
                SET preparedStatement.parameter[6] = record.timestamp
                
                EXECUTE preparedStatement.executeUpdate()
            CATCH SQLException
                OUTPUT "Error inserting record"
            END TRY
        END
    
    METHOD getAllRecords() RETURNS recordList
        BEGIN
            CREATE recordList as empty list
            SET sql = "SELECT * FROM conversion_history ORDER BY timestamp ASC"
            
            TRY WITH connection = DatabaseManager.connect(),
                     statement = connection.createStatement(),
                     resultSet = statement.executeQuery(sql)
                
                WHILE resultSet has more rows DO
                    CREATE record with data from resultSet
                    ADD record to recordList
                END WHILE
            CATCH SQLException
                OUTPUT "Error retrieving records"
            END TRY
            
            RETURN recordList
        END
    
    METHOD clearAllRecords()
        BEGIN
            SET deleteSql = "DELETE FROM conversion_history"
            SET resetSql = "DELETE FROM sqlite_sequence WHERE name='conversion_history'"
            
            TRY WITH connection = DatabaseManager.connect(),
                     statement = connection.createStatement()
                
                EXECUTE statement.executeUpdate(deleteSql)
                EXECUTE statement.executeUpdate(resetSql)
            CATCH SQLException
                OUTPUT "Error clearing records"
            END TRY
        END
END CLASS
```

**Reference to IEB Guidelines:** Database management using SQL is a core component of the IEB IT curriculum. This implementation demonstrates proper use of CREATE, INSERT, SELECT, and DELETE statements, as well as understanding of primary keys, auto-increment, and data types.

### 3.3 File Encryption and ZIP Archive Creation

**Description:**
FileX includes functionality to create password-protected ZIP archives using AES encryption. Users can select a folder, optionally enable encryption, provide a password, and create a secure ZIP archive.

The Zip4j library is used for the actual ZIP operations, but the implementation in FileX demonstrates understanding of encryption concepts and secure file handling. When encryption is enabled, the algorithm configures AES encryption parameters and applies them to the ZIP operation.

This technique shows understanding of information security, encryption algorithms, and file compression.

**IEB-Style Pseudocode:**

```
CLASS ZipHandler
    METHOD createZip(sourceFolder, targetZipFile, password) RETURNS success
        BEGIN
            TRY
                CREATE zipFile object for targetZipFile
                CREATE zipParameters
                
                // Configure encryption if password provided
                IF password is not null AND password is not empty THEN
                    SET zipParameters.encryptFiles = TRUE
                    SET zipParameters.encryptionMethod = AES
                    SET zipFile.password = password
                END IF
                
                // Add folder to ZIP with encryption settings
                CALL zipFile.addFolder(sourceFolder, zipParameters)
                
                RETURN TRUE
            CATCH IOException
                OUTPUT "ZIP creation failed"
                RETURN FALSE
            END TRY
        END
END CLASS

METHOD handleConvertToZip()
    BEGIN
        // Get user inputs
        GET sourceFolder from sourceFolderField
        GET outputLocation from zipOutputLocationField  
        GET encrypt from encryptCheckbox
        
        // Validate inputs
        IF sourceFolder is empty THEN
            SHOW error "Please select a source folder"
            RETURN
        END IF
        
        IF encrypt is TRUE AND password is empty THEN
            SHOW error "Please enter a password for encryption"
            RETURN
        END IF
        
        // Show progress indicators
        SHOW zipProgressBar
        SET zipStatusLabel = "Creating ZIP archive..."
        
        // Create task for background processing
        CREATE zipTask as Task
            METHOD call()
                BEGIN
                    CREATE sourceFile from sourceFolder
                    
                    // Determine output directory
                    IF outputLocation is not empty THEN
                        SET outputDir = outputLocation
                    ELSE
                        SET outputDir = sourceFile.parent OR defaultOutputLocation
                    END IF
                    
                    // Create ZIP file path
                    CREATE zipFile = outputDir + sourceFile.name + ".zip"
                    
                    // Check for existing file
                    IF zipFile exists AND user declines overwrite THEN
                        SET zipStatusLabel = "ZIP creation cancelled by user"
                        HIDE zipProgressBar
                        RETURN null
                    END IF
                    
                    // Create encrypted ZIP
                    CALL zipHandler.createZip(sourceFile, zipFile, password)
                    RETURN null
                END
            
            METHOD succeeded()
                BEGIN
                    HIDE zipProgressBar
                    SET zipStatusLabel = "ZIP archive created successfully!"
                    
                    IF settings.logConversions THEN
                        SAVE conversion record to database
                    END IF
                END
            
            METHOD failed()
                BEGIN
                    HIDE zipProgressBar
                    SET zipStatusLabel = "ZIP creation failed"
                    SHOW error dialog
                END
        END CREATE
        
        // Execute on background thread
        CREATE thread = new Thread(zipTask)
        START thread
    END
```

**Reference to IEB Guidelines:** Encryption and data security are important topics in IT. This implementation demonstrates understanding of password protection, AES encryption (a symmetric encryption algorithm), and the importance of securing sensitive data.

### 3.4 Singleton Design Pattern

**Description:**
The Singleton design pattern is implemented in the SettingsManager class to ensure that only one instance of the settings manager exists throughout the application. This prevents multiple parts of the application from creating conflicting instances and ensures that all components access the same settings data.

The implementation uses a private constructor to prevent external instantiation, a static instance variable, and a synchronized getInstance() method to provide thread-safe access to the single instance.

This technique demonstrates understanding of object-oriented design patterns, which are considered advanced topics in programming.

**IEB-Style Pseudocode:**

```
CLASS SettingsManager
    // Private static instance variable
    PRIVATE STATIC instance = null
    PRIVATE properties
    PRIVATE CONSTANT SETTINGS_FILE = "settings.properties"
    
    // Private constructor prevents external instantiation
    PRIVATE METHOD SettingsManager()
        BEGIN
            CREATE properties as new Properties
            CALL loadSettings()
        END
    
    // Public synchronized method to get the single instance
    PUBLIC SYNCHRONIZED METHOD getInstance() RETURNS SettingsManager
        BEGIN
            IF instance is null THEN
                SET instance = new SettingsManager()
            END IF
            RETURN instance
        END
    
    PRIVATE METHOD loadSettings()
        BEGIN
            CREATE file = new File(SETTINGS_FILE)
            
            IF file exists THEN
                TRY WITH fileInputStream = new FileInputStream(file)
                    CALL properties.load(fileInputStream)
                CATCH IOException
                    OUTPUT "Error loading settings"
                END TRY
            END IF
        END
    
    PUBLIC METHOD saveSettings()
        BEGIN
            TRY WITH fileOutputStream = new FileOutputStream(SETTINGS_FILE)
                CALL properties.store(fileOutputStream, "FileX Application Settings")
            CATCH IOException
                OUTPUT "Error saving settings"
            END TRY
        END
    
    PUBLIC METHOD getDefaultOutputLocation() RETURNS path
        BEGIN
            RETURN properties.getProperty("defaultOutputLocation")
        END
    
    PUBLIC METHOD setDefaultOutputLocation(path)
        BEGIN
            SET properties.property["defaultOutputLocation"] = path
            CALL saveSettings()
        END
    
    PUBLIC METHOD getOverwriteExistingFiles() RETURNS boolean
        BEGIN
            SET value = properties.getProperty("overwriteExistingFiles", "false")
            RETURN Boolean.parseBoolean(value)
        END
    
    PUBLIC METHOD setOverwriteExistingFiles(overwrite)
        BEGIN
            SET properties.property["overwriteExistingFiles"] = String.valueOf(overwrite)
            CALL saveSettings()
        END
END CLASS

// Usage in application
METHOD initializeSettings()
    BEGIN
        // Get the single instance (creates it if first call)
        SET settingsManager = SettingsManager.getInstance()
        
        // Access settings through the singleton
        SET defaultPath = settingsManager.getDefaultOutputLocation()
        SET overwriteMode = settingsManager.getOverwriteExistingFiles()
    END
```

**Reference to IEB Guidelines:** Design patterns represent advanced object-oriented programming concepts. The Singleton pattern demonstrates understanding of object instantiation control, encapsulation, and the importance of maintaining consistent state across an application.

### 3.5 Properties File Configuration Management

**Description:**
FileX uses a properties file (settings.properties) to persistently store user preferences. This technique allows settings to survive application restarts and provides a simple, human-readable configuration format.

The implementation uses Java's built-in Properties class, which provides methods for loading from and storing to files. Settings are accessed through getter methods that provide default values when a setting hasn't been configured yet. When settings are modified, they are immediately saved to disk.

This technique demonstrates understanding of file I/O, data persistence, and configuration management.

**IEB-Style Pseudocode:**

```
METHOD loadSettings()
    BEGIN
        CREATE file = new File("settings.properties")
        
        IF file exists THEN
            TRY WITH fileInputStream = new FileInputStream(file)
                // Properties.load() reads file in key=value format
                CALL properties.load(fileInputStream)
                OUTPUT "Settings loaded successfully"
            CATCH IOException as error
                OUTPUT "Error loading settings: " + error.message
            END TRY
        ELSE
            OUTPUT "No settings file found, using defaults"
        END IF
    END

METHOD saveSettings()
    BEGIN
        TRY WITH fileOutputStream = new FileOutputStream("settings.properties")
            // Properties.store() writes in key=value format with optional comments
            CALL properties.store(fileOutputStream, "FileX Application Settings")
            OUTPUT "Settings saved successfully"
        CATCH IOException as error
            OUTPUT "Error saving settings: " + error.message
        END TRY
    END

METHOD getSetting(key, defaultValue) RETURNS value
    BEGIN
        // Get property value or return default if not found
        SET value = properties.getProperty(key, defaultValue)
        RETURN value
    END

METHOD setSetting(key, value)
    BEGIN
        // Update property in memory
        SET properties.property[key] = value
        
        // Immediately persist to disk
        CALL saveSettings()
    END

// Example usage for specific settings
METHOD getDefaultOutputLocation() RETURNS path
    BEGIN
        RETURN getSetting("defaultOutputLocation", null)
    END

METHOD setDefaultOutputLocation(path)
    BEGIN
        CALL setSetting("defaultOutputLocation", path)
    END

METHOD getOverwriteExistingFiles() RETURNS boolean
    BEGIN
        SET value = getSetting("overwriteExistingFiles", "false")
        RETURN Boolean.parseBoolean(value)
    END

METHOD setOverwriteExistingFiles(overwrite)
    BEGIN
        SET value = String.valueOf(overwrite)
        CALL setSetting("overwriteExistingFiles", value)
    END

METHOD applyFileNamingConvention(baseFileName, extension) RETURNS fullFileName
    BEGIN
        SET convention = getSetting("fileNamingConvention", "Keep original name")
        
        IF convention equals "Add timestamp" THEN
            SET now = getCurrentDateTime()
            SET formatter = "dd_MM_yyyy-HH_mm_ss"
            SET timestamp = now.format(formatter)
            RETURN baseFileName + "_" + timestamp + extension
        ELSE IF convention equals "Add _converted suffix" THEN
            RETURN baseFileName + "_converted" + extension
        ELSE // Keep original name
            RETURN baseFileName + extension
        END IF
    END
```

**Reference to IEB Guidelines:** File handling and data persistence are fundamental IT concepts. This implementation shows understanding of text file I/O, key-value data structures, and the importance of maintaining user preferences across application sessions. The properties file format is also commonly used in professional software development.

### 3.6 Observer Pattern with Property Binding

**Description:**
JavaFX's property binding system, used extensively in FileX, implements the Observer design pattern. UI components can be bound to data model properties, so when the model changes, the UI automatically updates without explicit refresh calls.

In FileX, this is demonstrated in password field visibility toggling, progress bar binding to task progress, and status label updates. The showPasswordCheckbox controls the visibility of both the password field and password text field through property bindings, creating a responsive UI with minimal code.

**IEB-Style Pseudocode:**

```
METHOD setupPasswordToggle()
    BEGIN
        // Bind visibility and management of password text field to checkbox state
        BIND passwordTextField.managed TO showPasswordCheckbox.selected
        BIND passwordTextField.visible TO showPasswordCheckbox.selected
        
        // Bind password field to opposite of checkbox (when checkbox selected, hide password field)
        BIND passwordField.managed TO NOT showPasswordCheckbox.selected
        BIND passwordField.visible TO NOT showPasswordCheckbox.selected
        
        // Bind text values bidirectionally (changes in either field update the other)
        BIND passwordTextField.text BIDIRECTIONAL TO passwordField.text
    END

METHOD setupProgressBinding(task)
    BEGIN
        // Bind progress bar to task progress (automatically updates as task progresses)
        BIND progressBar.progress TO task.progress
        
        // Show progress bar when task starts
        SET progressBar.visible = TRUE
        
        // Task framework automatically updates bound properties
    END

METHOD setupSourceFileListener()
    BEGIN
        // Add listener to source file field text property
        ADD LISTENER TO sourceFileField.text
            WHEN text changes FROM oldText TO newText
                IF newText is not empty THEN
                    CREATE file from newText
                    CALL updateTargetFormatOptions(file)
                    SET statusLabel.visible = FALSE
                ELSE
                    RESET targetFormatBox to show all formats
                    CLEAR targetFormatBox selection
                END IF
            END WHEN
        END LISTENER
    END

METHOD setupEncryptionListener()
    BEGIN
        // Add listener to encryption checkbox
        ADD LISTENER TO encryptCheckbox.selected
            WHEN selection changes FROM wasSelected TO isNowSelected
                SET passwordBox.visible = isNowSelected
                SET passwordBox.managed = isNowSelected
                
                IF isNowSelected is FALSE THEN
                    CLEAR passwordField.text
                    CLEAR passwordTextField.text
                END IF
            END WHEN
        END LISTENER
    END
```

**Reference to IEB Guidelines:** The Observer pattern and property binding demonstrate advanced understanding of event-driven programming and reactive UI design. These concepts show how modern applications respond to user interactions and data changes efficiently.

---

## Conclusion

This SBA Report documents the FileX - Offline File Converter project for IEB assessment purposes. The report has confirmed that:

1. **External Code Compliance:** All external code is properly attributed and accessed through libraries. No external code has been copied into the project beyond standard library usage, confirming compliance with the 20% limit.

2. **Critical Algorithms:** All core algorithms (file format detection, conversion pipeline, progress calculation, error handling, database persistence, and settings management) have been implemented from scratch using sound programming logic.

3. **Advanced Techniques:** The project demonstrates mastery of advanced programming concepts including multithreading, database operations with JDBC, file encryption, design patterns (Singleton and Observer), and configuration management through properties files.

The FileX project represents a complete, functional application that solves a real-world problem (offline file conversion) using professional software development practices suitable for a Grade 11 IT PAT assessment.

**Project Statistics:**
- Total self-written Java code: ~2,727 lines
- Number of classes: 12
- External libraries: 10 (all accessed via Maven)
- Supported file formats: PDF, DOCX, XLSX, CSV, JPG, PNG, WEBP, ZIP
- Database tables: 1 (conversion_history)
- Advanced techniques implemented: 6

This report is ready for teacher assessment and meets IEB guidelines for practical assessment task documentation.
