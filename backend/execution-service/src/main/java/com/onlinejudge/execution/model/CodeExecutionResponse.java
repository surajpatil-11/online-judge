package com.onlinejudge.execution.model;

public class CodeExecutionResponse {
    private String output;
    private String error;
    private boolean success;

    public CodeExecutionResponse(String output, String error, boolean success) {
        this.output = output;
        this.error = error;
        this.success = success;
    }

    public String getOutput() {
        return output;
    }

    public String getError() {
        return error;
    }

    public boolean isSuccess() {
        return success;
    }
}
