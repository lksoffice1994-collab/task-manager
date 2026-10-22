package com.learning.notificationlambda.model;

public class OtpRequest {
    private String userName;
    private String senderEmail;
    private String otp;

    public OtpRequest() {} // Needed for deserialization

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}