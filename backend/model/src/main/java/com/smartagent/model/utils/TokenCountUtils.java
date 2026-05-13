package com.smartagent.model.utils;

import com.smartagent.model.dto.AiUsage;

public final class TokenCountUtils {

    private TokenCountUtils() {
    }

    public static int countTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }

        int tokenCount = 0;
        String[] words = text.trim().split("\\s+");
        for (String word : words) {
            if (word.matches(".*[\\u4e00-\\u9fa5].*")) {
                tokenCount += word.length();
            } else {
                tokenCount += 1;
            }
        }
        return tokenCount;
    }

    public static AiUsage buildUsage(String prompt, String completion) {
        int promptTokens = countTokens(prompt);
        int completionTokens = countTokens(completion);
        return new AiUsage(promptTokens, completionTokens, promptTokens + completionTokens);
    }
}
