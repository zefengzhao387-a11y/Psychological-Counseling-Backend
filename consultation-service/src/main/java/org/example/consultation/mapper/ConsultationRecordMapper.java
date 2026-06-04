package org.example.consultation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.consultation.entity.ConsultationRecord;

@Mapper
public interface ConsultationRecordMapper extends BaseMapper<ConsultationRecord> {
}
