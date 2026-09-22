package com.example.java_template.feature.controller;

import com.example.java_template.common.response.ApiResponse;
import com.example.java_template.common.response.ApiResponseFactory;
import com.example.java_template.feature.controller.api.TemplateApi;
import com.example.java_template.feature.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TemplateController implements TemplateApi {

    private final ApiResponseFactory apiResponseFactory;
    @SuppressWarnings("unused") // injected để Spring scan dependency TemplateService bean
    private final TemplateService templateService;

    @Override
    public ApiResponse<String> test() {
        return apiResponseFactory.success("Template controller is up and running");
    }
}
