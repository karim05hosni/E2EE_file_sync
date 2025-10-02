package com.karimhosny.cli.commands;

import com.karimhosny.auth.api.UserSession;
import com.karimhosny.auth.entities.User;
import com.karimhosny.auth.services.contracts.IAuthService;
import com.karimhosny.cli.commands.contracts.ICommand;

public class LoginCommand implements ICommand {
    private final IAuthService authService;

    public LoginCommand(IAuthService authService) {
        this.authService = authService;
    }

    @Override
    public String getName() {
        return "login";
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: login <email> <password>");
            return;
        }
        String email = args[0];
        String password = args[1];
        try {
            authService.login(email, password);
            User authuser = UserSession.getInstance().getCurrentUser();
            System.out.println("✅ Logged in as " + authuser.getName());
        } catch (Exception e) {
            System.err.println("❌ Login failed: " + e.getMessage());
        }
    }
}
