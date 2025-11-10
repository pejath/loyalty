package com.clubloyalty.server.domain;

import com.clubloyalty.server.domain.user.User;

import javax.persistence.*;

@Entity
@Table(name = "members")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true)
    private String phone;

    @ManyToOne(optional = false)
    private Tier tier;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private Balance balance;

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String n) {
        fullName = n;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String p) {
        phone = p;
    }

    public Tier getTier() {
        return tier;
    }

    public void setTier(Tier t) {
        tier = t;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User u) {
        user = u;
    }

    public Balance getBalance() {
        return balance;
    }

    public void setBalance(Balance b) {
        balance = b;
    }
}
