package org.example.user.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 心语碎片展示对象
 */
@Data
public class HealingFragmentVO {

    private Long id;
    private Integer moodLevel;
    private String moodLabel;
    private String moodEmoji;
    private String note;
    private String fragmentContent;
    private Integer isRead;
    private LocalDateTime createTime;
}
