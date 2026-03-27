package com.example.bolsa_empleo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Admin {

    @Id
    private String identification;

    private String password;
    private String name;

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
