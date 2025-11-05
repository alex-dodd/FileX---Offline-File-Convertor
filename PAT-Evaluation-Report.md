# FileX - Project Evaluation Report

**Student:** Alex Dodd  
**Project:** FileX - Offline File Converter  
**Date:** November 2024

---

## 4.2.1 Evaluation of the Programmed Solution

Looking at what I planned in section 1.3 versus what actually got built, here's an honest breakdown of how FileX turned out:

### 1. Batch Folder Conversion

**Status:** Not Implemented

What I wanted: Process entire folders with subfolders in one click, auto-detect file types with MIME checks, skip unsupported formats, and mirror folder structure.

What actually works: Only single file conversions. You have to convert files one at a time through the UI.

**Why it's missing:** This was honestly way more complex than I thought. Would've needed recursive folder traversal, MIME type detection (which I learned isn't straightforward in Java), and duplicate handling across multiple directory levels. Ran out of time trying to get the basic conversions working properly first.

**How to fix it:** 
- Add a "Batch" tab to the UI with folder selection
- Use `Files.walk()` to recursively traverse directories
- Implement MIME detection with `java.nio.file.Files.probeContentType()` or Apache Tika library
- Create matching output folder structure using `Files.createDirectories()`
- Add progress tracking showing "file X of Y" instead of just a spinner

**Alternative approach:** Could also do a simpler version where you select multiple files instead of a whole folder. Less impressive but way easier to implement and still useful.

---

### 2. Multithreaded Processing

**Status:** Partially Implemented

What I wanted: Thread pool processing 4 files at once, real-time progress metrics (files per second, ETA).

What actually works: Single-file conversions run on background threads using JavaFX Task so the UI doesn't freeze. Only processes one file at a time though.

**Why it's partial:** Got the threading concept working to prevent UI freezing (which was the main issue I was facing), but never implemented the thread pool for multiple simultaneous conversions. The progress bar just shows indeterminate spinning, not actual completion percentage or metrics.

**How to fix it:**
- Replace single Task with ExecutorService thread pool: `Executors.newFixedThreadPool(4)`
- Add file counter and timer to track conversion rate
- Calculate ETA based on average time per file
- Update progress bar to show actual percentage instead of spinner
- Display "Processing file 3/10" type messages

**Suggestion:** Start with a pool of 2 threads first and test stability. 4 threads might cause issues on weaker machines, especially with large files.

---

### 3. Conflict Resolution

**Status:** Partially Implemented

What I wanted: Auto-rename duplicates (file(1).pdf style), generate error logs for corrupted files, save logs to database.

What actually works: Asks user with a dialog if they want to overwrite when a file exists. Can be disabled in settings to always overwrite. No auto-renaming or error logging though.

**Why it's partial:** The overwrite dialog works fine, but I never got around to implementing smart auto-renaming or proper error logging. Errors just print to console which isn't very helpful for users.

**How to fix it:**
- Add method to check for duplicates and append numbers: check if file exists, if yes try filename(1), filename(2), etc. until finding unused name
- Create error log table in database with columns: timestamp, filename, error_type, error_message
- Log failed conversions to this table
- Add "View Error Log" button in history tab to show problematic files
- Maybe add export error log to CSV option

**Better approach:** Give users choice in settings: "On duplicate: Ask me / Auto-rename / Always overwrite / Skip". More flexible than forcing one behavior.

---

### 4. Supported Formats

**Status:** Mostly Implemented

What I wanted: Images (WebP ↔ PNG/JPG, SVG → PNG), Documents (DOCX ↔ PDF, XLSX ↔ CSV), Archives (password-protected ZIP).

What actually works:
- ✅ Images: JPG/PNG/WEBP conversions work (WebP support via TwelveMonkeys)
- ❌ SVG to PNG: Not implemented (Apache Batik library not added)
- ✅ Documents: DOCX ↔ PDF works but very basic text extraction only
- ✅ Spreadsheets: XLSX ↔ CSV works well
- ✅ Archives: ZIP with AES password encryption works (zip4j)

**Issues found:**
- DOCX to PDF loses all formatting (no bold, italics, images, tables). Just dumps plain text onto PDF pages.
- PDF to DOCX also loses formatting. Just extracts text into one paragraph.
- Can't handle complex spreadsheets with formulas or multiple sheets (only converts first sheet)

**How to improve:**
- For better PDF conversion, use Apache FOP or docx4j library instead of manual PDFBox content streams
- Add SVG support by including Apache Batik dependency and implementing SVG renderer
- Handle multi-sheet Excel files by asking user which sheet to convert or converting all sheets
- Preserve basic formatting like bold/italic/underline when converting documents

**Realistic fix:** The document formatting is really hard. Maybe just add a warning that says "Note: Only plain text is converted. Formatting and images are not preserved." Better to set expectations than promise features that don't work right.

---

### 5. User Tools

**Status:** Not Implemented

What I wanted: Drag-and-drop files into highlighted zone, save/load conversion presets, export history to CSV.

What actually works:
- ❌ No drag-and-drop (have to use browse buttons)
- ❌ No presets feature
- ❌ Can't export history to CSV (history is view-only in app)

**Why it's missing:** These are all "nice to have" features that I deprioritized when core conversion functionality was taking longer than expected. Drag-and-drop especially seemed complicated with JavaFX.

**How to add them:**

*Drag-and-drop:*
```java
// Add to FXML component
setOnDragOver(event -> {
    if (event.getDragboard().hasFiles()) {
        event.acceptTransferModes(TransferMode.COPY);
    }
});

setOnDragDropped(event -> {
    List<File> files = event.getDragboard().getFiles();
    sourceFileField.setText(files.get(0).getAbsolutePath());
});
```

*Presets:* 
- Create presets table in database (preset_name, target_format, output_location, naming_convention)
- Add "Save Current as Preset" and "Load Preset" buttons
- Dropdown to select and apply saved presets

*CSV Export:*
- Add "Export to CSV" button in History tab
- Use existing conversion history data
- Write to CSV with columns: Date, Source File, Target Format, Status, Output Location

**Priority:** I'd do CSV export first (easiest), then drag-and-drop (most impressive), then presets (least critical).

---

### 6. Accessibility

**Status:** Not Implemented

What I wanted: High-contrast mode (black background, white/orange text), keyboard shortcuts (Ctrl+O open, Ctrl+S convert/save).

What actually works: Default JavaFX styling only. No accessibility features.

**Why it's missing:** Ran out of time. This was definitely lower priority than getting conversions working.

**How to add it:**

*High-contrast mode:*
- Create alternate CSS file: `high-contrast.css` with dark theme
- Add toggle button in settings
- Load alternate stylesheet: `scene.getStylesheets().add("high-contrast.css")`
- Use CSS like: `background-color: #000000; -fx-text-fill: #FFFFFF;`

*Keyboard shortcuts:*
```java
scene.setOnKeyPressed(event -> {
    if (event.isControlDown() && event.getCode() == KeyCode.O) {
        handleBrowseFile();
    } else if (event.isControlDown() && event.getCode() == KeyCode.S) {
        handleConvertFile();
    }
});
```

**Realistic plan:** High-contrast mode would take maybe 2-3 hours to implement properly. Keyboard shortcuts could be done in under an hour. Both would make the app way more professional.

---

## Summary of What Actually Works

**Working well:**
- Single file conversions for images (JPG/PNG/WEBP)
- CSV ↔ XLSX spreadsheet conversions
- Password-protected ZIP creation
- Conversion history tracking in SQLite database
- Settings persistence (naming conventions, default folders, overwrite behavior)
- Background threading (UI doesn't freeze during conversions)

**Partially working:**
- Document conversions (DOCX ↔ PDF) but text-only, no formatting
- File overwrite handling (dialog works, but no auto-rename)
- Threading (prevents freezing, but no parallel processing or progress metrics)

**Not working:**
- Batch folder conversion
- Drag-and-drop
- Presets
- SVG support
- Accessibility features
- Error logging
- History CSV export

**Overall assessment:** I'd say I got about 50-60% of what I originally planned working. The core conversion features work for basic use cases, but a lot of the advanced features and polish didn't make it. It's functional enough to use for simple file conversions, but definitely not production-ready or feature-complete compared to the original vision.

The biggest learning was that I way underestimated how long things would take. Features that seemed simple ("just add drag-and-drop") turned out to need way more code than expected. If I could redo it, I'd focus on doing fewer features really well instead of partially implementing everything.

---

## 4.2.2 Functional Testing

Below are two complete sets of functional testing to verify which features from section 1.3 are working. Tests were conducted with different users on different dates to get varied perspectives.

### Test Set 1: Initial Feature Testing

| Feature Being Tested | Test Description | Works? ☐ | Partial? ☐ | Broken? ☐ | Tester Comments |
|---------------------|------------------|----------|------------|-----------|-----------------|
| Application Launch | Open FileX from JAR file | ☐ | ☐ | ☐ | |
| Main Window Display | Check if UI loads properly with all tabs visible | ☐ | ☐ | ☐ | |
| Browse File Button | Click browse and select a test DOCX file | ☐ | ☐ | ☐ | |
| Format Selection | Choose target format from dropdown | ☐ | ☐ | ☐ | |
| Output Location | Browse and select output folder | ☐ | ☐ | ☐ | |
| DOCX to PDF Conversion | Convert sample Word doc to PDF | ☐ | ☐ | ☐ | |
| PDF to DOCX Conversion | Convert sample PDF to Word doc | ☐ | ☐ | ☐ | |
| Image Conversion (PNG to JPG) | Convert PNG image to JPG format | ☐ | ☐ | ☐ | |
| Image Conversion (JPG to WEBP) | Convert JPG to WebP format | ☐ | ☐ | ☐ | |
| WEBP to PNG Conversion | Convert WebP image back to PNG | ☐ | ☐ | ☐ | |
| CSV to XLSX Conversion | Convert spreadsheet from CSV to Excel | ☐ | ☐ | ☐ | |
| XLSX to CSV Conversion | Convert Excel file to CSV | ☐ | ☐ | ☐ | |
| Overwrite Dialog | Try converting to existing file, check if prompt appears | ☐ | ☐ | ☐ | |
| Conversion Progress | Verify progress bar shows during conversion | ☐ | ☐ | ☐ | |
| Success Message | Check if "Conversion completed successfully!" appears | ☐ | ☐ | ☐ | |
| Error Handling | Try converting unsupported format, check error message | ☐ | ☐ | ☐ | |
| History Tab | Open history tab and check if recent conversions show | ☐ | ☐ | ☐ | |
| History Search | Type filename in search box, verify filtering works | ☐ | ☐ | ☐ | |
| Clear History | Click clear history button and confirm deletion | ☐ | ☐ | ☐ | |
| Settings Tab | Open settings tab and verify options load | ☐ | ☐ | ☐ | |
| Default Output Location | Set default output folder in settings | ☐ | ☐ | ☐ | |
| Auto-Overwrite Setting | Enable "Always overwrite" and test behavior | ☐ | ☐ | ☐ | |
| File Naming - Timestamp | Set naming to "Add timestamp" and test output | ☐ | ☐ | ☐ | |
| File Naming - Suffix | Set naming to "Add _converted suffix" and test | ☐ | ☐ | ☐ | |
| Conversion Logging Toggle | Disable conversion logging and verify history not saved | ☐ | ☐ | ☐ | |
| ZIP Creation | Go to Archive tab, select folder, create ZIP | ☐ | ☐ | ☐ | |
| Password-Protected ZIP | Create ZIP with password and test extraction | ☐ | ☐ | ☐ | |
| ZIP Without Password | Create regular ZIP without encryption | ☐ | ☐ | ☐ | |
| Large File Handling | Convert large file (>50MB) without crash | ☐ | ☐ | ☐ | |
| UI Responsiveness | Verify UI doesn't freeze during conversion | ☐ | ☐ | ☐ | |
| Documentation Tab | Open documentation and verify help content loads | ☐ | ☐ | ☐ | |
| Documentation Search | Search for feature in documentation | ☐ | ☐ | ☐ | |
| Tooltips | Hover over buttons to check tooltip text appears | ☐ | ☐ | ☐ | |
| Window Resize | Resize window and verify layout adjusts properly | ☐ | ☐ | ☐ | |
| Tab Navigation | Switch between all tabs without errors | ☐ | ☐ | ☐ | |
| Exit Application | Close app and verify database saved properly | ☐ | ☐ | ☐ | |

**Tester Name:** ________________________________  
**Date Tested:** ________________________________  
**Overall Result:** ________________________________

---

### Test Set 2: Advanced Feature Testing

| Feature Being Tested | Test Description | Works? ☐ | Partial? ☐ | Broken? ☐ | Tester Comments |
|---------------------|------------------|----------|------------|-----------|-----------------|
| Batch Folder Conversion | Select folder with multiple files for conversion | ☐ | ☐ | ☐ | |
| Subfolder Processing | Convert folder containing subfolders | ☐ | ☐ | ☐ | |
| MIME Type Detection | Drop unsupported file (exe/psd) and verify it's skipped | ☐ | ☐ | ☐ | |
| Folder Structure Mirroring | Verify output folder matches input structure | ☐ | ☐ | ☐ | |
| Multi-Threading (4 files) | Monitor if 4 files process simultaneously | ☐ | ☐ | ☐ | |
| Files Per Second Display | Check if conversion rate shows in UI | ☐ | ☐ | ☐ | |
| Estimated Time Remaining | Verify ETA appears during batch conversion | ☐ | ☐ | ☐ | |
| Progress Bar Percentage | Check if progress shows actual % complete | ☐ | ☐ | ☐ | |
| Auto-Rename Duplicates | Convert to existing file, check if file(1) created | ☐ | ☐ | ☐ | |
| Auto-Rename Sequential | Test multiple duplicates (file(1), file(2), file(3)) | ☐ | ☐ | ☐ | |
| Error Log Generation | Cause conversion error, check if logged | ☐ | ☐ | ☐ | |
| View Error Log | Access error log from history tab | ☐ | ☐ | ☐ | |
| Export Error Log | Export errors to CSV file | ☐ | ☐ | ☐ | |
| SVG to PNG Conversion | Convert SVG vector graphic to PNG | ☐ | ☐ | ☐ | |
| Document Formatting Preservation | Convert DOCX with bold/italic, check PDF output | ☐ | ☐ | ☐ | |
| Images in Documents | Convert DOCX with images, verify in PDF | ☐ | ☐ | ☐ | |
| Tables in Documents | Convert document with tables, check formatting | ☐ | ☐ | ☐ | |
| Multiple Excel Sheets | Convert XLSX with 3 sheets, check all convert | ☐ | ☐ | ☐ | |
| Excel Formulas | Convert XLSX with formulas, check if preserved | ☐ | ☐ | ☐ | |
| Drag-and-Drop Single File | Drag file onto drop zone | ☐ | ☐ | ☐ | |
| Drag-and-Drop Multiple Files | Drag 5 files onto drop zone | ☐ | ☐ | ☐ | |
| Drag-and-Drop Folder | Drag entire folder onto drop zone | ☐ | ☐ | ☐ | |
| Save Conversion Preset | Create preset "High Quality PDF" and save | ☐ | ☐ | ☐ | |
| Load Conversion Preset | Apply saved preset to new file | ☐ | ☐ | ☐ | |
| Delete Preset | Remove saved preset from list | ☐ | ☐ | ☐ | |
| Export History to CSV | Export all conversion history to CSV file | ☐ | ☐ | ☐ | |
| History CSV Content | Open exported CSV, verify all fields present | ☐ | ☐ | ☐ | |
| High-Contrast Mode Toggle | Enable high-contrast theme in settings | ☐ | ☐ | ☐ | |
| High-Contrast Readability | Check if text readable in dark mode | ☐ | ☐ | ☐ | |
| Keyboard Shortcut: Ctrl+O | Press Ctrl+O to open file browser | ☐ | ☐ | ☐ | |
| Keyboard Shortcut: Ctrl+S | Press Ctrl+S to start conversion | ☐ | ☐ | ☐ | |
| Keyboard Shortcut: Ctrl+H | Press Ctrl+H to open history | ☐ | ☐ | ☐ | |
| Memory Usage (Large Files) | Convert 100MB+ file, monitor RAM usage | ☐ | ☐ | ☐ | |
| Multi-Conversion Stability | Run 20 conversions in a row without crash | ☐ | ☐ | ☐ | |
| Network Independence | Verify app works with internet disconnected | ☐ | ☐ | ☐ | |
| Cross-Platform (Windows) | Test all features on Windows PC | ☐ | ☐ | ☐ | |

**Tester Name:** ________________________________  
**Date Tested:** ________________________________  
**Overall Result:** ________________________________

---

## 4.2.3 Test Plan and Results for TWO Input Variables

This section documents structured testing for two critical input variables using standard, extreme, and abnormal data. Screenshots showing before and after states should be inserted in the designated spaces below each test case.

### Input Variable 1: File Size (Image Conversion)

**Test Objective:** Verify that FileX correctly handles image conversions across a range of file sizes without crashes, data loss, or excessive processing time.

**Input Variable:** Image file size (PNG to JPG conversion)  
**Test File Type:** PNG images of varying file sizes  
**Target Format:** JPG

#### Test Cases for File Size Variable

| Test Type | File Size | Test File Details | Expected Result |
|-----------|-----------|-------------------|-----------------|
| Standard | 500 KB | Typical smartphone photo (1920x1080) | Converts successfully in <5 seconds |
| Standard | 2 MB | High-quality camera photo (4000x3000) | Converts successfully in <10 seconds |
| Extreme (Lower) | 2 KB | Tiny 50x50px icon image | Converts without error, maintains image integrity |
| Extreme (Upper) | 50 MB | Large 8000x6000px professional photo | Converts without crash, may take 30+ seconds |
| Extreme (Upper) | 100 MB | Massive 12000x8000px image | System handles gracefully (or shows appropriate error) |
| Abnormal | 0 KB | Empty file or corrupted PNG | Shows error message "Cannot read image file" |
| Abnormal | Non-image | Text file renamed to .png | Shows error message or skips conversion |

#### How to Conduct This Test:

1. **Prepare test files:**
   - Create or download PNG images matching the sizes above
   - Name them clearly: `test_500kb.png`, `test_2mb.png`, `test_2kb.png`, etc.
   - For abnormal test, create empty file and rename .txt to .png

2. **For each test case:**
   - Open FileX application
   - Select the test PNG file using browse button
   - Choose "JPG" as target format
   - Select output location
   - Click "Convert"
   - **Take screenshot BEFORE clicking convert** (showing file details)
   - **Take screenshot AFTER conversion completes** (showing output file and any messages)
   - Record actual time taken
   - Verify output JPG file opens correctly
   - Check output file size is reasonable

3. **Screenshot Requirements:**
   - Before: Show selected file with size visible in file browser
   - After: Show conversion success/error message AND output file properties

---

**INSERT SCREENSHOTS BELOW:**

#### Standard Test - 500 KB File

**BEFORE Screenshot:**

`[Insert screenshot showing selection of test_500kb.png file with file size visible]`

---

**AFTER Screenshot:**

`[Insert screenshot showing successful conversion message and output JPG file properties]`

**Actual Result:** _______________________________________________

**Time Taken:** _______________________________________________

---

#### Standard Test - 2 MB File

**BEFORE Screenshot:**

`[Insert screenshot showing selection of test_2mb.png file]`

---

**AFTER Screenshot:**

`[Insert screenshot showing successful conversion and output file]`

**Actual Result:** _______________________________________________

**Time Taken:** _______________________________________________

---

#### Extreme Test - 2 KB Tiny File

**BEFORE Screenshot:**

`[Insert screenshot showing selection of test_2kb.png tiny icon file]`

---

**AFTER Screenshot:**

`[Insert screenshot showing conversion result and output file, verify image quality maintained]`

**Actual Result:** _______________________________________________

**Time Taken:** _______________________________________________

---

#### Extreme Test - 50 MB Large File

**BEFORE Screenshot:**

`[Insert screenshot showing selection of test_50mb.png large file]`

---

**AFTER Screenshot:**

`[Insert screenshot showing conversion progress/result and output file]`

**Actual Result:** _______________________________________________

**Time Taken:** _______________________________________________

---

#### Extreme Test - 100 MB Massive File

**BEFORE Screenshot:**

`[Insert screenshot showing selection of test_100mb.png massive file]`

---

**AFTER Screenshot:**

`[Insert screenshot showing how system handles this extreme case - success or appropriate error]`

**Actual Result:** _______________________________________________

**Time Taken:** _______________________________________________

---

#### Abnormal Test - 0 KB Empty File

**BEFORE Screenshot:**

`[Insert screenshot showing selection of empty_file.png with 0 KB size]`

---

**AFTER Screenshot:**

`[Insert screenshot showing error message displayed by FileX]`

**Actual Result:** _______________________________________________

**Error Message Shown:** _______________________________________________

---

#### Abnormal Test - Non-Image File

**BEFORE Screenshot:**

`[Insert screenshot showing selection of textfile.png (actually a text file)]`

---

**AFTER Screenshot:**

`[Insert screenshot showing error handling when attempting to convert non-image]`

**Actual Result:** _______________________________________________

**Error Message Shown:** _______________________________________________

---

### Input Variable 2: File Path Characters (Document Conversion)

**Test Objective:** Verify that FileX correctly handles file paths containing special characters, spaces, and different lengths without errors or path resolution issues.

**Input Variable:** File path and filename characters (DOCX to PDF conversion)  
**Test File Type:** Simple DOCX document (same content, different names/paths)  
**Target Format:** PDF

#### Test Cases for File Path Variable

| Test Type | File Path / Name | Test Description | Expected Result |
|-----------|------------------|------------------|-----------------|
| Standard | `C:\Documents\report.docx` | Normal path with simple filename | Converts successfully |
| Standard | `C:\My Files\project report.docx` | Path with space in folder name | Converts successfully |
| Extreme (Length) | Path >200 characters | Very long nested folder path | Converts or shows clear error |
| Extreme (Depth) | Folder nested 15 levels deep | Testing path depth limit | Handles gracefully |
| Abnormal | `report!@#$%.docx` | Special characters in filename | Converts (or rejects with clear message) |
| Abnormal | `תּוֹרָה.docx` | Unicode/Hebrew characters | Handles unicode correctly |
| Abnormal | `file..docx` (double dot) | Invalid filename format | Shows error about invalid filename |
| Abnormal | Read-only destination | Output folder is read-only | Shows permission error message |

#### How to Conduct This Test:

1. **Prepare test environment:**
   - Create identical DOCX file content (simple text document: "This is a test document.")
   - Save copies with different names/paths matching test cases
   - Create folder structures for nested path tests
   - Set up read-only folder for permission test

2. **For each test case:**
   - Place test DOCX file in the specified path
   - Open FileX
   - Browse and select the test file
   - Choose PDF as target format
   - Select output location
   - **Take screenshot BEFORE conversion** (showing file path clearly visible)
   - Click convert
   - **Take screenshot AFTER conversion** (showing result and output file path)
   - Verify PDF was created in correct location
   - Open PDF to verify content converted correctly

3. **Screenshot Requirements:**
   - Before: Show file browser with full path visible
   - After: Show success/error message AND file explorer with output file

---

**INSERT SCREENSHOTS BELOW:**

#### Standard Test - Simple Path

**File Path:** `C:\Documents\report.docx`

**BEFORE Screenshot:**

`[Insert screenshot showing FileX with simple path selected]`

---

**AFTER Screenshot:**

`[Insert screenshot showing successful conversion with output PDF]`

**Actual Result:** _______________________________________________

---

#### Standard Test - Path With Spaces

**File Path:** `C:\My Files\project report.docx`

**BEFORE Screenshot:**

`[Insert screenshot showing path with spaces selected]`

---

**AFTER Screenshot:**

`[Insert screenshot showing conversion result]`

**Actual Result:** _______________________________________________

---

#### Extreme Test - Very Long Path

**File Path:** (200+ characters)  
`C:\Users\Alex\Documents\School\Year11\IT\Projects\PAT\FileX\Testing\Subfolder1\Subfolder2\Subfolder3\Subfolder4\Subfolder5\Subfolder6\Subfolder7\Subfolder8\very_long_filename_for_testing_extreme_cases.docx`

**BEFORE Screenshot:**

`[Insert screenshot showing extremely long path in file browser]`

---

**AFTER Screenshot:**

`[Insert screenshot showing how FileX handles this - success or error]`

**Actual Result:** _______________________________________________

---

#### Extreme Test - Deep Nesting (15 Folders Deep)

**File Path:** (example structure)  
`C:\Folder1\Folder2\Folder3\Folder4\Folder5\Folder6\Folder7\Folder8\Folder9\Folder10\Folder11\Folder12\Folder13\Folder14\Folder15\report.docx`

**BEFORE Screenshot:**

`[Insert screenshot showing deeply nested path]`

---

**AFTER Screenshot:**

`[Insert screenshot showing conversion result]`

**Actual Result:** _______________________________________________

---

#### Abnormal Test - Special Characters in Filename

**File Path:** `C:\Documents\report!@#$%.docx`

**BEFORE Screenshot:**

`[Insert screenshot showing filename with special characters]`

---

**AFTER Screenshot:**

`[Insert screenshot showing how FileX handles special characters]`

**Actual Result:** _______________________________________________

**Error Message (if any):** _______________________________________________

---

#### Abnormal Test - Unicode Characters

**File Path:** `C:\Documents\תּוֹרָה.docx` (Hebrew)

**BEFORE Screenshot:**

`[Insert screenshot showing unicode filename]`

---

**AFTER Screenshot:**

`[Insert screenshot showing unicode handling result]`

**Actual Result:** _______________________________________________

---

#### Abnormal Test - Invalid Filename Format

**File Path:** `C:\Documents\file..docx` (double dot before extension)

**BEFORE Screenshot:**

`[Insert screenshot showing invalid filename format]`

---

**AFTER Screenshot:**

`[Insert screenshot showing error message about invalid filename]`

**Actual Result:** _______________________________________________

**Error Message:** _______________________________________________

---

#### Abnormal Test - Read-Only Destination

**File Path:** Normal input file  
**Output Location:** Read-only folder

**Setup:** Set output folder permissions to read-only before testing

**BEFORE Screenshot:**

`[Insert screenshot showing read-only output folder selected]`

---

**AFTER Screenshot:**

`[Insert screenshot showing permission denied error message]`

**Actual Result:** _______________________________________________

**Error Message:** _______________________________________________

---

### Testing Summary

**Variable 1 (File Size) - Summary of Findings:**

_______________________________________________________________________________

_______________________________________________________________________________

_______________________________________________________________________________

**Variable 2 (File Path) - Summary of Findings:**

_______________________________________________________________________________

_______________________________________________________________________________

_______________________________________________________________________________

**Overall Conclusions:**

_______________________________________________________________________________

_______________________________________________________________________________

_______________________________________________________________________________

---

**Test Report Completed By:** ________________________________  
**Date:** ________________________________  
**Version Tested:** FileX v1.0.0
