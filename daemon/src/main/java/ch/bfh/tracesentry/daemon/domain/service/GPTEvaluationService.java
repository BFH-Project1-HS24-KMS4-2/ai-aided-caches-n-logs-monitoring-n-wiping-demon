package ch.bfh.tracesentry.daemon.domain.service;

import ch.bfh.tracesentry.daemon.domain.model.gptclient.GPTRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class GPTEvaluationService implements EvaluationService {

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GPTEvaluationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String evaluate(String filePath) {
        try {
            String fileContent = Files.readString(Path.of(filePath), Charset.defaultCharset());

            String requestBody = getPrompt(fileContent);


            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.add("Authorization", "Bearer " + Optional.ofNullable(System.getenv("OPENAI_API_KEY"))
                    .orElseThrow(() -> new IllegalStateException("API key is missing! Set OPENAI_API_KEY environment variable.")));
            headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    OPENAI_API_URL, HttpMethod.POST, entity, JsonNode.class);
            if (response.getBody() == null) {
                return "Error: Empty response";
            }
            return response.getBody().path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    private String getPrompt(String fileContent) {

        final var SYSTEM_PROMPT = "You receive entire file contents including their file name and path. Especially log or cache files. You will check these for harmful or problematic features.";

        var request = new GPTRequest(SYSTEM_PROMPT, fileContent, "o1-mini");
        try {
            return objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}

