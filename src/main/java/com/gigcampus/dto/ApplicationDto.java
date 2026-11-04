package com.gigcampus.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ApplicationDto {

    private Long gigId;

    @NotBlank(message = "Please write a concise proposal explaining how you will complete this gig")
    @Size(min = 20, max = 2000, message = "Proposal must be between 20 and 2000 characters")
    private String proposal;

    @NotNull(message = "Please provide an estimated delivery time in days")
    @Min(value = 1, message = "Estimated delivery must be at least 1 day")
    private Integer estimatedDays;

    @DecimalMin(value = "100.00", message = "Proposed budget must be at least ₹100.00")
    private BigDecimal proposedBudget;

    public ApplicationDto() {
    }

    public Long getGigId() {
        return gigId;
    }

    public void setGigId(Long gigId) {
        this.gigId = gigId;
    }

    public String getProposal() {
        return proposal;
    }

    public void setProposal(String proposal) {
        this.proposal = proposal;
    }

    public Integer getEstimatedDays() {
        return estimatedDays;
    }

    public void setEstimatedDays(Integer estimatedDays) {
        this.estimatedDays = estimatedDays;
    }

    public BigDecimal getProposedBudget() {
        return proposedBudget;
    }

    public void setProposedBudget(BigDecimal proposedBudget) {
        this.proposedBudget = proposedBudget;
    }
}
