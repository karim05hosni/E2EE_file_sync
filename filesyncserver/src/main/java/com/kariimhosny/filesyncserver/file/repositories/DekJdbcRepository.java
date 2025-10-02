package com.kariimhosny.filesyncserver.file.repositories;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DekJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void saveDek(Long userId, Integer fileVersionId, byte[] encryptedDek) {
        String sql = "INSERT INTO deks (user_id, file_version_id, encrypted_dek) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, fileVersionId, encryptedDek);
    }
}
