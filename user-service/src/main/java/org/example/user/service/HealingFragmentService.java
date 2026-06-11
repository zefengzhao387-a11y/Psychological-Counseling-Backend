package org.example.user.service;

import org.example.user.dto.GenerateFragmentDTO;
import org.example.user.dto.HealingFragmentVO;

import java.util.List;

/**
 * 心语碎片服务接口
 */
public interface HealingFragmentService {

    /**
     * 生成新的治愈碎片（调用 AI 生成内容）
     */
    HealingFragmentVO generate(Long studentId, GenerateFragmentDTO dto);

    /**
     * 查看学生所有治愈碎片（按时间倒序）
     */
    List<HealingFragmentVO> listByStudent(Long studentId);

    /**
     * 标记碎片为已读
     */
    void markRead(Long fragmentId, Long studentId);

    /**
     * 获取今日心情统计（最近7天趋势）
     */
    List<HealingFragmentVO> weeklyTrend(Long studentId);

    /**
     * 删除某条碎片
     */
    void delete(Long fragmentId, Long studentId);
}
