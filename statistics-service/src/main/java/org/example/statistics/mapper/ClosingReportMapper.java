package org.example.statistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.statistics.entity.ClosingReport;

/**
 * 结案报告 Mapper（统计分析服务只读）
 */
@Mapper
public interface ClosingReportMapper extends BaseMapper<ClosingReport> {
}
