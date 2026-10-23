package com.learning.valkey;

public interface ValkeyService {

    void saveOtp(String email, String otp);

    String getOtp(String email);

    void deleteOtp(String email);

}
