package com.quizapp.view.quiz;

import com.quizapp.controller.QuizController;
import com.quizapp.model.Category;
import com.quizapp.model.Question;
import com.quizapp.model.QuizResult;
import com.quizapp.util.UIConstants;
import com.quizapp.util.AccessibilityManager;

import javax.swing.*;
import java.awt.*;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Arrays;
import java.util.List;

/**
 * JPanel for displaying and taking a quiz.
 */
public class QuizPanel extends JPanel {
    private Timer timer;
    private JPanel optionsPanel;
    private JPanel controlsPanel;
    private JPanel questionPanel;
    private JLabel questionLabel;
    private JLabel timerLabel;
    private JLabel questionCountLabel;
    private JButton nextButton;
    private JButton prevButton;
    private JButton submitButton;
    private JButton helpButton;
    private final QuizController quizController;
    private Runnable onQuizEnd;
    private ButtonGroup optionsGroup;
    private JRadioButton[] optionButtons;
    private JProgressBar progressBar;
    private JPanel breadcrumbPanel;
    
    private Category selectedCategory;
    private List<Question> questions;
    private int currentQuestionIndex;
    private int[] userAnswers;
    private int correctAnswers;
    private long quizStartTime;
    private long quizEndTime;
    private int totalQuizTimeSeconds;
    private int remainingTimeSeconds;

    public QuizPanel(int userId) {
        this.quizController = new QuizController(userId);
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(
            UIConstants.STANDARD_PADDING, 
            UIConstants.STANDARD_PADDING, 
            UIConstants.STANDARD_PADDING, 
            UIConstants.STANDARD_PADDING
        ));
        setBackground(UIConstants.BACKGROUND_COLOR);
        
        // Initialize components
        initComponents();
        
        // Setup keyboard navigation
        setupKeyboardNavigation();
        
