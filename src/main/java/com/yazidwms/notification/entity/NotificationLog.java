package com.yazidwms.notification.entity;

import com.yazidwms.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notification_logs")
public class NotificationLog extends BaseEntity {

    @Column(nullable = false, length = 180)
    private String recipient;

    @Column(nullable = false, length = 180)
    private String subject;

    @Column(nullable = false, length = 2000)
    private String body;

    @Column(nullable = false)
    private boolean sent;

    @Column(length = 500)
    private String errorMessage;
}
