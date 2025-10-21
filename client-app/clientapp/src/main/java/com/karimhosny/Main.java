package com.karimhosny;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karimhosny.DIcontainer.AppFactory;
import com.karimhosny.auth.api.UserSession;
import com.karimhosny.auth.entities.User;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("Client starting...");

        try {
            AppFactory appFactory = new AppFactory();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> config = mapper.readValue(appFactory.getStorageConfig().getConfigPath().toFile(), Map.class);
            
            appFactory.getAuthService().login(config.get("email"), config.get("password"));
            appFactory.getOnboardingManager().run();
            User user = UserSession.getInstance().getCurrentUser();
            appFactory.getWsClient().connect("ws://localhost:8080/ws");
            appFactory.getUploadPipelineManager().start();
            appFactory.getDownloadPipelineManager().start();
        } catch (Exception ex) {
            throw ex;
        }

    }
}
