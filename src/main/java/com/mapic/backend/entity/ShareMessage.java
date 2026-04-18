package com.mapic.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "share_messages")
@Data
@EqualsAndHashCode(callSuper = true)
public class ShareMessage extends Message {
    private Long targetId;
    private String shareType; // MOMENT, ALBUM
}
