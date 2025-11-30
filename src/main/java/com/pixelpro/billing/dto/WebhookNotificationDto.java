package com.pixelpro.billing.dto;

public record WebhookNotificationDto(
        Data data,
        String type,
        String action,
//        @JsonProperty("date_created")
        String dateCreated
) {
    public record Data(
            String id
    ) {
    }
}
