package com.example.projectfinalmobile.Networking;

public class OpenAIApiClient {
    private static ApiService openAIService;

    public static ApiService getGeminiService() {
        if (openAIService == null) {
            openAIService = GeminiClient.getGeminiService();
        }
        return openAIService;
    }
}
