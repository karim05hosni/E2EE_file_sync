package com.karimhosny.crypto.services.contracts;

import java.security.PrivateKey;

public interface ICryptoService {


    void initUserKeys();

    byte[] loadPrivK();

    // byte[] loadPubK();


    byte[] loadUMK();

    void initUMK();


}
