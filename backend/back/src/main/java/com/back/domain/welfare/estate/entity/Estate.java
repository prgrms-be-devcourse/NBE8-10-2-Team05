package com.back.domain.welfare.estate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "estate")
@Getter
@NoArgsConstructor
public class Estate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uppAisTpNm;
    private String aisTpCdNm;
    private String panNm;
    private String cnpCdNm;
    private String panSs;
    private String dtlUrl;
}
