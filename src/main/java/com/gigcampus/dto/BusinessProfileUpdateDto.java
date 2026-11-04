package com.gigcampus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class BusinessProfileUpdateDto {

    @NotBlank(message = "Contact person name is required")
    @Size(min = 2, max = 100)
    private String fullName;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be a valid 10-digit number")
    private String phone;

    @NotBlank(message = "Business name is required")
    @Size(min = 2, max = 150)
    private String businessName;

    @NotBlank(message = "Business category is required")
    private String businessCategory;

    @NotBlank(message = "Location in Erode is required")
    private String location;

    private String description;
    private String contactEmail;
    private String contactPhone;
    private String websiteUrl;
    private String gstinOrRegNumber;

    public BusinessProfileUpdateDto() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessCategory() {
        return businessCategory;
    }

    public void setBusinessCategory(String businessCategory) {
        this.businessCategory = businessCategory;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getGstinOrRegNumber() {
        return gstinOrRegNumber;
    }

    public void setGstinOrRegNumber(String gstinOrRegNumber) {
        this.gstinOrRegNumber = gstinOrRegNumber;
    }
}
