package com.karimhosny.connection.http.requests;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karimhosny.connection.http.config.Client;
import com.karimhosny.connection.http.responses.BaseResponse;
import com.karimhosny.crypto.dto.UserKeysView;

public class CryptoRequests {

    private final Client client;
    private final ObjectMapper mapper = new ObjectMapper();

    public CryptoRequests(Client client) {
        this.client = client;
    }

    public BaseResponse storePubK(String publicKey, String token) throws JsonProcessingException, IOException, InterruptedException {
        // Serialize JSON safely
        Map<String, String> jsonMap = Map.of("publicKey", publicKey);
        String body = mapper.writeValueAsString(jsonMap);
        System.out.println("Request body: \n"+body);
        String respJson = client.post("api/crypto/store-pubK", body, token);

        // Deserialize response
        return mapper.readValue(respJson, BaseResponse.class);
    }

    public BaseResponse getPubk(String token) throws IOException, InterruptedException{
        String respJson = client.get("api/crypto/get-pubK", token);
        // Deserialize response
        return mapper.readValue(respJson, BaseResponse.class);
    }

    /**
     *
     * @param token
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public BaseResponse<UserKeysView> getSpaceUsersPubKeys(String token) throws IOException, InterruptedException{
        String respJson = client.get("api/crypto/get-users-pubk", token);
        // Deserialize response
        return mapper.readValue(respJson, new TypeReference<BaseResponse<UserKeysView>>() {});
    }
}
