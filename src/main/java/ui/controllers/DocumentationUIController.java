package ui.controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList; //NOTE TO SELF, CAN IMPORT ALL USING '*', DOES NOT WORK FOR SOME REASON (AUTO IMPORTS ANYWAY)
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField; //NOTE TO SELF, CAN IMPORT ALL USING '*', DOES NOT WORK FOR SOME REASON (AUTO IMPORTS ANYWAY)
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

public class DocumentationUIController {
    
    // My FXML Controls
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button clearSearchButton;
    @FXML private Button backButton;

    
    @FXML private TreeView<String> navigationTree;
    @FXML private ScrollPane navigationScrollPane;
    
    @FXML private VBox searchResultsBox;
    @FXML private Label searchResultsHeader;
    @FXML private Label searchResultsCount;
    @FXML private VBox searchResultsContainer;
    
    @FXML private HBox breadcrumbBox;
    @FXML private WebView contentWebView;
    @FXML private ScrollPane contentScrollPane;
    
    // My data management controls
    private Map<String, String> documentSections = new LinkedHashMap<>();
    private Map<String, String> sectionContent = new HashMap<>();
    private Map<String, List<String>> sectionKeywords = new HashMap<>();
    private String currentSection = "welcome";
    private String lastSearchTerm = "";
    private String fullDocumentContent = "";
    private List<String> navigationHistory = new ArrayList<>();
    
    /**
     * This is what represents a search result with context and location information
     */
    private static class SearchResult {
        final String sectionKey;
        final String sectionTitle;
        final String context;
        final String category;
        final int relevanceScore;
        
        SearchResult(String sectionKey, String sectionTitle, String context, String category, int relevanceScore) {
            this.sectionKey = sectionKey;
            this.sectionTitle = sectionTitle;
            this.context = context;
            this.category = category;
            this.relevanceScore = relevanceScore;
        }
    }
    
