package com.mysite.sbb;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class BaseTimeEntity {
	@Column(name = "create_date")
    private LocalDateTime createDate;   // 생성일시

    @Column(name = "modifiy_date")
    private LocalDateTime modifyDate;  // 최종 수정일시

    @PrePersist
    public void prePersist() {
        this.createDate = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.modifyDate = LocalDateTime.now();
    }
}
