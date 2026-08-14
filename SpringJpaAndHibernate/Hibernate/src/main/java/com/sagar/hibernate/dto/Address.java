package com.sagar.hibernate.dto;

import jakarta.persistence.Embeddable;

@Embeddable
public class Address {
    private String city;
    private long pincode;

}