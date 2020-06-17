package com.xxl.job.admin.controller;

import com.xxl.job.admin.controller.annotation.PermissionLimit;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author kang.wang
 * @date 2020/6/17
 * @description
 */
@Controller
@RequestMapping("status")
public class HealthCheckController {

    @RequestMapping("")
    @ResponseBody
    @PermissionLimit(limit=false)
    public String healthCheck() {
        return "success";
    }
}
