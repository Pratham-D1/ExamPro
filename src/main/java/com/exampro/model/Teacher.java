package com.exampro.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Teacher – Combined model for Teacher user, profile, and assigned subjects.
 */
public class Teacher implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    private long id;              // Maps to users.id
    private long profileId;       // Maps to teacher_profiles.id
    private String fullName;
    private String email;
    private boolean active = true;
    private String employeeId;
    private String phone;
    private String department;
    private String qualification;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Subject> assignedSubjects = new ArrayList<>();

    public Teacher() {
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

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
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

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
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

    public List<Subject> getAssignedSubjects() {
        return assignedSubjects;
    }

    public void setAssignedSubjects(List<Subject> assignedSubjects) {
        this.assignedSubjects = (assignedSubjects != null) ? assignedSubjects : new ArrayList<>();
    }

    public int getAssignedSubjectsCount() {
        return assignedSubjects.size();
    }

    public String getAssignedSubjectCodes() {
        if (assignedSubjects.isEmpty()) {
            return "None";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < assignedSubjects.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(assignedSubjects.get(i).getCode());
        }
        return sb.toString();
    }

    public String getFormattedCreatedAt() {
        return (createdAt != null) ? createdAt.format(FORMATTER) : "N/A";
    }

    public String getFormattedUpdatedAt() {
        return (updatedAt != null) ? updatedAt.format(FORMATTER) : "N/A";
    }
}
