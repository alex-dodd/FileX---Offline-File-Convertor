# PAT Report: Algorithms and Advanced Techniques

**Project:** FileX - Offline File Converter  
**Student:** Alex Dodd  
**Purpose:** Grade 11 IT School PAT (Practical Assessment Task)

---

## 1. Externally Sourced Code

| Code Snippet/Section | Source/Link | AI Assistance | % of Total Code |
|---------------------|-------------|---------------|-----------------|
| Database Setup and SQL Schema | SQLite documentation (https://www.sqlite.org/docs.html) and online tutorials for JDBC PreparedStatements | Yes - ChatGPT helped me debug the SQL CREATE TABLE syntax and fix parameter binding issues in PreparedStatements | ~3% |
| Thread Synchronization with CountDownLatch | Stack Overflow discussion on JavaFX threading (https://stackoverflow.com/questions/29449297/) and Oracle JavaFX concurrency docs | Yes - AI suggested using CountDownLatch to solve my problem of waiting for user dialog response from background thread | ~2% |
| PDFBox Document Content Stream Setup | Apache PDFBox documentation (https://pdfbox.apache.org/) and Stack Overflow post about text positioning (https://stackoverflow.com/questions/31260351/) | Yes - ChatGPT helped me understand how to use PDPageContentStream, setLeading(), and newLineAtOffset() for proper text layout in PDFs | ~2% |
| JavaFX TableView with Custom Cell Factories | JavaFX documentation and Baeldung tutorial on TableView (https://www.baeldung.com/javafx-tableview) | No - figured this out from the docs | ~1% |
| ZIP File Encryption with zip4j Library | zip4j GitHub documentation (https://github.com/srikanth-lingala/zip4j) and Stack Overflow discussion on AES encryption | Yes - AI helped me understand how to set up ZipParameters and EncryptionMethod.AES properly | ~2% |

**Total External Code: ~10%**

Most of my code I wrote myself by learning from the official documentation for Apache POI, PDFBox, and JavaFX. The bits listed above are where I got stuck and needed help from online resources or AI to figure out the tricky parts. The complex stuff like thread synchronization and PDF content streams were particularly hard and I definitely needed outside help for those.

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

**Explanation:** So this is basically the brain of my whole converter. When someone picks a file and chooses what format they want, this figures out which conversion method to use. I check the file extension of what they selected, match it with the target format they chose, and send it to the right converter method. Covers all the file types I built support for - documents (DOCX/PDF), spreadsheets (CSV/XLSX), and images (JPG/PNG/WEBP). If they try something I haven't coded yet, it just returns false and shows an error instead of crashing.

---

### Algorithm 2: CSV to Excel Conversion

This one reads through a CSV file (just plain text with commas) and turns it into a proper Excel spreadsheet.

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

**Explanation:** CSV files are basically just text with commas separating everything, so I needed to parse them properly and create actual Excel files. Goes through the CSV one line at a time, splits wherever there's a comma to get each value, and sticks each value in its own cell in Excel. The rowNum counter keeps track of what row we're working on. Pretty simple loop but it works well for converting data tables.

---

### Algorithm 3: Background Thread Conversion Handler

This handles running conversions in the background so the whole program doesn't freeze while files are being converted.

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

**Explanation:** Had a major issue where converting files (especially big ones) would make the whole UI freeze up and look crashed. Learned about JavaFX Tasks which let you run stuff in the background. This algorithm manages everything UI-related around the conversion - validates what the user typed in, shows a progress bar so they know it's working, checks if they're about to overwrite an existing file, and actually runs the conversion on a separate thread. When it finishes, updates the status message and saves the conversion to the database if logging is turned on in settings.

---

## 3. Advanced Techniques

### Technique 1: Multi-Threading with JavaFX Tasks

So one of the first major issues I ran into was my UI completely freezing whenever someone tried to convert a big file. It would just hang there and look like it crashed - not great. I used JavaFX Task to run conversions in the background so the UI stays responsive.

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

**Why I used it:** Like I said, my program kept freezing up during conversions and users would think it crashed. I found out about JavaFX Tasks which basically let you run stuff in the background. The `call()` method is where the actual conversion happens, and then `succeeded()` and `failed()` handle what to do when it finishes. Had to use `Platform.runLater()` to update the UI from the background thread - you can't just update UI stuff directly from another thread or JavaFX throws a fit. This keeps everything running smooth even when processing large files.

---

### Technique 2: Thread Synchronization with CountDownLatch

This one was a real headache to figure out. I needed to show confirmation dialogs from my background threads and wait for the user's answer before continuing the conversion.

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

**Why I used it:** Okay so this was honestly the hardest part to wrap my head around. When my background thread is doing a conversion and needs to ask the user "hey do you want to overwrite this file?", I can't just pop up a dialog because JavaFX dialogs MUST run on the UI thread. If you try to show them from another thread, it crashes. So I had to use CountDownLatch to basically pause the background thread until the user responds. The latch starts at 1, then the UI thread shows the dialog and gets the user's answer (stored in an AtomicBoolean), and finally calls `latch.countDown()`. Meanwhile the background thread is sitting at `latch.await()` just waiting. Once the user clicks, the latch hits 0 and the background thread can continue. Took me forever to understand this but it works perfectly now.

---

### Technique 3: SQLite Database with JDBC

I wanted to add a history feature so users could see all their past conversions. A database seemed like the right way to do this instead of just writing to some text file.

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

**Why I used it:** So I thought it would be cool if users could look back and see what conversions they've done. Could've just written to a text file or something but a proper database felt more professional. SQLite is great because it's literally just one file (conversion_history.db) sitting in my program folder - no need to install MySQL or anything complicated. The `CREATE TABLE IF NOT EXISTS` part makes sure the table gets created the first time someone runs the program. I'm storing everything about each conversion: what files, what formats, if it worked, and when it happened. Makes for a nice history screen.

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

**Why I used it:** Thought it would be a cool feature to let people make password-protected ZIP files. Found this zip4j library that does AES encryption which is way more secure than the basic ZIP password thing (apparently that's super easy to crack). If someone types in a password, I enable encryption and set it to AES mode. If they leave it blank, just makes a regular ZIP. Pretty straightforward once I figured out the library.

---

### Technique 5: Singleton Pattern for Settings

Had to figure out a way to manage settings across my whole program without things getting messy. Went with the Singleton pattern for the SettingsManager.

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

**Why I used it:** Settings like default folders and naming options need to be accessible from anywhere in the program. Singleton pattern makes sure there's only ever ONE SettingsManager object. Made the constructor private so you can't just do `new SettingsManager()` - you have to call `getInstance()` which either makes the single instance if it doesn't exist yet, or gives you the existing one. Prevents weird bugs where different parts of the code might have different settings loaded. Added `synchronized` to make it thread-safe since I'm doing multithreading elsewhere.

---

### Technique 6: JavaFX Observable Collections and Data Binding

Needed to make a searchable history table that updates automatically. Used ObservableList and FilteredList for this.

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

**Why I used it:** Wanted a search box where users could type and the table would filter in real-time. ObservableList is cool because it tells the TableView whenever stuff gets added or removed automatically. Wrapped it in a FilteredList which lets me set conditions for what shows up. When someone types in the search box, I just update the filter and boom - table updates by itself. Way easier than coding manual refresh logic every time the data changes.

---

### Technique 7: Dynamic File Naming with DateTimeFormatter

Added options for how converted files get named - some people wanted timestamps, others just wanted "_converted" added.

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

**Why I used it:** Different people wanted different ways to name their files. Some wanted to keep the original name, some wanted timestamps so files don't overwrite each other, and some just wanted "_converted" stuck on the end. Used DateTimeFormatter to generate timestamps in the format "04_11_2024-15_30_45" - had to use underscores instead of slashes or colons because those aren't allowed in filenames. Switch statement checks what the user picked in settings and applies it. Keeps the conversion code clean since all the naming logic is in one place.

---

## Summary

This PAT project ended up being way more work than I expected but I learned a ton. Got to mess around with file handling for all different formats, set up a proper database with SQLite, figure out multi-threading so the UI doesn't freeze, and build a decent-looking interface. The threading stuff was definitely the hardest - took me ages to understand how to make background threads work with JavaFX and not have everything crash. Pretty happy with how it came out though, especially the ZIP encryption and the history tracking features.

**Total Lines of Code:** About 2,700 lines of Java  
**External Code:** Around 10% (mainly database setup, threading patterns, and PDF stuff from online help)
