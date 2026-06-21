import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class GeminiService {

    private static final String MODEL = "gemini-2.5-flash";
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent";

    public String generateReply(List<Message> history) throws Exception {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            apiKey = System.getenv("GOOGLE_API_KEY");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new Exception("GEMINI_API_KEY is missing. Set it and run again.");
        }

        String payload = buildRequestBody(history);

        HttpURLConnection connection = (HttpURLConnection) new URL(API_URL).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("x-goog-api-key", apiKey);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(120000);
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        int status = connection.getResponseCode();
        InputStream stream = (status >= 200 && status < 300)
                ? connection.getInputStream()
                : connection.getErrorStream();

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        String responseText = response.toString();
        System.out.println("RAW GEMINI RESPONSE:");
        System.out.println(responseText);

        if (status < 200 || status >= 300) {
            String errorMessage = JsonUtils.extractJsonString(responseText, "message");
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = "HTTP " + status + ": " + responseText;
            }
            throw new Exception(errorMessage);
        }

        String output = JsonUtils.extractGeminiText(responseText);
        if (output == null || output.trim().isEmpty()) {
            String finishReason = JsonUtils.extractJsonString(responseText, "finishReason");
            String blockReason = JsonUtils.extractJsonString(responseText, "blockReason");

            if (blockReason != null && !blockReason.isEmpty()) {
                throw new Exception("Gemini blocked the response. blockReason = " + blockReason);
            }

            if (finishReason != null && !finishReason.isEmpty()) {
                throw new Exception("Gemini returned no text. finishReason = " + finishReason);
            }

            throw new Exception("No text found in Gemini response. Check RAW GEMINI RESPONSE in terminal.");
        }

        return output.trim();
    }

    private String buildRequestBody(List<Message> history) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"contents\":[");
        for (int i = 0; i < history.size(); i++) {
            Message msg = history.get(i);
            if (i > 0) sb.append(",");
            String role = "user";
            if ("model".equalsIgnoreCase(msg.getRole())) {
                role = "model";
            }

            sb.append("{");
            sb.append("\"role\":\"").append(role).append("\",");
            sb.append("\"parts\":[{");
            sb.append("\"text\":\"").append(JsonUtils.escapeJson(msg.getContent())).append("\"");
            sb.append("}]");
            sb.append("}");
        }
        sb.append("],");
        sb.append("\"generationConfig\":{");
        sb.append("\"temperature\":0.7,");
        sb.append("\"maxOutputTokens\":512");
        sb.append("}");
        sb.append("}");
        return sb.toString();
    }
}