package com.karimhosny.crypto.entities;

/**
 * store this in umk_metadata.json so that when the app restarts, 
 * you can re-derive the exact same UMK in memory.
 * @author Karim
*/
public class kdfMetadata {
    private String algorithm;     // e.g. "argon2id"
    private byte[] salt;
    private int memory;
    private int iterations;
    private int parallelism;

    public kdfMetadata() {
    }

    
    public kdfMetadata(String algorithm, byte[] salt, int memory, int iterations, int parallelism) {
        this.algorithm = algorithm;
        this.salt = salt;
        this.memory = memory;
        this.iterations = iterations;
        this.parallelism = parallelism;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public byte[] getSalt() {
        return salt;
    }

    public int getMemory() {
        return memory;
    }

    public int getIterations() {
        return iterations;
    }

    public int getParallelism() {
        return parallelism;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public void setParallelism(int parallelism) {
        this.parallelism = parallelism;
    }

    
}
