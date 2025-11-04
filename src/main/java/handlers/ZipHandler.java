package handlers;

import java.io.File;
import java.io.IOException;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod; //NOTE TO SELF, CAN IMPORT ALL USING '*', DOES NOT WORK FOR SOME REASON

/**
 * This handles the ZIP archive creation, including the password protection.
 */
public class ZipHandler {

    /**
     * This creates a ZIP archive from a source folder, with optional password protection.
     * @param sourceFolder The folder to be archived.
     * @param targetZipFile The output ZIP file.
     * @param password Optional password for encryption. Can be null or empty for no encryption.
     * @throws IOException If an I/O error occurs during zipping.
     */
    public void zipFolder(File sourceFolder, File targetZipFile, String password) throws IOException {
        ZipFile zipFile = new ZipFile(targetZipFile);
        ZipParameters parameters = new ZipParameters();

        // Thid sets encryption if a password is provided
        if (password != null && !password.trim().isEmpty()) {
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(EncryptionMethod.AES);
            zipFile.setPassword(password.toCharArray());
        }

        // THis will add the folder to the zip file
        zipFile.addFolder(sourceFolder, parameters);
    }

    /**
     * This creates a ZIP archive from a source folder with optional password protection.
     * This method is the main interface for ZIP creation in my application.
     * @param sourceFolder The folder to compress into ZIP.
     * @param targetZipFile The output ZIP file.
     * @param password Optional password for encryption. Can be null for no encryption.
     * @return true if ZIP creation was successful, false otherwise.
     */
    public boolean createZip(File sourceFolder, File targetZipFile, String password) {
        try {
            zipFolder(sourceFolder, targetZipFile, password);
            return true;
        } catch (IOException e) {
            System.err.println("ZIP creation failed: " + e.getMessage());
            return false;
        }
    }
}
