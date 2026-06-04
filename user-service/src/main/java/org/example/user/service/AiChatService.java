package org.example.user.service;

import org.example.user.dto.AiChatResponseDTO;
import org.example.user.dto.ChatMessageDTO;

import java.util.List;

public interface AiChatService {

    AiChatResponseDTO chat(List<ChatMessageDTO> messages);
}
