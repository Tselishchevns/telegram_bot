package com.example.tele.model;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Data
@Table
public class Orders implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String text;
    private String comments;
    private Timestamp timeCreate;
    private Timestamp timeCheck;
    private long userId;

    public Orders(){

    }
    public Orders(long id, String text, String comments, Timestamp timeCreate) {
        this.id = id;
        this.text = text;
        this.comments = comments;
        this.timeCreate = timeCreate;
    }
}
