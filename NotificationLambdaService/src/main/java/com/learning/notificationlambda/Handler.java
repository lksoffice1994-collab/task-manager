package com.learning.notificationlambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.notificationlambda.model.OtpRequest;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;

public class Handler implements RequestHandler<SNSEvent, Void> {
    private static final String RESEND_API_KEY_ENV = "RESEND_API_KEY";

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Void handleRequest(SNSEvent event, Context context) {
        for (SNSEvent.SNSRecord record : event.getRecords()) {
            try {
                OtpRequest request = mapper.readValue(
                        record.getSNS().getMessage(),
                        OtpRequest.class
                );

                sendOtp(
                        request.getUserName(),
                        request.getSenderEmail(),
                        request.getOtp()
                );
            } catch (Exception e) {
                throw new RuntimeException("Could not process SNS message", e);
            }
        }
        return null;
    }

    private void sendOtp(String userName, String senderEmail, String otp)
            throws ResendException {

        Resend resend = new Resend(getResendApiKey());

        String html = "<p>Hi " + escapeHtml(userName) + ",</p>"
                + "<p>Welcome to the learning group. Please use this OTP: <strong>"
                + escapeHtml(otp)
                + "</strong>.</p>"
                + "<p>Thanks,<br>Learning Group Team</p>";

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Learning Group <onboarding@pragyasharma.online>")
                .to(senderEmail)
                .subject("Your Learning Group OTP")
                .html(html)
                .build();

        resend.emails().send(params);
    }

    private String getResendApiKey() {
        String apiKey = System.getenv(RESEND_API_KEY_ENV);
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Lambda environment variable " + RESEND_API_KEY_ENV + " is not set"
            );
        }
        return apiKey;
    }

    private String escapeHtml(String value) {
        return value == null ? "" : value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
