package com.example.LMS_test.patron;

import com.example.LMS_test.ban.BanRecord;
import com.example.LMS_test.baseEntity.BaseEntity;
import com.example.LMS_test.borrow.BorrowingRecord;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "patron", uniqueConstraints = {@UniqueConstraint(name = "name_unique", columnNames = "name")})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Patron extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String name;
    private String contactInfo;

    @OneToMany(mappedBy = "patron", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<BorrowingRecord> borrowingRecords;

    @OneToMany(mappedBy = "patron", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<BanRecord> banRecords;


}

