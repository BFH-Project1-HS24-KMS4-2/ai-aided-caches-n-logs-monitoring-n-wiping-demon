package ch.bfh.tracesentry.daemon.domain.model.gptclient;

import java.util.ArrayList;
import java.util.List;

public class GPTRequest {

    private static final String DEFAULT_MODEL = "gpt-3.5-turbo";

    private final String model;
    private final List<GPTMessage> messages = new ArrayList<>();

    public GPTRequest(String systemPrompt, String userPrompt) {
        messages.add(new GPTMessage("system", systemPrompt));
        messages.add(new GPTMessage("user", userPrompt));
        this.model = DEFAULT_MODEL;
    }

    public GPTRequest(String systemPrompt, String userPrompt, String model) {
        //messages.add(new GPTMessage("system", systemPrompt));
        messages.add(new GPTMessage("user", systemPrompt + "\n" + userPrompt));
        this.model = model;
    }

    public String getModel() {
        return model;
    }

    public List<GPTMessage> getMessages() {
        return messages;
    }
}
