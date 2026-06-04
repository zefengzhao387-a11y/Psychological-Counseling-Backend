package org.example.user.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiChatRequestDTO {
    private List<ChatMessageDTO> messages;
}
