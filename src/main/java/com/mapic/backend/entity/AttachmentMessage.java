package com.mapic.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "attachment_messages")
@Data
@EqualsAndHashCode(callSuper = true)
public class AttachmentMessage extends Message {
    private String attachmentUrl;
    private String publicId;
    private String attachmentType;
}
