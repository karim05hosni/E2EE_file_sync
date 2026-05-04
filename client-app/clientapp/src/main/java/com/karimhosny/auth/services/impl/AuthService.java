package com.karimhosny.auth.services.impl;

import java.io.IOException;

import com.karimhosny.auth.api.UserSession;
import com.karimhosny.auth.entities.User;
import com.karimhosny.auth.services.contracts.IAuthService;
import com.karimhosny.connection.http.requests.AuthRequests;
import com.karimhosny.connection.http.responses.AuthResponse;
import com.karimhosny.connection.http.responses.BaseResponse;

public class AuthService implements IAuthService {
    private AuthRequests authRequests;
    public AuthService(AuthRequests authRequests) {
        this.authRequests = authRequests;
    }
    @Override
    public User login(String email, String password) {
        try {
            // send login request
            BaseResponse<AuthResponse> response = authRequests.login(email, password);
            AuthResponse responseData = response.getData().get(0);

            if (!response.isSuccess() && response.getData().isEmpty()) {
                System.out.println(" Login failed: " + response.getMessage());
                return null ;
            }
            // load User Entity
            User user = new User(responseData.getId(), responseData.getName(), "email", responseData.getSpaceId(), responseData.getToken());
            UserSession.getInstance().setCurrentUser(user);
            System.out.println(" Login success, hello: " + UserSession.getInstance().getCurrentUser().getName());
            return user;
        } catch (IOException ex) {
            System.getLogger(AuthService.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (InterruptedException ex) {
            System.getLogger(AuthService.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return null;
        }


    @Override
    public User register(String name, String email, String password) {
        try {
            BaseResponse<AuthResponse> response = authRequests.register(name, password, email);
            AuthResponse responseData = response.getData().get(0);
            
            if (!response.isSuccess() && response.getData().isEmpty()) {
                System.out.println("❌ Register failed: " + response.getMessage());
                return null ;
            }

            // load User Entity
            User user = new User(responseData.getId(), responseData.getName(), "email", responseData.getSpaceId(), responseData.getToken());
            UserSession.getInstance().setCurrentUser(user);
            System.out.println("✅ Login success, hello: " + UserSession.getInstance().getCurrentUser().getName());
            return user;
        } catch (Exception e) {
            System.out.println("⚠️ Error during Register: " + e.getMessage());
            return null;
        }
    }

}
