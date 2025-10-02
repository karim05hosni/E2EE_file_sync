package com.kariimhosny.filesyncserver.file.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "files_metadata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    

    
    @Column(name = "ext", length = 50)
    private String extension;
    
    @Column(name = "space_id", nullable = false)
    private Long spaceId;
    
    @Column(name = "owner", nullable = false)
    private Long owner;
    
    @Column(name = "path", nullable = false, columnDefinition = "TEXT")
    private String path;
}
