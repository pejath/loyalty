package com.clubloyalty.server.domain.user;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "roles")
public class Role implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String code;

    public Role() {
    }

    public Role(String code) {
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String c) {
        this.code = c;
    }
}
