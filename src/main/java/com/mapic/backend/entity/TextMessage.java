package com.mapic.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "text_messages")
@Data
@EqualsAndHashCode(callSuper = true)
public class TextMessage extends Message {
    private String content;
}
