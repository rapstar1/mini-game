package com.example.demo.models;

import lombok.Getter;
import lombok.Setter;
import java.util.Map;


@Getter
@Setter
public class Response {
    private int status;
    private Map<String, Object> data;

    public Response(int status, Map<String, Object> message) {
        this.status = status;
        this.data = message;
    }

    
    public void addJwt(String jwt) {
        if (this.data != null) {
            this.data.put("jwt", jwt);  // 将 JWT 添加到返回的 Map 中
        }
    }
}
