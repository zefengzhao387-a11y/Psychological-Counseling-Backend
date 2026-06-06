package org.example.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.notification.entity.SmsLog;

@Mapper
public interface SmsLogMapper extends BaseMapper<SmsLog> {
}
