# PAT Report: Algorithms and Advanced Techniques

**Project:** FileX - Offline File Converter  
**Student:** Alex Dodd  
**Purpose:** Grade 11 IT School PAT (Practical Assessment Task)

---

## 1. Externally Sourced Code

| Code Snippet/Section | Source/Link | AI Assistance | % of Total Code |
|---------------------|-------------|---------------|-----------------|
| Database Setup and SQL Schema | SQLite documentation (https://www.sqlite.org/docs.html) and online tutorials for JDBC PreparedStatements | Yes - ChatGPT helped me debug the SQL CREATE TABLE syntax and fix parameter binding issues in PreparedStatements | ~3% |
| Thread Synchronization Pattern (CountDownLatch) | Stack Overflow discussion on JavaFX threading (https://stackoverflow.com/questions/29449297/) and Oracle JavaFX concurrency docs | Yes - AI suggested using CountDownLatch to solve my problem of waiting for user dialog response from background thread | ~2% |
| File Naming Convention Logic | ChatGPT helped me understand DateTimeFormatter patterns | Yes - AI helped me figure out the date format string "dd_MM_yyyy-HH_mm_ss" for timestamped filenames | ~1% |

**Total External Code: ~6%**

Most of my code I wrote myself by learning from the official documentation for Apache POI, PDFBox, and JavaFX. The bits listed above are where I got stuck and needed help from online resources or AI to figure out the tricky parts.

---

## 2. Explanation of Critical Algorithms

### Algorithm 1: File Format Detection and Conversion Routing

This is my main algorithm that figures out what type of conversion the user wants and sends it to the right converter method.

**Pseudocode:**
```
BEGIN convertFile(sourceFile, targetFile, targetFormat)
    GET the source filename and make it lowercase
    MAKE the target format lowercase too
    
    IF source is ".docx" AND target is "pdf" THEN
        CALL my convertDocxToPdf method
    ELSE IF source is ".pdf" AND target is "docx" THEN
        CALL my convertPdfToDocx method
    ELSE IF source is ".csv" AND target is "xlsx" THEN
        CALL my convertCsvToXlsx method
    ELSE IF source is ".xlsx" AND target is "csv" THEN
        CALL my convertXlsxToCsv method
    ELSE IF source and target are both image formats THEN
        CALL my convertImage method
    ELSE
        RETURN false because conversion not supported
    END IF
    
    RETURN true if it worked
CATCH any errors
    DISPLAY error message to user
    RETURN false
END
```

**Explanation:** This algorithm is the heart of my file converter. It looks at what file the user selected and what format they want to convert it to, then it routes the conversion to the right method. I made sure to check for all the file types I support (DOCX, PDF, CSV, XLSX, and images like JPG/PNG/WEBP). If someone tries to convert something I don't support, it returns false and shows them an error.

---

### Algorithm 2: CSV to Excel Conversion

This algorithm reads a CSV file line by line and creates an Excel spreadsheet from it.

**Pseudocode:**
```
BEGIN convertCsvToXlsx(sourceFile, targetFile)
    OPEN the CSV file for reading
    CREATE a new Excel workbook
    CREATE a new sheet called "Sheet1"
    
    SET rowNum to 0
    
    WHILE there are more lines in the CSV file DO
        READ the current line
        SPLIT the line by commas to get each value
        CREATE a new row in Excel at position rowNum
        
        FOR each value in the split data DO
            CREATE a cell in the row
            PUT the value into the cell
        END FOR
        
        ADD 1 to rowNum
    END WHILE
    
    SAVE the workbook to the target file
    CLOSE all the files
END
```

**Explanation:** I needed a way to convert CSV files (which are just text with commas) into proper Excel files. This algorithm reads the CSV one line at a time, splits each line wherever there's a comma, and then puts each piece into its own Excel cell. The row counter keeps track of which row we're on so everything goes in the right place. It's pretty straightforward but it gets the job done.

---

### Algorithm 3: Background Thread Conversion Handler

This algorithm runs the file conversion in the background so my UI doesn't freeze up.

**Pseudocode:**
```
BEGIN handleConvertFile()
    GET the source file, target format, and output location from the UI fields
    
    IF any of the inputs are empty THEN
        SHOW an error message
        EXIT
    END IF
    
    MAKE the progress bar visible
    SET status text to "Converting..."
    
    CREATE a background task that does:
        SET up the source and target file paths
        IF target file already exists THEN
            ASK user if they want to overwrite it
            IF user says no THEN
                CANCEL the conversion
                EXIT task
            END IF
        END IF
        
        CALL the conversion handler to do the actual conversion
    END task
    
    WHEN task finishes successfully:
        HIDE the progress bar
        SET status to "Conversion completed successfully!"
        IF logging is turned on THEN
            SAVE the conversion to database history
        END IF
    
    IF task fails:
        HIDE the progress bar
        SHOW an error dialog with the error message
    
    START the background task
END
```

**Explanation:** One of the biggest problems I had was that when I ran file conversions (especially big files), my whole program would freeze and look like it crashed. So I learned about JavaFX Tasks to run the conversion in a background thread. This algorithm handles all the UI stuff - it validates the inputs, shows a progress bar, checks if files will be overwritten, and runs the conversion without freezing. When it's done, it updates the status and logs the conversion to my database if the user has that setting turned on.

---

## 3. Advanced Techniques

### Technique 1: Multi-Threading with JavaFX Tasks

I used JavaFX Task to run file conversions in the background so the UI doesn't freeze. This was really important because converting big files can take a while.

**Code Snippet from MainUIController.java:**
```java
Task<Void> conversionTask = new Task<Void>() {
    @Override
    protected Void call() throws Exception {
        File sourceFileObj = new File(sourceFile);
        // ... setup file paths ...
        conversionHandler.convertFile(sourceFileObj, targetFileObj, targetFormat);
        return null;
    }
    
    @Override
    protected void succeeded() {
        Platform.runLater(() -> {
            conversionProgressBar.setVisible(false);
            statusLabel.setText("Conversion completed successfully!");
            if (settingsManager.getLogSuccessfulConversions()) {
                historyDAO.addConversionRecord(new ConversionRecord(
                    sourceFile, outputPath, sourceFormat, targetFormat, true
                ));
            }
        });
    }
    
    @Override
    protected void failed() {
        Platform.runLater(() -> {
            conversionProgressBar.setVisible(false);
            statusLabel.setText("Conversion failed");
            showError("Conversion Failed", getException().getMessage());
        });
    }
};

new Thread(conversionTask).start();
```

**Why I used it:** At first my program would completely freeze when converting files, which looked like it crashed. I learned about JavaFX Tasks which let me run the conversion in a background thread. The `call()` method does the actual work, and `succeeded()` and `failed()` methods handle what happens after. I used `Platform.runLater()` to update the UI safely from the background thread. This keeps my program responsive even during long conversions.

---

### Technique 2: Thread Synchronization with CountDownLatch

I needed a way to show confirmation dialogs from background threads and wait for the user's response before continuing.

**Code Snippet from MainUIController.java:**
```java
private boolean confirmOverwrite(File file) {
    if (settingsManager.getOverwriteExistingFiles()) {
        return true;
    }
    
    CountDownLatch latch = new CountDownLatch(1);
    AtomicBoolean userResponse = new AtomicBoolean(false);
    
    Platform.runLater(() -> {
        try {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("File Exists");
            confirmAlert.setHeaderText("Overwrite Existing File?");
            confirmAlert.setContentText("The file '" + file.getName() + "' already exists. Do you want to overwrite it?");
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            userResponse.set(result.isPresent() && result.get() == ButtonType.OK);
        } finally {
            latch.countDown();
        }
    });
    
    try {
        latch.await();
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return false;
    }
    
    return userResponse.get();
}
```

**Why I used it:** This was probably the trickiest part. When my background conversion thread needs to ask the user if they want to overwrite a file, I can't just show a dialog because dialogs must run on the UI thread. So I use CountDownLatch to make the background thread wait. The latch starts at 1, the UI thread shows the dialog and saves the user's choice in AtomicBoolean, then counts down the latch. The background thread waits at `latch.await()` until the user responds. This way I can get user input from a background thread safely.

---

### Technique 3: SQLite Database with JDBC

I implemented a SQLite database to keep track of all the conversions users do, so they can see their history.

**Code Snippet from DatabaseManager.java:**
```java
public static void initialize() {
    try (Connection conn = connect();
        Statement stmt = conn.createStatement()) {

        String sql = "CREATE TABLE IF NOT EXISTS conversion_history (\n" + 
                     "id INTEGER PRIMARY KEY AUTOINCREMENT,\n" + 
                     "source_path TEXT NOT NULL,\n" + 
                     "target_path TEXT NOT NULL,\n" + 
                     "source_format TEXT NOT NULL,\n" + 
                     "target_format TEXT NOT NULL,\n" + 
                     "success BOOLEAN NOT NULL,\n" + 
                     "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP\n" +
                     ");";
        stmt.execute(sql);
        System.out.println("SUCCESS!!! Database initialised successfully.");
    } catch (SQLException e) {
        System.err.println("ANOTHER ERROR!!! Error initialising database: " + e.getMessage());
    }
}
```

**Why I used it:** I wanted users to be able to see what files they've converted before. Instead of just saving to a text file, I decided to use a proper database. SQLite is perfect because it's just a single file (conversion_history.db) and I don't need a separate database server. The `CREATE TABLE IF NOT EXISTS` makes sure the table is created when the program first runs. I store the source file, target file, formats, whether it succeeded, and a timestamp for each conversion.

---

### Technique 4: AES Encryption for ZIP Files

I added a feature to create password-protected ZIP files with proper AES encryption for security.

**Code Snippet from ZipHandler.java:**
```java
public void zipFolder(File sourceFolder, File targetZipFile, String password) throws IOException {
    ZipFile zipFile = new ZipFile(targetZipFile);
    ZipParameters parameters = new ZipParameters();

    if (password != null && !password.trim().isEmpty()) {
        parameters.setEncryptFiles(true);
        parameters.setEncryptionMethod(EncryptionMethod.AES);
        zipFile.setPassword(password.toCharArray());
    }

    zipFile.addFolder(sourceFolder, parameters);
}
```

**Why I used it:** I thought it would be cool if users could compress folders into ZIP files with password protection. I found the zip4j library which supports AES encryption (which is much more secure than the old ZIP encryption). If the user provides a password, I turn on encryption and set it to use AES. If they don't provide a password, it just creates a normal unencrypted ZIP. This gives users the option to protect sensitive files.

---

### Technique 5: Singleton Pattern for Settings

I used the Singleton design pattern for my SettingsManager so there's only ever one instance managing all the settings.

**Code Snippet from SettingsManager.java:**
```java
public class SettingsManager {
    private static final String SETTINGS_FILE = "settings.properties";
    private static SettingsManager instance;
    private Properties properties = new Properties();

    private SettingsManager() {
        loadSettings();
    }

    public static synchronized SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }
    // ... other methods ...
}
```

**Why I used it:** I needed a way to manage settings (like default output folder, file naming conventions, etc.) that every part of my program could access. The Singleton pattern means there's only one SettingsManager object in my entire program. The constructor is private so nobody can make new instances with `new SettingsManager()`. Instead, they have to call `getInstance()` which either creates the one instance (if it doesn't exist) or returns the existing one. This prevents bugs where different parts of the program might have different setting values. The `synchronized` keyword makes it thread-safe.

---

### Technique 6: JavaFX Observable Collections and Data Binding

I used JavaFX's ObservableList and FilteredList to make my history table automatically update when data changes.

**Code Snippet from HistoryUIController.java:**
```java
private ObservableList<ConversionRecord> allRecords;
private FilteredList<ConversionRecord> filteredRecords;

public void initialize(URL location, ResourceBundle resources) {
    // Load all records from database
    allRecords = FXCollections.observableArrayList(historyDAO.getAllRecords());
    
    // Create filtered list wrapper
    filteredRecords = new FilteredList<>(allRecords, record -> true);
    
    // Bind table to filtered list
    historyTable.setItems(filteredRecords);
    
    // Set up filtering
    filterField.textProperty().addListener((obs, oldText, newText) -> {
        filteredRecords.setPredicate(record -> {
            if (newText == null || newText.isEmpty()) {
                return true;
            }
            String lowerCase = newText.toLowerCase();
            return record.getSourcePath().toLowerCase().contains(lowerCase) ||
                   record.getTargetPath().toLowerCase().contains(lowerCase);
        });
    });
}
```

**Why I used it:** For my history screen, I wanted users to be able to filter the table by typing in a search box. ObservableList automatically notifies the TableView when items are added or removed. I wrapped it in a FilteredList which lets me set a predicate (filter condition) that determines which items show up. Whenever the user types in the search box, I update the predicate and the table automatically refreshes to show only matching records. This is way better than manually refreshing the table every time something changes.

---

### Technique 7: Dynamic File Naming with DateTimeFormatter

I implemented different file naming options so users can choose how converted files are named.

**Code Snippet from SettingsManager.java:**
```java
public String applyFileNamingConvention(String baseFileName, String extension) {
    String convention = getFileNamingConvention();
    
    switch (convention) {
        case "Add timestamp":
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd_MM_yyyy-HH_mm_ss");
            return baseFileName + "_" + now.format(formatter) + extension;
            
        case "Add _converted suffix":
            return baseFileName + "_converted" + extension;
            
        case "Keep original name":
        default:
            return baseFileName + extension;
    }
}
```

**Why I used it:** Users wanted different ways to name their converted files. Some wanted to keep the original name, others wanted timestamps so they don't overwrite old conversions, and some just wanted a "_converted" suffix. I used Java's DateTimeFormatter to create a timestamp in the format "day_month_year-hour_minute_second" (like "04_11_2024-15_30_45"). The switch statement checks which convention the user chose in settings and applies it. This makes the file naming flexible without cluttering up my conversion code.

---

## Summary

This PAT project was a great learning experience. I got to work with file handling (reading and writing different formats), database operations (SQLite), multi-threading (JavaFX Tasks), and user interface design. The hardest part was definitely figuring out the threading stuff - making sure the UI doesn't freeze but also being able to show dialogs from background threads. I'm pretty proud of how it turned out, especially the encryption feature and the conversion history tracking.

**Total Lines of Code:** About 2,700 lines of Java  
**External Code:** Around 6% (mainly database setup and threading patterns from online help)
