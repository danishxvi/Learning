package com.danish.spring.validation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// A NESTED object inside CreateBookRequest - its own constraints only get checked if
// the outer request's field is annotated @Valid (not just present). See CreateBookRequest.
public class PublisherInfo {
    @NotBlank(message = "publisher name must not be blank")
    private String name;

    @Email(message = "publisher contact must be a well-formed email address")
    private String contactEmail;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
}
