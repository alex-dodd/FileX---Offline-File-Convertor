# FileX - Project Evaluation Report

**Student:** Alex Dodd  
**Project:** FileX - Offline File Converter  
**Date:** November 2024

---

## 4.2.1 Evaluation of the Programmed Solution

Comparing what I planned in section 1.3 versus what actually works:

### Features Status Summary

| Feature | Status | What Works | What's Missing |
|---------|--------|------------|----------------|
| Batch Folder Conversion | ❌ Not Done | Single file conversions only | Folder processing, subfolder support, MIME detection |
| Multithreaded Processing | ⚠️ Partial | Background threads (UI doesn't freeze) | Thread pool, parallel processing, progress metrics |
| Conflict Resolution | ⚠️ Partial | Overwrite dialog | Auto-rename, error logging |
| Supported Formats | ✅ Mostly | Images (JPG/PNG/WEBP), DOCX↔PDF, XLSX↔CSV, ZIP encryption | SVG support, formatting preservation |
| User Tools | ❌ Not Done | Basic UI only | Drag-and-drop, presets, history export |
| Accessibility | ❌ Not Done | Default styling only | High-contrast mode, keyboard shortcuts |

### Key Issues

**Documents:** DOCX↔PDF conversions lose all formatting (bold, images, tables). Only plain text works.

**Batch Processing:** No folder conversion or multi-file selection. Have to do one file at a time.

**Threading:** Prevents UI freezing but doesn't process multiple files simultaneously.

### Suggestions

- Add `Files.walk()` for recursive folder processing
- Use ExecutorService thread pool for parallel conversions
- Implement auto-rename: check if file exists, append (1), (2), etc.
- Add warning that document conversions only preserve plain text

**Overall:** About 50-60% of planned features working. Core conversions work but missing polish and advanced features.

---

## 4.2.2 Functional Testing

Two sets of tests to verify features from section 1.3 work correctly.

### Test Set 1: Core Features

**Tester:** ________________  **Date:** ________________

| Feature | Test | ☐ Works | ☐ Partial | ☐ Broken | Notes |
|---------|------|---------|-----------|----------|-------|
| File Conversion | Convert DOCX to PDF | ☐ | ☐ | ☐ | |
| File Conversion | Convert PNG to JPG | ☐ | ☐ | ☐ | |
| File Conversion | Convert WEBP to PNG | ☐ | ☐ | ☐ | |
| File Conversion | Convert CSV to XLSX | ☐ | ☐ | ☐ | |
| File Conversion | Convert XLSX to CSV | ☐ | ☐ | ☐ | |
| Overwrite Handling | Existing file shows dialog | ☐ | ☐ | ☐ | |
| Progress Display | Progress bar shows during conversion | ☐ | ☐ | ☐ | |
| Success Feedback | "Conversion completed" message appears | ☐ | ☐ | ☐ | |
| Error Handling | Unsupported format shows error | ☐ | ☐ | ☐ | |
| History Tracking | Conversion appears in history tab | ☐ | ☐ | ☐ | |
| History Search | Search box filters results | ☐ | ☐ | ☐ | |
| Settings | Default output location saves | ☐ | ☐ | ☐ | |
| Settings | File naming options work | ☐ | ☐ | ☐ | |
| ZIP Creation | Folder compresses to ZIP | ☐ | ☐ | ☐ | |
| ZIP Encryption | Password-protected ZIP works | ☐ | ☐ | ☐ | |
| Large Files | 50MB+ file converts without crash | ☐ | ☐ | ☐ | |
| UI Responsiveness | UI doesn't freeze during conversion | ☐ | ☐ | ☐ | |
| Help System | Documentation tab opens | ☐ | ☐ | ☐ | |

---

### Test Set 2: Section 1.3 Features

**Tester:** ________________  **Date:** ________________

| Section 1.3 Feature | Test | ☐ Works | ☐ Partial | ☐ Broken | Notes |
|---------------------|------|---------|-----------|----------|-------|
| Batch Folder Conversion | Select folder with multiple files | ☐ | ☐ | ☐ | |
| Subfolder Processing | Folder with subfolders converts | ☐ | ☐ | ☐ | |
| MIME Detection | Unsupported file types skipped | ☐ | ☐ | ☐ | |
| Multithreading | 4 files process simultaneously | ☐ | ☐ | ☐ | |
| Progress Metrics | Files/sec and ETA display | ☐ | ☐ | ☐ | |
| Auto-Rename | Duplicate creates file(1), file(2) | ☐ | ☐ | ☐ | |
| Error Logging | Failed conversions logged | ☐ | ☐ | ☐ | |
| SVG Support | SVG converts to PNG | ☐ | ☐ | ☐ | |
| Format Preservation | DOCX bold/italic preserved in PDF | ☐ | ☐ | ☐ | |
| Multiple Sheets | XLSX with 3 sheets converts all | ☐ | ☐ | ☐ | |
| Drag-and-Drop | File drag onto UI works | ☐ | ☐ | ☐ | |
| Presets | Save and load conversion preset | ☐ | ☐ | ☐ | |
| History Export | Export history to CSV | ☐ | ☐ | ☐ | |
| High-Contrast Mode | Dark theme toggle works | ☐ | ☐ | ☐ | |
| Keyboard Shortcuts | Ctrl+O, Ctrl+S function | ☐ | ☐ | ☐ | |

---

## 4.2.3 Test Plan and Results for TWO Input Variables

### Variable 1: Image File Size

Test PNG to JPG conversion with different file sizes.

| Test Type | File | Expected Result |
|-----------|------|-----------------|
| Standard | test_standard_2mb.png (2MB) | Converts successfully |
| Extreme | test_extreme_50mb.png (50MB) | Handles large file without crash |
| Abnormal | test_abnormal_empty.png (0KB) | Shows error message |

**Test Files:** Located in `test_files/` folder

**BEFORE Screenshot:** `[Insert screenshot showing file selection]`

**AFTER Screenshot:** `[Insert screenshot showing conversion result]`

**Results:**

- Standard (2MB): _______________________________________________
- Extreme (50MB): _______________________________________________
- Abnormal (0KB): _______________________________________________

---

### Variable 2: File Path Length

Test DOCX to PDF conversion with different path lengths.

| Test Type | Path Description | Expected Result |
|-----------|------------------|-----------------|
| Standard | `C:\Documents\report.docx` | Converts successfully |
| Extreme | 200+ character nested path | Handles or shows clear error |
| Abnormal | Special chars `report!@#$%.docx` | Converts or rejects cleanly |

**Test Files:** Use `test_standard_simple.txt` - save as .docx in test locations

**BEFORE Screenshot:** `[Insert screenshot showing file path]`

**AFTER Screenshot:** `[Insert screenshot showing conversion result]`

**Results:**

- Standard path: _______________________________________________
- Extreme (long path): _______________________________________________
- Abnormal (special chars): _______________________________________________

---

**Test Summary:**

Variable 1 findings: _______________________________________________

Variable 2 findings: _______________________________________________

**Tester:** ________________  **Date:** ________________
