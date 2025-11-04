package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    private void loadSettings() {
        File file = new File(SETTINGS_FILE);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                properties.load(fis);
            } catch (IOException e) {
                System.err.println("Error loading settings: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void reloadSettings() {
        loadSettings();
    }

    public void saveSettings() {
        try (FileOutputStream fos = new FileOutputStream(SETTINGS_FILE)) {
            properties.store(fos, "FileX Application Settings");
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getDefaultOutputLocation() {
        return properties.getProperty("defaultOutputLocation");
    }

    public void setDefaultOutputLocation(String path) {
        properties.setProperty("defaultOutputLocation", path);
        saveSettings();
    }

    public boolean getOverwriteExistingFiles() {
        return Boolean.parseBoolean(properties.getProperty("overwriteExistingFiles", "false"));
    }

    public void setOverwriteExistingFiles(boolean overwrite) {
        properties.setProperty("overwriteExistingFiles", String.valueOf(overwrite));
        saveSettings();
    }

    public boolean getShowConfirmationDialogs() {
        return Boolean.parseBoolean(properties.getProperty("showConfirmationDialogs", "true"));
    }

    public void setShowConfirmationDialogs(boolean show) {
        properties.setProperty("showConfirmationDialogs", String.valueOf(show));
        saveSettings();
    }

    public boolean getLogSuccessfulConversions() {
        return Boolean.parseBoolean(properties.getProperty("logSuccessfulConversions", "true"));
    }

    public void setLogSuccessfulConversions(boolean log) {
        properties.setProperty("logSuccessfulConversions", String.valueOf(log));
        saveSettings();
    }

    public boolean getEnableVerboseLogging() {
        return Boolean.parseBoolean(properties.getProperty("enableVerboseLogging", "false"));
    }

    public void setEnableVerboseLogging(boolean enable) {
        properties.setProperty("enableVerboseLogging", String.valueOf(enable));
        saveSettings();
    }

    public String getLogFilePath() {
        return properties.getProperty("logFilePath");
    }

    public void setLogFilePath(String path) {
        properties.setProperty("logFilePath", path);
        saveSettings();
    }

    public String getLastZipOutputLocation() {
        return properties.getProperty("lastZipOutputLocation");
    }

    public void setLastZipOutputLocation(String location) {
        properties.setProperty("lastZipOutputLocation", location);
        saveSettings();
    }

    public String getLastFileLocation() {
        return properties.getProperty("lastFileLocation");
    }

    public void setLastFileLocation(String location) {
        properties.setProperty("lastFileLocation", location);
        saveSettings();
    }

    public String getLastOutputLocation() {
        return properties.getProperty("lastOutputLocation");
    }

    public void setLastOutputLocation(String location) {
        properties.setProperty("lastOutputLocation", location);
        saveSettings();
    }

    public String getFileNamingConvention() {
        return properties.getProperty("fileNamingConvention", "Keep original name");
    }

    public void setFileNamingConvention(String convention) {
        properties.setProperty("fileNamingConvention", convention);
        saveSettings();
    }

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
}