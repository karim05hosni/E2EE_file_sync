package com.karimhosny.storage.services.contracts;

import java.io.IOException;

import com.karimhosny.crypto.entities.WrappedPrivK;
import com.karimhosny.crypto.entities.kdfMetadata;

public interface IKeyStorageService {


    void saveWrappedPrivateKey(WrappedPrivK wrappedPrivK) throws IOException;

    WrappedPrivK loadWrappedPrivateKey() throws IOException;

    void saveUMK(kdfMetadata umkMetadata) throws IOException;
    

    kdfMetadata loadUMK() throws IOException;

}
