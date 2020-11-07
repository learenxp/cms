package com.lutech.cms.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class Container {
    @Id
    private Long id;
}
