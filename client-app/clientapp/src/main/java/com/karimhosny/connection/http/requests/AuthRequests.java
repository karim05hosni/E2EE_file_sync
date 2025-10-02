package com.karimhosny.connection.http.requests;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karimhosny.connection.http.config.Client;
import com.karimhosny.connection.http.responses.AuthResponse;
import com.karimhosny.connection.http.responses.BaseResponse;

public class AuthRequests {

    private final Client client;
    private final ObjectMapper mapper = new ObjectMapper();


    public AuthRequests(Client client) {
        this.client = client;
    }

    public BaseResponse<AuthResponse> login(String email, String password) throws IOException, InterruptedException{
        String body = "{ \"email\": \"" + email + "\", \"password\": \"" + password + "\" }";

        String respJson = client.post("api/auth/login", body, null);
        // Parse JSON → model (use Jackson/Gson instead of manual parsing later)
        return mapper.readValue(respJson, new TypeReference<BaseResponse<AuthResponse>>() {});
    }

    public BaseResponse<AuthResponse> register(String name, String password, String email) throws IOException, InterruptedException {
        String body = "{ \"name\": \"" + name + "\", "
                + "\"password\": \"" + password + "\", "
                + "\"email\": \"" + email + "\" }";

        String respJson = client.post("api/auth/register", body, null);
        return mapper.readValue(respJson, new TypeReference<BaseResponse<AuthResponse>>() {});
    }
}
