package com.kariimhosny.filesyncserver.auth.enrtities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;
    
    @Column(name = "password", nullable = false, length = 255)
    private String password;  // This will store the hashed password
    
    @Column(name = "space_id")
    private Long spaceId;  // Foreign key to spaces table
    
    // // Timestamps are helpful for debugging
    // @Column(name = "created_at")
    // private LocalDateTime createdAt;
    
    // @Column(name = "updated_at")
    // private LocalDateTime updatedAt;
    
    // Default constructor (required by JPA)
    public User() {}
    
    // Constructor for creating new users
    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        // this.createdAt = LocalDateTime.now();
        // this.updatedAt = LocalDateTime.now();
    }
    
    // Automatically update timestamps
    // @PrePersist
    // protected void onCreate() {
    //     createdAt = LocalDateTime.now();
    //     updatedAt = LocalDateTime.now();
    // }
    
    // @PreUpdate
    // protected void onUpdate() {
    //     updatedAt = LocalDateTime.now();
    // }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Long getSpaceId() {
        return spaceId;
    }
    
    public void setSpaceId(Long spaceId) {
        this.spaceId = spaceId;
    }
    
    // public LocalDateTime getCreatedAt() {
    //     return createdAt;
    // }
    
    // public void setCreatedAt(LocalDateTime createdAt) {
    //     this.createdAt = createdAt;
    // }
    
    // public LocalDateTime getUpdatedAt() {
    //     return updatedAt;
    // }
    
    // public void setUpdatedAt(LocalDateTime updatedAt) {
    //     this.updatedAt = updatedAt;
    // }
    
    // toString for debugging (don't include password!)
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", spaceId=" + spaceId +
                // ", createdAt=" + createdAt +
                '}';
    }
}
