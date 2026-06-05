package org.example.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.user.dto.CounselorInfoDTO;
import org.example.user.dto.CounselorInfoVO;
import org.example.user.entity.CounselorInfo;

import java.util.List;

public interface CounselorInfoService extends IService<CounselorInfo> {

    /** 查询全部列表 */
    List<CounselorInfoVO> listAll();

    /** 新增 */
    void create(CounselorInfoDTO dto);

    /** 更新 */
    void update(Long id, CounselorInfoDTO dto);

    /** 删除 */
    void delete(Long id);
}
