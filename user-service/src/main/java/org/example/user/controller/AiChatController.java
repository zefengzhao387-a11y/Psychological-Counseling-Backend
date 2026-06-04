package org.example.user.controller;

import org.example.common.result.R;
import org.example.user.dto.AiChatRequestDTO;
import org.example.user.dto.AiChatResponseDTO;
import org.example.user.service.AiChatService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user/ai")
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping("/chat")
    public R<AiChatResponseDTO> chat(@RequestBody AiChatRequestDTO dto) {
        return R.ok(aiChatService.chat(dto.getMessages()));
    }
}
