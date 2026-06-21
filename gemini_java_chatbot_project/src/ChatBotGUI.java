import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class ChatBotGUI extends JFrame {
    private final JTextArea chatArea = new JTextArea();
    private final JTextField inputField = new JTextField();
    private final JButton sendButton = new JButton("Send");
    private final JButton clearButton = new JButton("Clear");
    private final GeminiService geminiService = new GeminiService();
    private final List<Message> history = new ArrayList<>();

    public ChatBotGUI() {
        setTitle("Gemini AI Java Chatbot");
        setSize(760, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        chatArea.setEditable(false);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 15));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(chatArea);

        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 8, 8));
        buttonPanel.add(sendButton);
        buttonPanel.add(clearButton);

        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        history.add(new Message("user",
                "You are a helpful AI chatbot. Answer clearly, politely, and briefly unless more detail is requested."));

        appendBot("Hello! I am a Gemini AI Java chatbot.\n"
                + "1) Set GEMINI_API_KEY in your terminal.\n"
                + "2) Run this program.\n"
                + "3) Ask any question below.\n");

        sendButton.addActionListener(this::handleSend);
        inputField.addActionListener(this::handleSend);

        clearButton.addActionListener(e -> {
            history.clear();
            history.add(new Message("user",
                    "You are a helpful AI chatbot. Answer clearly, politely, and briefly unless more detail is requested."));
            chatArea.setText("");
            appendBot("Chat cleared. Start a new conversation.");
        });
    }

    private void handleSend(ActionEvent e) {
        String userText = inputField.getText().trim();
        if (userText.isEmpty()) return;

        appendUser(userText);
        inputField.setText("");
        inputField.setEnabled(false);
        sendButton.setEnabled(false);

        history.add(new Message("user", userText));

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                try {
                    return geminiService.generateReply(history);
                } catch (Exception ex) {
                    return "Error: " + ex.getMessage();
                }
            }

            @Override
            protected void done() {
                try {
                    String reply = get();
                    history.add(new Message("model", reply));
                    appendBot(reply);
                } catch (Exception ex) {
                    appendBot("Error: Unable to read AI response.");
                } finally {
                    inputField.setEnabled(true);
                    sendButton.setEnabled(true);
                    inputField.requestFocus();
                }
            }
        };
        worker.execute();
    }

    private void appendUser(String text) {
        chatArea.append("You: " + text + "\n\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void appendBot(String text) {
        chatArea.append("Bot: " + text + "\n\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatBotGUI().setVisible(true));
    }
}
