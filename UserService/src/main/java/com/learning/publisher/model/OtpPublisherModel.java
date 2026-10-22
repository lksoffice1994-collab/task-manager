package com.learning.publisher.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OtpPublisherModel {
    private String userName;
    private String senderEmail;
    private String otp;
}