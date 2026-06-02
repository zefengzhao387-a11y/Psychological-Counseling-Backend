package org.example.appointment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.appointment.entity.TimeConfig;
import org.example.appointment.mapper.TimeConfigMapper;
import org.example.appointment.service.TimeConfigService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TimeConfigServiceImpl extends ServiceImpl<TimeConfigMapper, TimeConfig> implements TimeConfigService {

    @Override
    public List<TimeConfig> listAll() {
        return lambdaQuery().orderByAsc(TimeConfig::getStartTime).list();
    }
}
