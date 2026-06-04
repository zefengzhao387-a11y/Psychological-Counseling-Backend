package org.example.consultation.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.consultation.dto.ClosingReportQueryDTO;
import org.example.consultation.dto.ClosingReportSaveDTO;
import org.example.consultation.entity.ClosingReport;

import java.util.List;

/**
 * 结案报告 Service 接口
 */
public interface ClosingReportService extends IService<ClosingReport> {

    /**
     * 分页查询结案报告列表
     */
    Page<ClosingReport> pageQuery(ClosingReportQueryDTO queryDTO);

    /**
     * 根据ID查询结案报告详情
     */
    ClosingReport getDetailById(Long id);

    /**
     * 新增结案报告
     */
    ClosingReport create(ClosingReportSaveDTO saveDTO);

    /**
     * 咨询师提交结案报告（提交后自动生成 Word）
     */
    ClosingReport submit(Long counselorId, ClosingReport report);

    /**
     * 修改结案报告
     */
    ClosingReport update(Long id, ClosingReportSaveDTO saveDTO);

    /**
     * 删除结案报告（逻辑删除）
     */
    void delete(Long id);

    /**
     * 生成 Word 文档并返回文件路径
     */
    String generateWord(Long reportId);

    /**
     * 按条件查询列表
     */
    List<ClosingReport> query(String studentNo, String studentName, Long counselorId, Integer problemType);
}
