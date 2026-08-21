package com.example.java_template.feature.controller.api;


import com.example.java_template.common.response.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("api/template")
public interface TemplateApi {
    @PostMapping("/test")
    ApiResponse<String> test();
}
