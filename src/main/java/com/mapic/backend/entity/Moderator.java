package com.mapic.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "moderators")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Moderator extends Account {
}
