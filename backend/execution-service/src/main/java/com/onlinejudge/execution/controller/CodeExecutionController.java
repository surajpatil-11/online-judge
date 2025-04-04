package com.onlinejudge.execution.controller;

import com.onlinejudge.execution.model.CodeExecutionRequest;
import com.onlinejudge.execution.model.CodeExecutionResponse;
import com.onlinejudge.execution.service.JavaExecutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Code Execution", description = "API to execute Java code")
public class CodeExecutionController {

    @Autowired
    private JavaExecutorService javaExecutorService;

    @Operation(summary = "Execute Java code", description = "Executes complete Java class named 'Main'")
    @PostMapping("/execute")
    public CodeExecutionResponse executeCode(@RequestBody CodeExecutionRequest request) {
        return javaExecutorService.executeCode(request.getCode());
    }
}
