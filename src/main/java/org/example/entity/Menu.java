package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "menus")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    private  String path;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Menu parent; //self-reference, ini yang bikin hierarki menu

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
}

//@ManyToOne di Menu.parent — Entity ini mereferensikan dirinya sendiri, persis seperti relasi "parent of" di diagram di wa.
