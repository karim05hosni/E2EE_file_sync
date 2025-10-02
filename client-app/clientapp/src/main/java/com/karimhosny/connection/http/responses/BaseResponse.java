package com.karimhosny.connection.http.responses;

import java.util.List;

public class BaseResponse<T> {
    private boolean success;
    private String message;
    private List<T> data;

    // getters + setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<T> getData() { return data; }
    public void setData(List<T> data) { this.data = data; }
}