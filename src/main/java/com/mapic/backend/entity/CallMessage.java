package com.mapic.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "call_messages")
@Data
@EqualsAndHashCode(callSuper = true)
public class CallMessage extends Message {
    private String callStatus; // MISSED, ANSWERED, DECLINED
    private Integer duration; // in seconds
}
