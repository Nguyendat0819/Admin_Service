package com.example.java_template.feature.controller;

import com.example.java_template.common.response.ApiResponse;
import com.example.java_template.feature.controller.api.TemplateApi;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class TemplateController implements TemplateApi {
    @Override
    public ApiResponse<String> test(){
        return ApiResponse.<String>builder() .transactionTime(LocalDateTime.now())
                .code("200").message("success").build();
    }
}
