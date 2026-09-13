package com.danish.spring.txpropagation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String owner;
    private int balanceCents;

    protected Account() { }

    public Account(String owner, int balanceCents) {
        this.owner = owner;
        this.balanceCents = balanceCents;
    }

    public Long getId() { return id; }
    public String getOwner() { return owner; }
    public int getBalanceCents() { return balanceCents; }
    public void setBalanceCents(int balanceCents) { this.balanceCents = balanceCents; }
}
