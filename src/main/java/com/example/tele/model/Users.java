package com.example.tele.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table
@Data
public class Users implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private Long chatId;
    private String firstName;
    private String lastName;
    private String userName;
    private int level;
    private Timestamp registredAd;
    private Timestamp deleteAd;


    public Users() {
    }

    public Users(Long chatId, String firstName, String lastName, String userName, int a_level) {
        this.chatId = chatId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.level = a_level;
    }

    @Override
    public String toString() {
        return "ID:" + chatId + " , " + lastName + " " + firstName + ", " + userName
                + ", Уровень: " + level;
    }
}
