package org.example.appointment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.appointment.entity.TimeConfig;

import java.util.List;

public interface TimeConfigService extends IService<TimeConfig> {

    /** 获取全部启用时段 */
    List<TimeConfig> listAll();
}
