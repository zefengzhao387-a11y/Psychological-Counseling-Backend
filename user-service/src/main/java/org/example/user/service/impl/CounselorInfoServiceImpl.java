package org.example.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.common.exception.BusinessException;
import org.example.user.dto.CounselorInfoDTO;
import org.example.user.dto.CounselorInfoVO;
import org.example.user.entity.CounselorInfo;
import org.example.user.mapper.CounselorInfoMapper;
import org.example.user.service.CounselorInfoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CounselorInfoServiceImpl extends ServiceImpl<CounselorInfoMapper, CounselorInfo>
        implements CounselorInfoService {

    @Override
    public List<CounselorInfoVO> listAll() {
        return list().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public void create(CounselorInfoDTO dto) {
        CounselorInfo info = new CounselorInfo();
        info.setUserId(dto.getUserId());
        info.setName(dto.getName());
        info.setGender(dto.getGender());
        info.setPhone(dto.getPhone());
        info.setEmail(dto.getEmail());
        info.setType(dto.getType());
        info.setQualification(dto.getQualification());
        info.setSpecialty(dto.getSpecialty());
        info.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        save(info);
    }

    @Override
    public void update(Long id, CounselorInfoDTO dto) {
        CounselorInfo info = getById(id);
        if (info == null) {
            throw new BusinessException("记录不存在");
        }
        if (dto.getUserId() != null) info.setUserId(dto.getUserId());
        if (dto.getName() != null) info.setName(dto.getName());
        if (dto.getGender() != null) info.setGender(dto.getGender());
        if (dto.getPhone() != null) info.setPhone(dto.getPhone());
        if (dto.getEmail() != null) info.setEmail(dto.getEmail());
        if (dto.getType() != null) info.setType(dto.getType());
        if (dto.getQualification() != null) info.setQualification(dto.getQualification());
        if (dto.getSpecialty() != null) info.setSpecialty(dto.getSpecialty());
        if (dto.getStatus() != null) info.setStatus(dto.getStatus());
        updateById(info);
    }

    @Override
    public void delete(Long id) {
        if (getById(id) == null) {
            throw new BusinessException("记录不存在");
        }
        removeById(id);
    }

    private CounselorInfoVO toVO(CounselorInfo info) {
        CounselorInfoVO vo = new CounselorInfoVO();
        vo.setId(info.getId());
        vo.setUserId(info.getUserId());
        vo.setName(info.getName());
        vo.setGender(info.getGender());
        vo.setPhone(info.getPhone());
        vo.setEmail(info.getEmail());
        vo.setType(info.getType());
        vo.setQualification(info.getQualification());
        vo.setSpecialty(info.getSpecialty());
        vo.setStatus(info.getStatus());
        vo.setCreateTime(info.getCreateTime());
        return vo;
    }
}
