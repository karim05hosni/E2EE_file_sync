package com.karimhosny;

import java.util.Arrays;

import com.karimhosny.DIcontainer.AppFactory;

public class Main {
    public static void main(String[] args) {
        System.out.println("Client starting...");

        try {
            AppFactory factory = new AppFactory("D:\\Projects\\distributed_file_sync\\client-app\\clientapp\\storage");
            factory.getOnboardingManager().run();
            
            System.out.println(Arrays.toString(factory.getCryptoService().loadPrivK()));
        } catch (Exception ex) {
            System.out.println(ex);
        }


    }
}