package ch.bfh.tracesentry.daemon.domain.service;

import ch.bfh.tracesentry.daemon.domain.model.gptclient.GPTRequest;
import ch.bfh.tracesentry.daemon.exception.InternalServerErrorException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.Optional;

import static ch.bfh.tracesentry.daemon.common.Constants.SYSTEM_PROMPT;

@Service
public class GPTInspectionService implements InspectionService {

    @Value("${ai.openai_endpoint}")
    private String OPENAI_API_URL;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GPTInspectionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String inspect(String fileContent, String filePath) {
        try {
            String requestBody = getPrompt(filePath, fileContent);
            HttpEntity<String> entity = prepareRequest(requestBody);
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    OPENAI_API_URL, HttpMethod.POST, entity, JsonNode.class);
            return Objects.requireNonNull(response.getBody()).path("choices").get(0).path("message").path("content").asText();
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException("Could not process request");
        } catch (IllegalStateException e) {
            throw new InternalServerErrorException("API key is missing");
        } catch (RestClientException e) {
            throw new InternalServerErrorException("Could not reach OpenAI API");
        }
    }

    private static HttpEntity<String> prepareRequest(String requestBody) throws IllegalStateException {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Authorization", "Bearer " + Optional.ofNullable(System.getenv("OPENAI_API_KEY"))
                .orElseThrow(() -> new IllegalStateException("API key is missing! Set OPENAI_API_KEY environment variable.")));
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        return new HttpEntity<>(requestBody, headers);
    }

    private String getPrompt(String filePath, String fileContent) throws JsonProcessingException {
        var userPrompt = """
                file path: %s
                file content:
                %s""".formatted(filePath, fileContent);

        var request = new GPTRequest(SYSTEM_PROMPT, userPrompt);
        return objectMapper.writeValueAsString(request);
    }
}
