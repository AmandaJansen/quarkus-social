package org.acme.sample.quarkussocial.domain.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Data
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "post_text")
    private String text;
    @Column(name = "dateTime")
    private LocalDateTime dateTime;
    @ManyToOne //usado para relacionamento de tabelas
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist

    public void prePersist() {

        setDateTime(LocalDateTime.now());

    }
}


