package com.kariimhosny.filesyncserver.file.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "file_versions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "file_id", nullable = false)
    private Integer fileId;
    
    @Column(name = "version_no", nullable = false)
    private Integer versionNo;
    
    @Column(name = "checksum", nullable = false)
    private String checksum;
    
    @Column(name = "by", nullable = false)
    private Long byUserId;
    
    @Column(name = "iv", nullable = false)
    private byte[] iv;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;
}
