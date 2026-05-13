package com.smartagent.knowledge.splitter;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 文本切片器
 * 将长文本切分成适合向量化的片段
 *
 * @author SmartAgent
 * @since 1.0.0
 */
public class TextSplitter {

    /**
     * 每片Token数
     */
    private static final int CHUNK_SIZE = 512;

    /**
     * 相邻片段重叠Token数
     */
    private static final int CHUNK_OVERLAP = 64;

    /**
     * 切分文本
     *
     * @param text     文本内容
     * @param metadata 元数据
     * @return 文本片段列表
     */
    public List<TextSegment> split(String text, Map<String, Object> metadata) {
        List<TextSegment> segments = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return segments;
        }

        // 按段落和句子粗切
        String[] paragraphs = text.split("\n\n");
        List<String> chunks = new ArrayList<>();

        StringBuilder currentChunk = new StringBuilder();
        for (String paragraph : paragraphs) {
            if (currentChunk.length() + paragraph.length() < CHUNK_SIZE) {
                currentChunk.append(paragraph).append("\n\n");
            } else {
                // 段落过长，按句子切分
                if (!currentChunk.isEmpty()) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk.setLength(0);
                }
                String[] sentences = paragraph.split("[。！？.!?]");
                for (String sentence : sentences) {
                    if (currentChunk.length() + sentence.length() < CHUNK_SIZE) {
                        currentChunk.append(sentence).append("。");
                    } else {
                        if (!currentChunk.isEmpty()) {
                            chunks.add(currentChunk.toString().trim());
                            currentChunk.setLength(0);
                        }
                        currentChunk.append(sentence).append("。");
                    }
                }
            }
        }

        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.toString().trim());
        }

        // 处理重叠
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            if (i > 0) {
                // 添加前一个片段的尾部重叠部分
                String previousChunk = chunks.get(i - 1);
                int overlapLength = Math.min(CHUNK_OVERLAP, previousChunk.length());
                String overlap = previousChunk.substring(previousChunk.length() - overlapLength);
                chunk = overlap + " " + chunk;
            }
            Metadata metadataObj = Metadata.from(metadata);
            segments.add(TextSegment.from(chunk, metadataObj));
        }

        return segments;
    }

    /**
     * 估算文本的Token数
     *
     * @param text 文本
     * @return Token数
     */
    private int countTokens(String text) {
        // 简单估算：1个中文或中文字符算1个Token，1个英文单词算1个Token
        int tokenCount = 0;
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (word.matches("[\\u4e00-\\u9fa5]")) {
                // 中文
                tokenCount += word.length();
            } else {
                // 英文
                tokenCount += 1;
            }
        }
        return tokenCount;
    }
}