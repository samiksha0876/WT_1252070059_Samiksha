package com.vit.result.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    // Max marks for internal (MSE) component
    private Double maxMse = 30.0;

    // Max marks for external (ESE) component
    private Double maxEse = 70.0;

    public Subject() {}

    public Subject(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Double getMaxMse() { return maxMse; }
    public void setMaxMse(Double maxMse) { this.maxMse = maxMse; }

    public Double getMaxEse() { return maxEse; }
    public void setMaxEse(Double maxEse) { this.maxEse = maxEse; }
}
