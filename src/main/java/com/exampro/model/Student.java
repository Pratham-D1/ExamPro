package com.exampro.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Student – Combined model for Student user and profile data.
 */
public class Student implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    private long id;              // Maps to users.id
    private long profileId;       // Maps to student_profiles.id
    private String fullName;
    private String email;
    private boolean active = true;
    private String registrationNo;
    private String phone;
    private String department;
    private String classLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int examsAttempted;

    public Student() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getProfileId() {
        return profileId;
    }

    public void setProfileId(long profileId) {
        this.profileId = profileId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getClassLevel() {
        return classLevel;
    }

    public void setClassLevel(String classLevel) {
        this.classLevel = classLevel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getExamsAttempted() {
        return examsAttempted;
    }

    public void setExamsAttempted(int examsAttempted) {
        this.examsAttempted = examsAttempted;
    }

    public String getFormattedCreatedAt() {
        return (createdAt != null) ? createdAt.format(FORMATTER) : "N/A";
    }

    public String getFormattedUpdatedAt() {
        return (updatedAt != null) ? updatedAt.format(FORMATTER) : "N/A";
    }
}
