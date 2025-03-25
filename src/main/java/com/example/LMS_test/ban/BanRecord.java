package com.example.LMS_test.ban;

import com.example.LMS_test.baseEntity.BaseEntity;
import com.example.LMS_test.patron.Patron;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class BanRecord extends BaseEntity {

    @Column(nullable = false)
    private LocalDate banUntil;

    @ManyToOne
    @JoinColumn(name = "patron_id", nullable = false)
    @JsonBackReference
    private Patron patron;
}
