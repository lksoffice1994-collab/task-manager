package com.learning.publisher.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.publisher.OtpPublisher;
import com.learning.publisher.model.OtpPublisherModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Component
public class SnsNotificationPublisher implements OtpPublisher {

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;
    private final String topicArn;

    public SnsNotificationPublisher(
            SnsClient snsClient,
            ObjectMapper objectMapper,
            @Value("${aws.sns.notification-topic-arn}") String topicArn) {
        this.snsClient = snsClient;
        this.objectMapper = objectMapper;
        this.topicArn = topicArn;
    }

    @Override
    public void publishOtp(OtpPublisherModel otpPublisherModel) {
        try {
            String message = objectMapper.writeValueAsString(otpPublisherModel);

            snsClient.publish(PublishRequest.builder()
                    .topicArn(topicArn)
                    .message(message)
                    .build());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not serialize OTP notification", e);
        }
    }
}