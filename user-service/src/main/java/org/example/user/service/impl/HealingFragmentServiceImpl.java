package org.example.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.common.exception.BusinessException;
import org.example.user.dto.GenerateFragmentDTO;
import org.example.user.dto.HealingFragmentVO;
import org.example.user.entity.HealingFragment;
import org.example.user.mapper.HealingFragmentMapper;
import org.example.user.service.HealingFragmentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HealingFragmentServiceImpl extends ServiceImpl<HealingFragmentMapper, HealingFragment>
        implements HealingFragmentService {

    private static final Map<Integer, String> MOOD_LABELS = Map.of(
            1, "很差", 2, "不太好", 3, "一般", 4, "不错", 5, "很好"
    );
    private static final Map<Integer, String> MOOD_EMOJIS = Map.of(
            1, "😢", 2, "😔", 3, "😐", 4, "😊", 5, "😄"
    );

    private static final List<String> FRAGMENT_THEMES = List.of(
            "温柔地提醒学生，任何情绪都是可以被接纳的",
            "鼓励学生看到自己已经走了多远的路",
            "分享一个温暖的小故事或隐喻",
            "肯定学生主动寻求帮助的勇气",
            "提醒学生照顾好自己的身体和心灵",
            "用诗意的语言描述希望和成长",
            "告诉学生他们并不孤单",
            "温柔地引导关注生活中的微小美好"
    );

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${deepseek.api-key:}")
    private String apiKey;

    @Value("${deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    public HealingFragmentServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public HealingFragmentVO generate(Long studentId, GenerateFragmentDTO dto) {
        if (dto.getMoodLevel() == null || dto.getMoodLevel() < 1 || dto.getMoodLevel() > 5) {
            throw new BusinessException("请选择一个心情状态");
        }
        if (dto.getNote() != null && dto.getNote().length() > 200) {
            throw new BusinessException("心情笔记最多200字");
        }

        // 每日限制最多生成3条
        long todayCount = lambdaQuery()
                .eq(HealingFragment::getStudentId, studentId)
                .ge(HealingFragment::getCreateTime, LocalDate.now().atStartOfDay())
                .count();
        if (todayCount >= 3) {
            throw new BusinessException("今日已生成足够多的碎片，明天再来吧~");
        }

        String aiContent = callAiForFragment(dto);
        if (!StringUtils.hasText(aiContent)) {
            throw new BusinessException("AI 生成失败，请稍后重试");
        }

        HealingFragment fragment = new HealingFragment();
        fragment.setStudentId(studentId);
        fragment.setMoodLevel(dto.getMoodLevel());
        fragment.setNote(dto.getNote());
        fragment.setFragmentContent(aiContent.trim());
        fragment.setIsRead(0);
        save(fragment);

        log.info("学生 {} 生成心语碎片 id={} mood={}", studentId, fragment.getId(), dto.getMoodLevel());
        return toVO(fragment);
    }

    @Override
    public List<HealingFragmentVO> listByStudent(Long studentId) {
        List<HealingFragment> list = lambdaQuery()
                .eq(HealingFragment::getStudentId, studentId)
                .orderByDesc(HealingFragment::getCreateTime)
                .list();
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markRead(Long fragmentId, Long studentId) {
        HealingFragment fragment = getById(fragmentId);
        if (fragment == null || !fragment.getStudentId().equals(studentId)) {
            throw new BusinessException("无权操作此碎片");
        }
        fragment.setIsRead(1);
        updateById(fragment);
    }

    @Override
    public List<HealingFragmentVO> weeklyTrend(Long studentId) {
        LocalDate weekAgo = LocalDate.now().minusDays(7);
        List<HealingFragment> list = lambdaQuery()
                .eq(HealingFragment::getStudentId, studentId)
                .ge(HealingFragment::getCreateTime, weekAgo.atStartOfDay())
                .orderByAsc(HealingFragment::getCreateTime)
                .list();
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long fragmentId, Long studentId) {
        HealingFragment fragment = getById(fragmentId);
        if (fragment == null || !fragment.getStudentId().equals(studentId)) {
            throw new BusinessException("无权操作此碎片");
        }
        removeById(fragmentId);
    }

    // ---- private helpers ----

    private HealingFragmentVO toVO(HealingFragment f) {
        HealingFragmentVO vo = new HealingFragmentVO();
        vo.setId(f.getId());
        vo.setMoodLevel(f.getMoodLevel());
        vo.setMoodLabel(MOOD_LABELS.getOrDefault(f.getMoodLevel(), "—"));
        vo.setMoodEmoji(MOOD_EMOJIS.getOrDefault(f.getMoodLevel(), "—"));
        vo.setNote(f.getNote());
        vo.setFragmentContent(f.getFragmentContent());
        vo.setIsRead(f.getIsRead());
        vo.setCreateTime(f.getCreateTime());
        return vo;
    }

    private String callAiForFragment(GenerateFragmentDTO dto) {
        if (!StringUtils.hasText(apiKey)) {
            // 如果没有配置 AI Key，返回预设的暖心话语
            return getFallbackFragment(dto.getMoodLevel());
        }

        String moodDesc = MOOD_LABELS.getOrDefault(dto.getMoodLevel(), "一般");
        String moodEmoji = MOOD_EMOJIS.getOrDefault(dto.getMoodLevel(), "😐");
        String theme = FRAGMENT_THEMES.get(new Random().nextInt(FRAGMENT_THEMES.size()));

        String userMessage = String.format(
                "我今天的心情是：%s %s。%s",
                moodEmoji, moodDesc,
                StringUtils.hasText(dto.getNote()) ? "我想说的是：" + dto.getNote() : ""
        );

        String systemPrompt = String.format("""
                你是「听心」心理系统的温柔陪伴者。你的任务是为学生生成一段温暖的"心语碎片"。

                要求：
                1. 主题方向：%s
                2. 内容温暖、诗意、有力量，像一束光照进心里
                3. 100-180字，中文
                4. 不要出现"建议"、"你应该"等说教口吻
                5. 用第二人称"你"
                6. 结尾加一个暖心的 emoji
                7. 回复纯文本，不要带任何格式标记""", theme);

        try {
            List<Map<String, String>> messages = List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userMessage)
            );

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", messages);
            body.put("temperature", 0.9);
            body.put("max_tokens", 400);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey.trim());

            String response = restTemplate.postForObject(
                    baseUrl + "/chat/completions",
                    new HttpEntity<>(body, headers),
                    String.class
            );

            JsonNode root = objectMapper.readTree(response);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || !StringUtils.hasText(content.asText())) {
                return getFallbackFragment(dto.getMoodLevel());
            }
            return content.asText().trim();
        } catch (Exception e) {
            log.warn("AI 生成心语碎片失败，使用预设内容: {}", e.getMessage());
            return getFallbackFragment(dto.getMoodLevel());
        }
    }

    /**
     * 当 AI 不可用时，返回预设的暖心话语
     */
    private String getFallbackFragment(int moodLevel) {
        List<String> fragments = switch (moodLevel) {
            case 1, 2 -> List.of(
                    "有时候，天空会有阴云，但这不代表太阳不存在了。你的感受是真实的，也是暂时的。给自己一点时间，就像允许一场雨下完，然后等待彩虹 🌈",
                    "难过的日子不是软弱，是你在用力感受这个世界。累了就歇一歇，你已经做得很好了。这个世界需要你，真实的你 💙",
                    "当世界变得沉重，记得你不需要一个人扛。低头看看脚下，你已经走过了那么多路。温柔地抱抱自己，明天会是新的一天 🌅"
            );
            case 3 -> List.of(
                    "平平淡淡的日子，就像静静的湖水。没有波澜不代表没有深度。你的存在本身就是一种安稳的力量，继续走，风景会在不经意间出现 🍃",
                    "生活不一定每天都有烟花，但每天都有光。也许是一杯热茶，也许是窗外的一片绿叶。你今天已经足够好了 ☀️",
                    "有时候，不悲不喜就是一种坚定。你在这平凡的一天里，也在认真地生活着，这本身就是一件了不起的事 🌿"
            );
            case 4, 5 -> List.of(
                    "今天的阳光格外明亮，那是因为你的心在发光。好心情是会传染的，你的笑容或许就是别人今天最温暖的礼物 ✨",
                    "感觉到快乐的时候，就尽情地快乐吧！你值得所有的美好。把这些温暖收集起来，在需要的时候它们会变成你内心的光 🌻",
                    "今天的心情像一首轻快的歌。记住这个感觉，它是你真的在好好生活的证明。愿你每一天都能找到属于自己的小确幸 🎵"
            );
        };
        return fragments.get(new Random().nextInt(fragments.size()));
    }
}
