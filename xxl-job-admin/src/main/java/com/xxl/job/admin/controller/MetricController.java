package com.xxl.job.admin.controller;

import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.admin.dao.XxlJobLogDao;
import com.xxl.job.admin.dao.XxlJobRegistryDao;
import io.prometheus.client.exporter.common.TextFormat;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author kang.wang
 * @date 2020/6/15
 * @description
 */
@Controller
@RequestMapping("/customize_metrics")
public class MetricController {

    @Resource
    public XxlJobInfoDao xxlJobInfoDao;
    @Resource
    private XxlJobLogDao xxlJobLogDao;

    @RequestMapping(value = "", produces = TextFormat.CONTENT_TYPE_004)
    @ResponseBody
    @PermissionLimit(limit=false)
    public String getMetrics() {
        // 查询health_check task开启的数量
        int healthCheckTaskCount = xxlJobInfoDao.countHealthCheckTask();
        // 查询1分钟内执行成功的health_check task数量
        Date triggerTimeEnd = new Date();
        Date triggerTimeStart = new Date(triggerTimeEnd.getTime() - 60 * 1000);
        int successHealthCheckTaskCount = xxlJobLogDao.pageListCount(0, 0, 0, 0, triggerTimeStart, triggerTimeEnd, 1);

        List<CustomizeMetric> metrics = new ArrayList<>();
        CustomizeMetric metric1 = new CustomizeMetric("xxl_health_check_task_count", healthCheckTaskCount);
        metric1.addLabel("application", "xxl-job-admin");
        CustomizeMetric metric2 = new CustomizeMetric("xxl_health_check_task_success_count", successHealthCheckTaskCount);
        metric2.addLabel("application", "xxl-job-admin");
        metrics.add(metric1);
        metrics.add(metric2);
        return formatMetrics(metrics);
    }


    private static class CustomizeMetric {
        private String name;
        private Map<String, String> labels;
        private float value;

        public CustomizeMetric(String name, float value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, String> getLabels() {
            return labels;
        }

        public void setLabels(Map<String, String> labels) {
            this.labels = labels;
        }

        public float getValue() {
            return value;
        }

        public void setValue(float value) {
            this.value = value;
        }

        public void addLabel(String key, String value) {
            if (labels == null) {
                labels = new HashMap<>();
            }
            labels.put(key, value);
        }
    }

    private String formatMetrics(List<CustomizeMetric> metricList) {
        StringBuilder sb = new StringBuilder();
        for (CustomizeMetric metric : metricList) {
            sb.append(metric.getName()).append("{");
            if (!CollectionUtils.isEmpty(metric.getLabels())) {
                for (Map.Entry<String,String> entry : metric.getLabels().entrySet()) {
                    sb.append(entry.getKey()).append("=\"");
                    sb.append(entry.getValue()).append("\",");
                }
                sb.deleteCharAt(sb.length() - 1).toString();
            }
            sb.append("} ").append(metric.getValue()).append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }
}
