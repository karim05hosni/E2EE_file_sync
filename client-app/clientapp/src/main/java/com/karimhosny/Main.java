package com.karimhosny;

import com.karimhosny.DIcontainer.AppFactory;
import com.karimhosny.auth.api.UserSession;
import com.karimhosny.auth.entities.User;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Client starting...");

        try {
            AppFactory appFactory = new AppFactory("D:\\Projects\\distributed_file_sync\\client-app\\clientapp\\storage");
            appFactory.getAuthService().login("karim@123.com", "1234567");
            User user = UserSession.getInstance().getCurrentUser();
            appFactory.getWsClient().connect("ws://localhost:8080/ws");
            appFactory.getUploadPipelineManager().start();
        } catch (Exception ex) {
            throw ex;
        }


    }
}