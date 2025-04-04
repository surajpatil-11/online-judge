package com.onlinejudge.execution.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.onlinejudge.execution.util.ThreadDumpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Thread Dump", description = "API to get thread dump information")
public class ThreadDumpController {

    @Operation(summary = "Get Thread Dump", description = "Returns a detailed thread dump of the running application")
    @GetMapping("/thread-dump")
    public String getThreadDump() {
        return ThreadDumpUtil.getThreadDump();
    }
}