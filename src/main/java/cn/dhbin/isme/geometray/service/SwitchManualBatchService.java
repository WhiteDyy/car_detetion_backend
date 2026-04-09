package cn.dhbin.isme.geometray.service;

import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.geometray.domain.entity.SwitchManualBatch;

import java.util.Map;

public interface SwitchManualBatchService {

    SwitchManualBatch createBatch(Map<String, Object> payload);

    Page<SwitchManualBatch> queryPage(Integer page, Integer pageSize, String keyword);
}
