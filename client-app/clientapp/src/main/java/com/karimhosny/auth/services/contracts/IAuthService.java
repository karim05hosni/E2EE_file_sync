package com.karimhosny.auth.services.contracts;

import com.karimhosny.auth.entities.User;

public interface IAuthService {

    User login(String email, String password);

    User register(String name, String email, String password);
}
