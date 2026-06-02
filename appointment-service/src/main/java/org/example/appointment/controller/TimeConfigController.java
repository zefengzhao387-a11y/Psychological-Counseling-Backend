package org.example.appointment.controller;

import org.example.appointment.entity.TimeConfig;
import org.example.appointment.service.TimeConfigService;
import org.example.common.result.R;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 时间段配置（管理员）
 */
@RestController
@RequestMapping("/api/v1/appointment/time-config")
public class TimeConfigController {

    private final TimeConfigService timeConfigService;

    public TimeConfigController(TimeConfigService timeConfigService) {
        this.timeConfigService = timeConfigService;
    }

    @GetMapping
    public R<List<TimeConfig>> list() {
        return R.ok(timeConfigService.listAll());
    }

    @PostMapping
    public R<TimeConfig> add(@RequestBody TimeConfig config) {
        timeConfigService.save(config);
        return R.ok(config);
    }

    @PutMapping("/{id}")
    public R<TimeConfig> update(@PathVariable Long id, @RequestBody TimeConfig config) {
        config.setId(id);
        timeConfigService.updateById(config);
        return R.ok(config);
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        timeConfigService.removeById(id);
        return R.ok();
    }
}
