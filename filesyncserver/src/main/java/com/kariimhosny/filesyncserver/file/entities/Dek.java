package com.kariimhosny.filesyncserver.file.entities;


import jakarta.persistence.*;

import java.time.LocalDateTime;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonSubTypes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "deks")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Dek {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_version_id", nullable = false)
    private Integer fileVersionId;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Lob
    @Column(name = "encrypted_dek", nullable = false, columnDefinition = "BYTEA")
    private byte[] encryptedDek;

    // // --- Constructors ---
    // public Dek() {
    // }

    // public Dek(Integer fileVersionId, Long userId, byte[] encryptedDek) {
    //     this.fileVersionId = fileVersionId;
    //     this.userId = userId;
    //     this.encryptedDek = encryptedDek;
    //     this.createdAt = LocalDateTime.now();
    // }

    // // --- Getters and Setters ---
    // public Long getId() {
    //     return id;
    // }

    // public Integer getFileVersionId() {
    //     return fileVersionId;
    // }

    // public void setFileVersionId(Integer fileVersionId) {
    //     this.fileVersionId = fileVersionId;
    // }

    // public LocalDateTime getCreatedAt() {
    //     return createdAt;
    // }

    // public void setCreatedAt(LocalDateTime createdAt) {
    //     this.createdAt = createdAt;
    // }

    // public Long getUserId() {
    //     return userId;
    // }

    // public void setUserId(Long userId) {
    //     this.userId = userId;
    // }

    // public byte[] getEncryptedDek() {
    //     return encryptedDek;
    // }

    // public void setEncryptedDek(byte[] encryptedDek) {
    //     this.encryptedDek = encryptedDek;
    // }
}
