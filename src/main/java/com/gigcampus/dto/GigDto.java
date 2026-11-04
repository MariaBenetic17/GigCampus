package com.gigcampus.dto;

import com.gigcampus.entity.GigStatus;
import com.gigcampus.entity.WorkMode;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GigDto {

    private Long id;

    @NotBlank(message = "Gig title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;

    @NotNull(message = "Please select a service category")
    private Long categoryId;

    @NotBlank(message = "Detailed description of requirements is required")
    @Size(min = 20, message = "Description should provide at least 20 characters of detail")
    private String description;

    @NotBlank(message = "Specify required skills (e.g. Canva, WordPress, Excel, Python)")
    private String requiredSkills;

    @NotNull(message = "Budget is required")
    @DecimalMin(value = "200.00", message = "Minimum gig budget is ₹200.00")
    @DecimalMax(value = "100000.00", message = "Budget cannot exceed ₹1,00,000.00")
    private BigDecimal budget;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Future(message = "Deadline must be in the future")
    private LocalDate deadline;

    @NotNull(message = "Please select work mode")
    private WorkMode workMode = WorkMode.REMOTE;

    private String location;

    private GigStatus status = GigStatus.OPEN;

    public GigDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public WorkMode getWorkMode() {
        return workMode;
    }

    public void setWorkMode(WorkMode workMode) {
        this.workMode = workMode;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public GigStatus getStatus() {
        return status;
    }

    public void setStatus(GigStatus status) {
        this.status = status;
    }
}
