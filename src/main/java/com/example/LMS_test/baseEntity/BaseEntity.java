package com.example.LMS_test.baseEntity;
//

import com.example.LMS_test.user.User;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners({AuditingEntityListener.class})
@MappedSuperclass
@Data
@Audited
public class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @ColumnDefault("0")
    @Column(nullable = false)
    private Long version;

    @CreatedDate
    private LocalDateTime creationDate;

    @LastModifiedDate
    private LocalDateTime lastModificationDate;

    @CreatedBy
    @ManyToOne(fetch = FetchType.EAGER)
    private User creator;

    @LastModifiedBy
    @ManyToOne(fetch = FetchType.EAGER)
    private User lastModifier;
}
