package org.example.user.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.common.exception.BusinessException;
import org.example.user.dto.AiChatResponseDTO;
import org.example.user.dto.ChatMessageDTO;
import org.example.user.service.AiChatService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiChatServiceImpl implements AiChatService {

    private static final String SYSTEM_PROMPT = """
            你是「听心」高校心理预约系统的 AI 助手。你的职责：
            1. 解答系统使用问题（初访登记、预约流程、角色功能等）；
            2. 提供一般性心理健康科普与自助建议，语气温暖、简洁；
            3. 遇到自伤、自杀、严重危机表述时，提醒用户立即联系学校心理中心或拨打危机热线，并建议预约专业咨询；
            4. 明确说明你不能替代专业心理咨询与诊断。
            回答使用中文，控制在 200 字以内，除非用户需要详细步骤。""";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${deepseek.api-key:}")
    private String apiKey;

    @Value("${deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    public AiChatServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public AiChatResponseDTO chat(List<ChatMessageDTO> messages) {
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException("DeepSeek API Key 未配置，请在 user-service 的 application.yml 或环境变量 DEEPSEEK_API_KEY 中设置");
        }
        if (messages == null || messages.isEmpty()) {
            throw new BusinessException("消息不能为空");
        }

        List<Map<String, String>> payloadMessages = new ArrayList<>();
        payloadMessages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
        for (ChatMessageDTO msg : messages) {
            if (!StringUtils.hasText(msg.getRole()) || !StringUtils.hasText(msg.getContent())) {
                continue;
            }
            String role = msg.getRole();
            if (!"user".equals(role) && !"assistant".equals(role)) {
                continue;
            }
            payloadMessages.add(Map.of("role", role, "content", msg.getContent().trim()));
        }

        if (payloadMessages.size() <= 1) {
            throw new BusinessException("请至少发送一条有效消息");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", payloadMessages);
        body.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey.trim());

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/chat/completions",
                    new HttpEntity<>(body, headers),
                    String.class
            );
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || !StringUtils.hasText(content.asText())) {
                throw new BusinessException("AI 返回内容为空");
            }
            return new AiChatResponseDTO(content.asText().trim());
        } catch (BusinessException e) {
            throw e;
        } catch (RestClientException e) {
            throw new BusinessException("调用 DeepSeek 失败：" + e.getMessage());
        } catch (Exception e) {
            throw new BusinessException("AI 服务异常：" + e.getMessage());
        }
    }
}