    @FXML
    private void initialize() {
        setupDocumentSections();
        setupNavigationTree();
        setupSearchComponents();
        setupKeyBindings();
        setupTooltips();
        
        // My own personal keyboard shortcuts will be set up when the scene becomes available
        contentWebView.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setupKeyboardShortcuts(newScene);
            }
        });
        
        loadSection("welcome");
        
        // My Enhanced WebView setup with loading states
        contentWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                // When my content loads successfully
            } else if (newState == Worker.State.FAILED) {
                showErrorContent("Failed to load content");
            }
        });
    }
    
    /**
     * This sets up my keyboard shortcuts and navigation bindings
     */
    private void setupKeyBindings() {
        searchField.setOnKeyPressed(this::handleKeyPress);
    }
    
    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            handleClearSearch();
        } else if (event.getCode() == KeyCode.ENTER) {
            handleSearch();
        }
    }

    private void setupDocumentSections() {
        fullDocumentContent = loadDocumentationText();
        parseDocumentationSections(fullDocumentContent);
        buildSearchKeywords();
    }
    
    private String loadDocumentationText() {
        try {
            InputStream inputStream = getClass().getResourceAsStream("/docs/user_documentation.txt");
            if (inputStream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    String content = reader.lines().collect(Collectors.joining("\n"));
                    // Only return content if it's from the actual documentation file
                    if (content != null && content.trim().length() > 0 && content.contains("Welcome to FileX")) {
                        return content;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading documentation: " + e.getMessage());
        }
        
        String defaultContent = getDefaultDocumentation();
        System.err.println("Warning: Using fallback documentation content");
        return defaultContent;
    }
    
    private void parseDocumentationSections(String fullDoc) {
        String[] lines = fullDoc.split("\n");
        StringBuilder currentContent = new StringBuilder();
        String currentSectionKey = "welcome";
        String currentSectionTitle = "Welcome to FileX";
        
        for (String line : lines) {
            if (line.startsWith("### ")) {
                if (currentContent.length() > 0) {
                    documentSections.put(currentSectionKey, currentSectionTitle);
                    sectionContent.put(currentSectionKey, markdownToHtml(currentContent.toString()));
                }
                
                currentSectionTitle = line.substring(4).trim();
                currentSectionKey = generateSectionKey(currentSectionTitle);
                currentContent = new StringBuilder();
                currentContent.append(line).append("\n");
            } else if (line.startsWith("## ")) {
                if (currentContent.length() > 0) {
                    documentSections.put(currentSectionKey, currentSectionTitle);
                    sectionContent.put(currentSectionKey, markdownToHtml(currentContent.toString()));
                }
                
                currentSectionTitle = line.substring(3).trim();
                currentSectionKey = generateSectionKey(currentSectionTitle);
                currentContent = new StringBuilder();
                currentContent.append(line).append("\n");
            } else if (line.startsWith("# Welcome to FileX")) {
                currentSectionTitle = "Welcome to FileX";
                currentSectionKey = "welcome";
                currentContent = new StringBuilder();
                currentContent.append(line).append("\n");
            } else if (!line.startsWith("# ") || line.startsWith("# Welcome")) {
                currentContent.append(line).append("\n");
            }
        }
        
        if (currentContent.length() > 0) {
            documentSections.put(currentSectionKey, currentSectionTitle);
            sectionContent.put(currentSectionKey, markdownToHtml(currentContent.toString()));
        }
    }
    
    private String generateSectionKey(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "";
        }
        
        switch (title) {
            case "Welcome":
                return "welcome";
            case "Quick Start":
                return "quick_start";
            case "Key Features":
                return "key_features";
            case "Document Conversion":
                return "document_conversion";
            case "Image Conversion":
                return "image_conversion";
            case "Archive Management":
                return "archive_management";
            case "Settings and Configuration":
                return "settings_and_configuration";
            case "File History":
                return "file_history";
            default:
                return title.toLowerCase()
                        .replaceAll("[^a-zA-Z0-9\\s]", "")
                        .replace(" ", "_");
        }
    }
    
    private String markdownToHtml(String markdown) {
        String html = markdown;
        
        html = html.replaceAll("(?m)^# (.+)$", "<h1 style='color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; margin-top: 20px;'>$1</h1>");
        html = html.replaceAll("(?m)^## (.+)$", "<h2 style='color: #34495e; border-bottom: 1px solid #bdc3c7; padding-bottom: 5px; margin-top: 15px;'>$1</h2>");
        html = html.replaceAll("(?m)^### (.+)$", "<h3 style='color: #2c3e50; margin-top: 12px;'>$1</h3>");
        html = html.replaceAll("(?m)^#### (.+)$", "<h4 style='color: #2c3e50; margin-top: 10px;'>$1</h4>");
        
        html = html.replaceAll("\\*\\*(.+?)\\*\\*", "<strong style='color: #2c3e50;'>$1</strong>");
        html = html.replaceAll("\\*(.+?)\\*", "<em style='color: #34495e;'>$1</em>");
        html = html.replaceAll("`([^`]+)`", "<code style='background: #f8f9fa; padding: 2px 4px; border-radius: 3px; font-family: monospace;'>$1</code>");
        
        html = html.replaceAll("```([\\s\\S]*?)```", "<pre style='background: #f8f9fa; border: 1px solid #e9ecef; border-radius: 5px; padding: 15px; margin: 10px 0; overflow-x: auto;'><code style='font-family: \"Courier New\", monospace; color: #212529;'>$1</code></pre>");
        
        html = html.replaceAll("(?m)^> (.+)$", "<blockquote style='border-left: 4px solid #3498db; margin: 10px 0; padding: 10px 15px; background: #f8f9fa; font-style: italic; color: #555;'>$1</blockquote>");
        
        html = html.replaceAll("(?m)^[-*] (.+)$", "<li style='margin: 5px 0; padding-left: 5px;'>$1</li>");
        html = html.replaceAll("(?m)^\\d+\\. (.+)$", "<li style='margin: 5px 0; padding-left: 5px;'>$1</li>");
        
        html = html.replaceAll("(<li[^>]*>.*?</li>)(?=\\s*<li)", "<ul style='margin: 10px 0; padding-left: 20px; list-style-type: disc;'>$1");
        html = html.replaceAll("(<li[^>]*>.*?</li>)(?!\\s*<li)", "$1</ul>");
        
        html = html.replaceAll("\\$\\$([^$]+)\\$\\$", "<div style='text-align: center; font-style: italic; margin: 10px 0; padding: 10px; background: #f0f8ff; border-radius: 5px;'>$1</div>");
        html = html.replaceAll("\\$([^$]+)\\$", "<span style='font-style: italic; color: #8e44ad;'>$1</span>");
        
        html = html.replaceAll("\\[([^\\]]+)\\]\\(([^\\)]+)\\)", "<a href='$2' style='color: #3498db; text-decoration: none;'>$1</a>");
        
        html = html.replaceAll("(?m)^---+$", "<hr style='border: none; border-top: 2px solid #bdc3c7; margin: 20px 0;'>");
        
        html = html.replaceAll("\\n\\s*\\n", "</p><p style='margin: 10px 0; line-height: 1.6; color: #2c3e50;'>");
        html = "<p style='margin: 10px 0; line-height: 1.6; color: #2c3e50;'>" + html + "</p>";
        html = html.replaceAll("<p[^>]*>\\s*</p>", "");
        html = html.replaceAll("\\n", "<br>");
        
        if (!lastSearchTerm.isEmpty()) {
            html = highlightSearchTerm(html, lastSearchTerm);
        }
        
        return wrapInSimpleHtmlDocument(html);
    }
    
    private String highlightSearchTerm(String html, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return html;
        }
        
        String[] terms = searchTerm.toLowerCase().split("\\s+");
        for (String term : terms) {
            if (term.length() > 2) {
                Pattern pattern = Pattern.compile("(?i)\\b" + Pattern.quote(term) + "\\b");
                html = pattern.matcher(html).replaceAll("<mark>$0</mark>");
            }
        }
        
        return html;
    }
    

    private String wrapInSimpleHtmlDocument(String content) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
            </head>
            <body>
                """ + content + """
            </body>
            </html>
            """;
    }
    
    private void setupNavigationTree() {
        TreeItem<String> root = new TreeItem<>("Documentation");
        root.setExpanded(true);
        
        TreeItem<String> gettingStarted = new TreeItem<>("Getting Started");
        gettingStarted.setExpanded(false);
        gettingStarted.getChildren().add(new TreeItem<>("Welcome"));
        gettingStarted.getChildren().add(new TreeItem<>("Quick Start"));
        gettingStarted.getChildren().add(new TreeItem<>("Key Features"));
        
        TreeItem<String> conversion = new TreeItem<>("File Conversion");
        conversion.setExpanded(false);
        conversion.getChildren().add(new TreeItem<>("Document Conversion"));
        conversion.getChildren().add(new TreeItem<>("Image Conversion"));
        conversion.getChildren().add(new TreeItem<>("Archive Management"));
        
        TreeItem<String> configuration = new TreeItem<>("Configuration");
        configuration.setExpanded(false);
        configuration.getChildren().add(new TreeItem<>("Settings and Configuration"));
        configuration.getChildren().add(new TreeItem<>("File History"));
        
        root.getChildren().addAll(gettingStarted, conversion, configuration);
        navigationTree.setRoot(root);
        navigationTree.setShowRoot(false);
        
        navigationTree.setOnMouseClicked(event -> {
            TreeItem<String> selectedItem = navigationTree.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.isLeaf()) {
                String sectionKey = generateSectionKey(selectedItem.getValue());
                loadSection(sectionKey);
            }
        });
    }
    
    private void setupSearchComponents() {
        searchResultsBox.setVisible(false);
        searchResultsBox.setManaged(false);
    }
    
    private void buildSearchKeywords() {
        sectionKeywords.put("welcome", Arrays.asList("welcome", "introduction", "overview", "start", "beginning"));
        sectionKeywords.put("getting_started", Arrays.asList("start", "begin", "installation", "setup", "first", "quick"));
        sectionKeywords.put("file_conversion", Arrays.asList("convert", "file", "format", "transform", "change", "export"));
        sectionKeywords.put("batch_processing", Arrays.asList("batch", "multiple", "bulk", "mass", "many", "group"));
        sectionKeywords.put("security_features", Arrays.asList("security", "safe", "protection", "encryption", "secure"));
        sectionKeywords.put("settings", Arrays.asList("settings", "preferences", "configuration", "options", "customize"));
        sectionKeywords.put("troubleshooting_and_support", Arrays.asList("help", "problem", "issue", "error", "support", "fix", "trouble"));
    }
    
    private void loadSection(String sectionKey) {
        currentSection = sectionKey;
        navigationHistory.add(sectionKey);
        
        String content = sectionContent.get(sectionKey);
        if (content == null) {
            content = sectionContent.get("welcome");
            if (content == null) {
                content = wrapInSimpleHtmlDocument("<h1>Section Not Found</h1><p>The requested section '" + sectionKey + "' could not be loaded.</p>");
            }
        }
        
        contentWebView.getEngine().loadContent(content);
        updateBreadcrumbs(sectionKey);
        updateBackButton();
    }
    
    private void updateBreadcrumbs(String sectionKey) {
        breadcrumbBox.getChildren().clear();
        
        String sectionTitle = documentSections.getOrDefault(sectionKey, "Documentation");
        Label breadcrumb = new Label("Documentation > " + sectionTitle);
        breadcrumb.getStyleClass().add("breadcrumb");
        
        breadcrumb.setTooltip(new Tooltip("Current location: " + sectionTitle + "\nYou are viewing this section"));
        
        breadcrumbBox.getChildren().add(breadcrumb);
    }
    
    private void updateBackButton() {
        backButton.setDisable(navigationHistory.size() <= 1);
    }
    
    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        if (!searchTerm.isEmpty()) {
            performSearch(searchTerm);
        }
    }
    
    private void performSearch(String searchTerm) {
        lastSearchTerm = searchTerm;        
        List<SearchResult> results = findSearchResults(searchTerm);
        displaySearchResults(results, searchTerm);
        
        searchResultsBox.setVisible(true);
        searchResultsBox.setManaged(true);
    }
    
    private List<SearchResult> findSearchResults(String searchTerm) {
        List<SearchResult> results = new ArrayList<>();
        String[] searchWords = searchTerm.toLowerCase().split("\\s+");
        
        for (Map.Entry<String, String> section : documentSections.entrySet()) {
            String sectionKey = section.getKey();
            String sectionTitle = section.getValue();
            
            String rawContent = getRawSectionContent(sectionKey);
            
            if (rawContent == null || rawContent.trim().isEmpty() || 
                sectionKey.equals("default") || rawContent.contains("enterprise-grade")) {
                continue;
            }
            
            String searchableContent = cleanContentForSearch(rawContent);
            
            int relevanceScore = calculateRelevanceScore(searchableContent, sectionTitle, searchWords, sectionKey);
            if (relevanceScore > 0) {
                String context = extractCleanContext(searchableContent, searchWords);
                String category = getCategoryForSection(sectionKey);
                
                results.add(new SearchResult(sectionKey, sectionTitle, context, category, relevanceScore));
            }
        }
        
        results.sort((a, b) -> Integer.compare(b.relevanceScore, a.relevanceScore));
        return results;
    }
    
    private String getRawSectionContent(String sectionKey) {
        String[] lines = fullDocumentContent.split("\n");
        StringBuilder content = new StringBuilder();
        boolean inSection = false;
        String targetSectionTitle = documentSections.get(sectionKey);
        
        if (targetSectionTitle == null) {
            return "";
        }
        
        for (String line : lines) {
            if (line.startsWith("### ") && line.substring(4).trim().equals(targetSectionTitle)) {
                inSection = true;
                content.append(line).append("\n");
                continue;
            }
            else if (line.startsWith("## ") && line.substring(3).trim().equals(targetSectionTitle)) {
                inSection = true;
                content.append(line).append("\n");
                continue;
            }
            else if ((line.startsWith("### ") || line.startsWith("## ")) && inSection) {
                break;
            }
            else if (line.startsWith("# Welcome to FileX") && sectionKey.equals("welcome")) {
                inSection = true;
                content.append(line).append("\n");
                continue;
            }
            else if (line.startsWith("# ") && inSection && sectionKey.equals("welcome")) {
                break;
            }
            
            if (inSection) {
                content.append(line).append("\n");
            }
        }
        
        return content.toString();
    }
    
    private String cleanContentForSearch(String content) {
        content = content.replaceAll("(?m)^#{1,6}\\s+.*$", "");
        content = content.replaceAll("```[\\s\\S]*?```", "");
        content = content.replaceAll("`[^`]+`", "");
        content = content.replaceAll("<[^>]*>", "");
        content = content.replaceAll("https?://\\S+", "");
        content = content.replaceAll("[{}\\[\\]()<>]", " ");
        content = content.replaceAll("\\s+", " ").trim();
        
        return content;
    }
    
    private String extractCleanContext(String content, String[] searchWords) {
        for (String word : searchWords) {
            int index = content.toLowerCase().indexOf(word.toLowerCase());
            if (index != -1) {
                int start = Math.max(0, index - 50);
                int end = Math.min(content.length(), index + word.length() + 50);
                String context = content.substring(start, end).trim();
                
                if (start > 0) context = "..." + context;
                if (end < content.length()) context = context + "...";
                
                return context;
            }
        }
        
        return content.length() > 100 ? content.substring(0, 100) + "..." : content;
    }
    
    private int calculateRelevanceScore(String content, String title, String[] searchWords, String sectionKey) {
        int score = 0;
        String lowerTitle = title.toLowerCase();
        
        for (String word : searchWords) {
            if (word.length() < 3) continue; 
            
            if (lowerTitle.contains(word)) {
                score += 20;
            }
            
            Pattern wordPattern = Pattern.compile("\\b" + Pattern.quote(word) + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = wordPattern.matcher(content);
            int contentMatches = 0;
            while (matcher.find()) {
                contentMatches++;
            }
            score += contentMatches * 5;
            
            List<String> keywords = sectionKeywords.get(sectionKey);
            if (keywords != null && keywords.contains(word)) {
                score += 15;
            }
        }
        
        return score;
    }
    
    private String getCategoryForSection(String sectionKey) {
        if (sectionKey.contains("getting") || sectionKey.contains("welcome") || sectionKey.contains("installation")) {
            return "Getting Started";
        } else if (sectionKey.contains("conversion") || sectionKey.contains("batch") || sectionKey.contains("security")) {
            return "Features";
        } else if (sectionKey.contains("settings") || sectionKey.contains("preferences")) {
            return "Configuration";
        } else if (sectionKey.contains("troubleshooting") || sectionKey.contains("support") || sectionKey.contains("faq")) {
            return "Support";
        }
        return "General";
    }
    
    private void displaySearchResults(List<SearchResult> results, String searchTerm) {
        searchResultsContainer.getChildren().clear();
        
        searchResultsHeader.setText("Search Results for: \"" + searchTerm + "\"");
        searchResultsCount.setText(results.size() + " result(s) found");
        
        if (results.isEmpty()) {
            Label noResults = new Label("No results found. Try different keywords or check spelling.");
            noResults.getStyleClass().add("no-results");
            
            noResults.setTooltip(new Tooltip("No matching content found.\nTry:\n• Different keywords\n• Simpler terms\n• Check spelling"));
            
            searchResultsContainer.getChildren().add(noResults);
            return;
        }
        
        Map<String, List<SearchResult>> groupedResults = results.stream()
            .collect(Collectors.groupingBy(r -> r.category, LinkedHashMap::new, Collectors.toList()));
        
        for (Map.Entry<String, List<SearchResult>> group : groupedResults.entrySet()) {
            Label categoryLabel = new Label(group.getKey());
            categoryLabel.getStyleClass().add("search-category");        
            categoryLabel.setTooltip(new Tooltip("Results from " + group.getKey() + " section\n" + group.getValue().size() + " result(s) in this category"));
            searchResultsContainer.getChildren().add(categoryLabel);
            
            for (SearchResult result : group.getValue()) {
                VBox resultBox = createSearchResultItem(result);
                searchResultsContainer.getChildren().add(resultBox);
            }
        }
    }
    
    private VBox createSearchResultItem(SearchResult result) {
        VBox resultBox = new VBox(5);
        resultBox.getStyleClass().add("search-result-item");
        
        Button titleButton = new Button(result.sectionTitle);
        titleButton.getStyleClass().add("search-result-title");
        titleButton.setOnAction(e -> {
            loadSection(result.sectionKey);
            hideSearchResults();
        });
        
        titleButton.setTooltip(new Tooltip("Click to view: " + result.sectionTitle));
        
        Label contextLabel = new Label(result.context);
        contextLabel.getStyleClass().add("search-result-context");
        contextLabel.setWrapText(true);
        
        contextLabel.setTooltip(new Tooltip("Preview of content containing your search terms"));
        
        Tooltip.install(resultBox, new Tooltip("Search result - click title to navigate to section"));
        
        resultBox.getChildren().addAll(titleButton, contextLabel);
        return resultBox;
    }
    
    @FXML
    private void handleClearSearch() {
        searchField.clear();
        lastSearchTerm = "";
        hideSearchResults();
        
        loadSection(currentSection);
    }
    
    private void hideSearchResults() {
        searchResultsBox.setVisible(false);
        searchResultsBox.setManaged(false);
    }
    
    @FXML
    private void handleBack() {
        if (navigationHistory.size() > 1) {
            navigationHistory.remove(navigationHistory.size() - 1);
            String previousSection = navigationHistory.get(navigationHistory.size() - 1);
            loadSection(previousSection);
        }
    }

    @FXML
    private void setupKeyboardShortcuts(javafx.scene.Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.isControlDown()) {
                switch (event.getCode()) {
                    case F:
                        searchField.requestFocus();
                        event.consume();
                        break;
                    case H:
                        loadSection("welcome");
                        event.consume();
                        break;
                    case B:
                        if (navigationHistory.size() > 1) {
                            handleBack();
                        }
                        event.consume();
                        break;
                    default:
                        break;
                }
            } else if (event.getCode() == KeyCode.ESCAPE) {
                if (searchResultsBox.isVisible()) {
                    hideSearchResults();
                } else {
                    searchField.clear();
                }
                event.consume();
            }
        });
    }

    private void setupTooltips() {
        searchField.setTooltip(new Tooltip("Search documentation (Ctrl+F)\nTry: 'file conversion', 'settings', 'troubleshooting'"));
        searchButton.setTooltip(new Tooltip("Search through documentation"));
        clearSearchButton.setTooltip(new Tooltip("Clear search results"));
        
        backButton.setTooltip(new Tooltip("Go back to previous section (Ctrl+B)"));
        navigationTree.setTooltip(new Tooltip("Click categories to expand/collapse\nClick items to view content"));
        
        searchResultsHeader.setTooltip(new Tooltip("Search results are displayed here"));
        searchResultsCount.setTooltip(new Tooltip("Number of matching search results"));
        
        Tooltip.install(searchResultsContainer, new Tooltip("Click on any result to view that section"));
        Tooltip.install(contentWebView, new Tooltip("Documentation content display\nScroll to read more"));
        Tooltip.install(contentScrollPane, new Tooltip("Use mouse wheel or arrow keys to scroll"));
        Tooltip.install(navigationScrollPane, new Tooltip("Scroll to see more navigation options"));
        Tooltip.install(breadcrumbBox, new Tooltip("Current location in documentation\nClick any breadcrumb to navigate"));
    }

    private void showErrorContent(String errorMessage) {
        String errorHtml = wrapInSimpleHtmlDocument(
            "<h1>Error Loading Documentation</h1>" +
            "<p>" + errorMessage + "</p>" +
            "<p>Please check your installation or contact support.</p>"
        );
        contentWebView.getEngine().loadContent(errorHtml);
    }
    
    private String getDefaultDocumentation() {
        return """
            # Welcome to FileX
            
            FileX is a powerful and secure file conversion utility designed for modern workflows.
            
            ## Getting Started
            
            Welcome to FileX! This application provides comprehensive file conversion capabilities
            with enterprise-grade security and batch processing features.
            
            ### Key Features
            
            - **File Conversion**: Convert between multiple file formats
            - **Security**: Secure processing (on-device processing)
            
            ## File Conversion
            
            FileX supports conversion between various file formats including documents,
            images, and archives. Simply drag files into the application or use the
            file picker to select your files.
            
            ## Settings
            
            Customize FileX behavior through the Settings panel. You can configure
            default output formats, security preferences, and processing options.
            
            ## Troubleshooting and Support
            
            If you encounter issues:
            
            1. Verify file permissions
            2. Ensure sufficient disk space

            For additional help, please visit my documentation.
            """;
    }
}