        // Hide quiz elements initially
        questionPanel.setVisible(false);
        controlsPanel.setVisible(false);
    }
    
    private void initComponents() {
        // Top panel with breadcrumb, timer, and question counter
        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        topPanel.setOpaque(false);
        
        // Initialize breadcrumb
        breadcrumbPanel = UIConstants.createBreadcrumbPanel(new String[]{"Quizzes"});
        topPanel.add(breadcrumbPanel, BorderLayout.NORTH);
        
        // Create info panel for timer and question counter
        JPanel infoPanel = new JPanel(new BorderLayout(10, 0));
        infoPanel.setOpaque(false);
        
        timerLabel = new JLabel("Time remaining: 00:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font(timerLabel.getFont().getName(), Font.BOLD, 16));
        timerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        questionCountLabel = new JLabel("Question 0/0", SwingConstants.RIGHT);
        questionCountLabel.setFont(new Font(questionCountLabel.getFont().getName(), Font.PLAIN, 14));
        
        infoPanel.add(timerLabel, BorderLayout.CENTER);
        infoPanel.add(questionCountLabel, BorderLayout.EAST);

        // Create modern timeline progress indicator
        JPanel progressPanel = new JPanel(new BorderLayout(0, 8));
        progressPanel.setOpaque(false);
        
        // Add basic progress bar (will be replaced by timeline)
        progressBar = UIConstants.createProgressBar(0, 100, 0);
        progressBar.setPreferredSize(new Dimension(getWidth(), 8));
        progressBar.setStringPainted(false);
        
        // Create timeline panel
        JPanel timelinePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        timelinePanel.setOpaque(false);
        
        // Timeline will be populated when questions are loaded
        
        progressPanel.add(infoPanel, BorderLayout.NORTH);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressPanel.add(timelinePanel, BorderLayout.SOUTH);
        
        topPanel.add(progressPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);
        
        // Question panel
        questionPanel = new JPanel(new BorderLayout(0, 15));
        questionPanel.setOpaque(false);
        
        questionLabel = UIConstants.createSubHeaderLabel("");
        
        optionsPanel = new JPanel(new GridLayout(0, 1, 5, 10));
        optionsPanel.setOpaque(false);
        optionsGroup = new ButtonGroup();
        optionButtons = new JRadioButton[4]; // Max 4 options
        
        for (int i = 0; i < optionButtons.length; i++) {
            optionButtons[i] = new JRadioButton();
            optionButtons[i].setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            optionButtons[i].setForeground(UIConstants.TEXT_COLOR);
            optionButtons[i].setBackground(UIConstants.BACKGROUND_COLOR);
            optionsGroup.add(optionButtons[i]);
            optionsPanel.add(optionButtons[i]);
            
            final int optionIndex = i;
            
            // Add keyboard shortcut (1-4 keys)
            int keyCode = KeyEvent.VK_1 + i;
            optionButtons[i].setMnemonic(keyCode);
            UIConstants.addTooltip(optionButtons[i], "Press " + (i + 1) + " to select this option");
            
            optionButtons[i].addActionListener(e -> {
                if (userAnswers != null && currentQuestionIndex < userAnswers.length) {
                    userAnswers[currentQuestionIndex] = optionIndex;
                    updateNavButtons();
                }
            });
        }
        
        questionPanel.add(questionLabel, BorderLayout.NORTH);
        questionPanel.add(optionsPanel, BorderLayout.CENTER);
        add(questionPanel, BorderLayout.CENTER);

        // Controls panel with navigation buttons - consistent layout
        controlsPanel = new JPanel(new BorderLayout(0, 10));
        controlsPanel.setOpaque(false);
        
        // Navigation buttons always at the bottom with consistent layout
        JPanel navButtonsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        navButtonsPanel.setOpaque(false);
        
        JPanel leftNavPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftNavPanel.setOpaque(false);
        prevButton = UIConstants.createSecondaryButton("← Previous");
        UIConstants.addTooltip(prevButton, "Go to the previous question");
        prevButton.addActionListener(e -> navigateToPreviousQuestion());
        leftNavPanel.add(prevButton);
        
        JPanel centerNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerNavPanel.setOpaque(false);
        submitButton = UIConstants.createPrimaryButton("Submit Quiz");
        UIConstants.addTooltip(submitButton, "Submit your answers and end the quiz");
        submitButton.addActionListener(e -> confirmSubmitQuiz());
        centerNavPanel.add(submitButton);
        
        JPanel rightNavPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightNavPanel.setOpaque(false);
        nextButton = UIConstants.createSecondaryButton("Next →");
        UIConstants.addTooltip(nextButton, "Go to the next question");
        nextButton.addActionListener(e -> navigateToNextQuestion());
        rightNavPanel.add(nextButton);
        
        navButtonsPanel.add(leftNavPanel);
        navButtonsPanel.add(centerNavPanel);
        navButtonsPanel.add(rightNavPanel);
        
        // Add help button for keyboard shortcuts
        helpButton = new JButton("?");
        helpButton.setFont(new Font(helpButton.getFont().getName(), Font.BOLD, 14));
        helpButton.setForeground(Color.WHITE);
        helpButton.setBackground(UIConstants.SECONDARY_COLOR);
        helpButton.setPreferredSize(new Dimension(30, 30));
        helpButton.setFocusPainted(false);
        helpButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        helpButton.setToolTipText("Show keyboard shortcuts");
        helpButton.addActionListener(e -> showKeyboardShortcutsHelp());

        JPanel helpPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        helpPanel.setOpaque(false);
        helpPanel.add(helpButton);
        
        // Add flag button to controls panel
        JButton flagButton = new JButton("🚩 Flag Question");
        flagButton.setFont(new Font(flagButton.getFont().getName(), Font.PLAIN, 12));
        flagButton.setForeground(UIConstants.WARNING_COLOR);
        flagButton.setBackground(UIConstants.BACKGROUND_COLOR);
        flagButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        flagButton.setFocusPainted(false);
        flagButton.addActionListener(e -> flagCurrentQuestion());
        
        JPanel flagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        flagPanel.setOpaque(false);
        flagPanel.add(flagButton);
        
        controlsPanel.add(navButtonsPanel, BorderLayout.SOUTH);
        controlsPanel.add(helpPanel, BorderLayout.NORTH);
        controlsPanel.add(flagPanel, BorderLayout.NORTH);
        add(controlsPanel, BorderLayout.SOUTH);
    }

    private void setupKeyboardNavigation() {
        // Setup global keyboard shortcuts
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            // Only process key pressed events and ignore repeated keys
            if (e.getID() != KeyEvent.KEY_PRESSED || e.isConsumed()) {
                return false;
            }
            
            // Check if quiz is active
            if (!questionPanel.isVisible()) {
                return false;
            }
            
            // Handle navigation keys
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_P:
                    if (prevButton.isEnabled()) {
                        navigateToPreviousQuestion();
                        e.consume();
                        return true;
                    }
                    break;
                    
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_N:
                    if (nextButton.isEnabled()) {
                        navigateToNextQuestion();
                        e.consume();
                        return true;
                    }
                    break;
                    
                case KeyEvent.VK_ENTER:
                    if (submitButton.isEnabled() && !nextButton.isEnabled()) {
                        confirmSubmitQuiz();
                        e.consume();
                        return true;
                    }
                    break;
                    
                case KeyEvent.VK_1:
                case KeyEvent.VK_2:
                case KeyEvent.VK_3:
                case KeyEvent.VK_4:
                    int idx = e.getKeyCode() - KeyEvent.VK_1;
                    if (idx >= 0 && idx < optionButtons.length && optionButtons[idx].isVisible()) {
                        optionButtons[idx].setSelected(true);
                        if (userAnswers != null && currentQuestionIndex < userAnswers.length) {
                            userAnswers[currentQuestionIndex] = idx;
                            updateNavButtons();
                        }
                        e.consume();
                        return true;
                    }
                    break;
                    
                case KeyEvent.VK_SLASH:
                    // Show help when ? is pressed (SHIFT+/ on most keyboards)
                    if (e.isShiftDown()) {
                        showKeyboardShortcutsHelp();
                        e.consume();
                        return true;
                    }
                    break;
            }
            
            return false;
        });
    }
    
    public void startQuiz(Category selectedCategory, int questionCount, int quizTimeSeconds, Runnable onQuizComplete) {
        this.selectedCategory = selectedCategory;
        this.onQuizEnd = onQuizComplete;
        
        // Update breadcrumb
        String[] path = {"Quizzes", selectedCategory.getName()};
        JPanel newBreadcrumbPanel = UIConstants.createBreadcrumbPanel(path);
        
        // Get the top panel and replace the breadcrumb
        JPanel topPanel = (JPanel) getComponent(0);
        JPanel headerPanel = (JPanel) topPanel.getComponent(0);
        headerPanel.remove(0); // Remove old breadcrumb
        headerPanel.add(newBreadcrumbPanel, BorderLayout.NORTH);
        headerPanel.revalidate();
        headerPanel.repaint();
        
        // Get timer settings from the category if available
        if (questionCount <= 0) {
            questionCount = 5; // Default count
        }
        if (quizTimeSeconds <= 0) {
            quizTimeSeconds = selectedCategory.getTotalTime() > 0 ? 
                selectedCategory.getTotalTime() : 120; // Default to 2 minutes
        }
        
        // Load questions for the selected category
        List<Question> loadedQuestions = quizController.getQuestionsForQuiz(selectedCategory.getCategoryId(), questionCount);
        
        if (loadedQuestions.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No questions available for this category.",
                "Cannot Start Quiz",
                JOptionPane.WARNING_MESSAGE);
            if (onQuizEnd != null) {
                onQuizEnd.run();
            }
            return;
        }

        // Reset quiz state
        this.questions = loadedQuestions;
        this.currentQuestionIndex = 0;
        this.correctAnswers = 0;
        this.userAnswers = new int[questions.size()];
        Arrays.fill(userAnswers, -1); // -1 indicates no answer selected
        this.quizStartTime = System.currentTimeMillis();
        this.quizEndTime = 0;

        // Reset progress bar
        progressBar.setMinimum(0);
        progressBar.setMaximum(questions.size());
        progressBar.setValue(1); // Start at first question
        progressBar.setString("Question 1 of " + questions.size());
        
        // Create timeline indicators
        createTimelineIndicators();
            
        // Start timer
        this.totalQuizTimeSeconds = quizTimeSeconds;
        this.remainingTimeSeconds = quizTimeSeconds;
        startTimer();

        // Show quiz elements
        questionPanel.setVisible(true);
        controlsPanel.setVisible(true);
        
        // Show first question
        displayCurrentQuestion();
        updateNavButtons();
    }
    
    private void startTimer() {
        if (timer != null) {
            timer.stop();
        }
        
        timer = new Timer(1000, e -> {
            remainingTimeSeconds--;
            updateTimerDisplay();
            
            if (remainingTimeSeconds <= 0) {
                timer.stop();
                JOptionPane.showMessageDialog(this,
                    "Time's up! Your quiz will be submitted automatically.",
                    "Time Expired",
                    JOptionPane.WARNING_MESSAGE);
                submitQuiz();
            }
        });
        
        timer.start();
        updateTimerDisplay();
    }
    
    private void updateTimerDisplay() {
        int minutes = remainingTimeSeconds / 60;
        int seconds = remainingTimeSeconds % 60;
        timerLabel.setText(String.format("Time remaining: %02d:%02d", minutes, seconds));
        
        // Make timer red when less than 30 seconds remain
        if (remainingTimeSeconds < 30) {
            timerLabel.setForeground(UIConstants.ERROR_COLOR);
        } else {
            timerLabel.setForeground(UIConstants.TEXT_COLOR);
        }
    }
    
    private void displayCurrentQuestion() {
        if (questions == null || questions.isEmpty() || currentQuestionIndex < 0 || currentQuestionIndex >= questions.size()) {
            return;
        }

        Question question = questions.get(currentQuestionIndex);
        questionLabel.setText((currentQuestionIndex + 1) + ". " + question.getQuestionText());
        questionCountLabel.setText("Question " + (currentQuestionIndex + 1) + "/" + questions.size());
        
        // Update progress bar
        progressBar.setValue(currentQuestionIndex + 1);
        progressBar.setString("Question " + (currentQuestionIndex + 1) + " of " + questions.size());
        
        // Update timeline indicators
        updateTimelineIndicators();
        
        // Clear previous options
        optionsGroup.clearSelection();
        
        // Hide all option buttons first
        for (JRadioButton button : optionButtons) {
            button.setVisible(false);
        }
        
        // Set options
        List<String> options = question.getOptions();
        for (int i = 0; i < options.size() && i < optionButtons.length; i++) {
            optionButtons[i].setText(options.get(i));
            optionButtons[i].setVisible(true);
            
            // Select button if user already answered this question
            if (userAnswers[currentQuestionIndex] == i) {
                optionButtons[i].setSelected(true);
            }
        }
        
        // Enable focus mode for accessibility if configured
        if (AccessibilityManager.isHighFocusMode()) {
            applyFocusMode();
        }
        
        optionsPanel.revalidate();
        optionsPanel.repaint();
    }
    
    private void navigateToPreviousQuestion() {
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            displayCurrentQuestion();
            updateNavButtons();
        }
    }
    
    private void navigateToNextQuestion() {
        if (currentQuestionIndex < questions.size() - 1) {
            currentQuestionIndex++;
            displayCurrentQuestion();
            updateNavButtons();
        }
    }

    private void updateNavButtons() {
        prevButton.setEnabled(currentQuestionIndex > 0);
        nextButton.setEnabled(currentQuestionIndex < questions.size() - 1);
        
        // Calculate how many questions have been answered
        int answeredCount = 0;
        for (int answer : userAnswers) {
            if (answer != -1) answeredCount++;
        }
        
        // Enable submit button when at least half the questions have been answered
        boolean canSubmit = answeredCount >= questions.size() / 2;
        submitButton.setEnabled(canSubmit);
        
        // Visual indication of progress
        int progressPercent = (answeredCount * 100) / questions.size();
        if (progressPercent >= 50) {
            submitButton.setBackground(UIConstants.SUCCESS_COLOR);
        } else {
            submitButton.setBackground(UIConstants.PRIMARY_COLOR);
        }
    }
    
    private void confirmSubmitQuiz() {
        // Count unanswered questions
        int unansweredCount = 0;
        for (int answer : userAnswers) {
            if (answer == -1) unansweredCount++;
        }
        
        String message;
        if (unansweredCount > 0) {
            message = "You have " + unansweredCount + " unanswered question(s). Are you sure you want to submit your quiz?";
        } else {
            message = "Are you sure you want to submit your quiz?";
        }
        
        int result = JOptionPane.showConfirmDialog(
            this,
            message,
            "Submit Quiz",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            submitQuiz();
        }
    }
    
    private void submitQuiz() {
        if (timer != null) {
            timer.stop();
        }
        
        quizEndTime = System.currentTimeMillis();
        int timeTaken = (int) ((quizEndTime - quizStartTime) / 1000);

        // Calculate score
        correctAnswers = 0;
        int totalPoints = 0;
        
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            if (userAnswers[i] == question.getCorrectOptionIndex()) {
                correctAnswers++;
                totalPoints += question.getPoints();
            }
        }

        // Save result
        QuizResult result = new QuizResult();
        result.setUserId(quizController.getUserId());
        result.setCategoryId(selectedCategory.getCategoryId());
        result.setScore(totalPoints);
        result.setTotalQuestions(questions.size());
        result.setTimeTaken(timeTaken);
        
        quizController.saveQuizResult(result);

        // Show results
        JOptionPane.showMessageDialog(
            this,
            String.format("Quiz completed!\n\nCorrect answers: %d/%d\nPoints: %d\nTime taken: %d minutes, %d seconds",
                correctAnswers, questions.size(), totalPoints, timeTaken / 60, timeTaken % 60),
            "Quiz Results",
            JOptionPane.INFORMATION_MESSAGE
        );
        
        // Hide quiz elements
        questionPanel.setVisible(false);
        controlsPanel.setVisible(false);
        
        // Call completion callback
        if (onQuizEnd != null) {
            onQuizEnd.run();
        }
    }
    
    private void showKeyboardShortcutsHelp() {
        String helpText = 
            "<html><h2>Keyboard Shortcuts</h2>" +
            "<table border='0' cellpadding='5' style='font-size:12pt'>" +
            "<tr><td><b>1-4</b></td><td>Select answer option</td></tr>" +
            "<tr><td><b>Left Arrow</b> or <b>P</b></td><td>Previous question</td></tr>" +
            "<tr><td><b>Right Arrow</b> or <b>N</b></td><td>Next question</td></tr>" +
            "<tr><td><b>Enter</b></td><td>Submit quiz (when on last question)</td></tr>" +
            "</table>" +
            "</html>";
        
        JOptionPane.showMessageDialog(
            this,
            helpText,
            "Keyboard Shortcuts Help",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void flagCurrentQuestion() {
        if (questions == null || currentQuestionIndex < 0 || currentQuestionIndex >= questions.size()) {
            return;
        }

        Question currentQuestion = questions.get(currentQuestionIndex);
        
        // Create flag dialog
        JTextField reasonField = new JTextField(30);
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Reason for flagging this question:"));
        panel.add(reasonField);
        
        int result = JOptionPane.showConfirmDialog(
            this,
            panel,
            "Flag Question",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.OK_OPTION) {
            String reason = reasonField.getText().trim();
            currentQuestion.setFlagged(true);
            currentQuestion.setFlagReason(reason);
            
            // Save the flag
            quizController.flagQuestion(currentQuestion);
            
            JOptionPane.showMessageDialog(
                this,
                "Question has been flagged for review.",
                "Question Flagged",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    public void setMobileMode(boolean mobile) {
        Font baseFont = mobile ? new Font(Font.SANS_SERIF, Font.BOLD, 22) : new Font(Font.SANS_SERIF, Font.PLAIN, 14);
        int buttonHeight = mobile ? 48 : 30;
        int buttonFontSize = mobile ? 20 : 14;
        // Update question label
        questionLabel.setFont(baseFont);
        // Update option buttons
        for (JRadioButton btn : optionButtons) {
            btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, buttonFontSize));
            btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, buttonHeight));
        }
        // Update nav buttons
        prevButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, buttonFontSize));
        nextButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, buttonFontSize));
        submitButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, buttonFontSize));
        // Update help and flag buttons if present
        if (helpButton != null) helpButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, buttonFontSize));
        // Re-layout options panel for single column in mobile mode
        optionsPanel.setLayout(mobile ? new GridLayout(0, 1, 10, 20) : new GridLayout(0, 1, 5, 10));
        revalidate();
        repaint();
    }

    /**
     * Creates timeline indicators for each question
     */
    private void createTimelineIndicators() {
        // Get the timeline panel from the top panel hierarchy
        JPanel topPanel = (JPanel) getComponent(0);
        JPanel progressPanel = (JPanel) topPanel.getComponent(1);
        JPanel timelinePanel = (JPanel) progressPanel.getComponent(2);
        
        // Clear any existing indicators
        timelinePanel.removeAll();
        
        // Create indicator for each question
        for (int i = 0; i < questions.size(); i++) {
            JPanel indicatorPanel = createTimelineIndicator(i);
            timelinePanel.add(indicatorPanel);
        }
        
        timelinePanel.revalidate();
        timelinePanel.repaint();
    }
    
    /**
     * Creates a single timeline indicator
     */
    private JPanel createTimelineIndicator(int questionIndex) {
        final int size = 24;
        final Color defaultColor = new Color(200, 200, 200);
        final Color currentColor = UIConstants.PRIMARY_COLOR;
        final Color answeredColor = UIConstants.SECONDARY_COLOR;
        final Color correctColor = UIConstants.SUCCESS_COLOR;
        final Color incorrectColor = UIConstants.ERROR_COLOR;
        
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Determine color based on question state
                Color fillColor;
                if (questionIndex == currentQuestionIndex) {
                    fillColor = currentColor;
                } else if (userAnswers != null && userAnswers[questionIndex] != -1) {
                    fillColor = answeredColor;
                } else {
                    fillColor = defaultColor;
                }
                
                // Draw circle
                g2d.setColor(fillColor);
                g2d.fillOval(4, 4, size - 8, size - 8);
                
                // Draw number
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font(getFont().getName(), Font.BOLD, 10));
                String number = String.valueOf(questionIndex + 1);
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(number);
                int textHeight = fm.getHeight();
                g2d.drawString(number, 
                    (size - textWidth) / 2, 
                    (size - textHeight) / 2 + fm.getAscent());
            }
        };
        
        panel.setPreferredSize(new Dimension(size, size));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add mouse listener for navigation
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navigateToQuestion(questionIndex);
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                panel.setToolTipText("Go to question " + (questionIndex + 1));
            }
        });
        
        return panel;
    }
    
    /**
     * Navigates to a specific question
     */
    private void navigateToQuestion(int questionIndex) {
        if (questionIndex >= 0 && questionIndex < questions.size()) {
            currentQuestionIndex = questionIndex;
            displayCurrentQuestion();
            updateNavButtons();
            updateTimelineIndicators();
        }
    }
    
    /**
     * Updates the timeline indicators to reflect current state
     */
    private void updateTimelineIndicators() {
        // Get the timeline panel from the top panel hierarchy
        JPanel topPanel = (JPanel) getComponent(0);
        JPanel progressPanel = (JPanel) topPanel.getComponent(1);
        JPanel timelinePanel = (JPanel) progressPanel.getComponent(2);
        
        // Refresh all indicators
        for (Component comp : timelinePanel.getComponents()) {
            comp.repaint();
        }
    }

    /**
     * Applies focus mode styling to highlight the current question and options
     * This is especially useful for users with cognitive disabilities or ADHD
     */
    private void applyFocusMode() {
        // Apply focus mode to question and options panels
        AccessibilityManager.enableFocusMode(questionPanel, optionsPanel);
        
        // Add visual indicator of current question
        for (int i = 0; i < optionButtons.length; i++) {
            if (optionButtons[i].isVisible()) {
                // Make options more distinct in focus mode
                optionButtons[i].setFocusPainted(true);
                
                // Add special focus effect to help with tracking
                int optionIndex = i;
                optionButtons[i].addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        optionButtons[optionIndex].setBackground(new Color(230, 240, 255));
                        optionButtons[optionIndex].setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(UIConstants.PRIMARY_COLOR, 2),
                            BorderFactory.createEmptyBorder(8, 10, 8, 10)
                        ));
                    }
                    
                    @Override
                    public void focusLost(FocusEvent e) {
                        optionButtons[optionIndex].setBackground(new Color(245, 245, 250));
                        optionButtons[optionIndex].setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(220, 220, 240), 1),
                            BorderFactory.createEmptyBorder(10, 12, 10, 12)
                        ));
                    }
                });
            }
        }
        
        // Auto-focus first option (helps keyboard users)
        SwingUtilities.invokeLater(() -> {
            for (JRadioButton button : optionButtons) {
                if (button.isVisible()) {
                    button.requestFocusInWindow();
                    break;
                }
            }
        });
    }
} 