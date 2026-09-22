package com.example.java_template.feature.controller.api;


import com.example.java_template.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/template")
public interface TemplateApi {
    @GetMapping("/test")
    ApiResponse<String> test();
}
