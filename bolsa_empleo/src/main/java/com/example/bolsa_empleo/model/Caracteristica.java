package com.example.bolsa_empleo.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Caracteristica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Caracteristica parent;

    @OneToMany(mappedBy = "parent")
    private List<Caracteristica> children;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Caracteristica getParent() {
        return parent;
    }

    public void setParent(Caracteristica parent) {
        this.parent = parent;
    }

    public List<Caracteristica> getChildren() {
        return children;
    }

    public void setChildren(List<Caracteristica> children) {
        this.children = children;
    }
}
