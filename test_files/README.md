# FileX Test Files

This folder contains test files for Section 4.2.3 input variable testing.

## Image Files (Variable 1: File Size Testing)

- **test_standard_2mb.png** - Standard size test image (~2MB)
- **test_extreme_50mb.png** - Large extreme size image (~50MB)  
- **test_abnormal_empty.png** - Empty/corrupted file (0 bytes)

Use these files to test PNG to JPG conversion with different file sizes.

## Document Files (Variable 2: File Path Testing)

- **test_standard_simple.txt** - Standard test document
  - For testing: Rename to `.docx` and place in `C:\Documents\`
  
- **test_extreme_longpath.txt** - For long path testing
  - For testing: Rename to `.docx` and place in deeply nested folder structure
  - Example path: `C:\Users\Alex\Documents\School\Year11\IT\Projects\PAT\FileX\Testing\Subfolder1\Subfolder2\Subfolder3\Subfolder4\Subfolder5\Subfolder6\Subfolder7\Subfolder8\very_long_filename_for_testing_extreme_cases.docx`

- **test_abnormal_special.txt** - For special character testing
  - For testing: Rename to `report!@#$%.docx` and place in `C:\Documents\`
  - Tests how FileX handles special characters in filenames

## How to Use

1. For PNG tests: Use files directly from this folder
2. For DOCX tests: 
   - Copy the `.txt` files 
   - Open in Microsoft Word or LibreOffice Writer
   - Save as `.docx` format
   - Place in the appropriate test locations as described above

## Note

The `.txt` files are provided as plain text. You need to convert them to proper `.docx` format using a word processor before testing. This is because Git repositories typically don't include large binary files, and proper DOCX files are ZIP-compressed XML documents that need a word processor to create correctly.
