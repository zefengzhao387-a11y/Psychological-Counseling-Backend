package org.example.consultation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.consultation.entity.ClosingReport;

import java.util.List;

public interface ClosingReportService extends IService<ClosingReport> {

    /** 提交结案报告 */
    ClosingReport submit(Long counselorId, ClosingReport report);

    /** 生成 Word 文档并返回文件路径 */
    String generateWord(Long reportId);

    /** 按条件查询 */
    List<ClosingReport> query(String studentNo, String studentName, Long counselorId, Integer problemType);
}
