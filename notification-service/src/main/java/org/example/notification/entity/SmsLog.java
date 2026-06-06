package org.example.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sms_log")
public class SmsLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;

    private String content;

    private String templateCode;

    /** 0待发送 1已发送 2发送失败 */
    private Integer sendStatus;

    private LocalDateTime sendTime;

    private String failReason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
