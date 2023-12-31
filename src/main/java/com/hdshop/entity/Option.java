package com.hdshop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "options")
public class Option {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long optionId;

    @Column(nullable = false)
    private String optionName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToMany(
            mappedBy = "option",
            cascade = {CascadeType.PERSIST, CascadeType.REMOVE}
    )
    private List<OptionValue> values = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        for (OptionValue value : values) {
            value.setOption(this);
        }
    }
}
