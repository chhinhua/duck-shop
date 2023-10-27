package com.hdshop.entity.product;

import com.hdshop.component.ApplicationContextProvider;
import com.hdshop.component.SkuGenerator;
import com.hdshop.service.product.OptionValueService;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_skus")
public class ProductSku {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sku_id")
    private Long skuId;

    private String sku;

    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToMany
    @JoinTable(
            name = "product_sku_option_values",
            joinColumns = @JoinColumn(name = "sku_id"), // Khóa ngoại của product_skus
            inverseJoinColumns = @JoinColumn(name = "value_id") // Khóa ngoại của option_values
    )
    private List<OptionValue> optionValues = new ArrayList<>();

    @PrePersist
    @PreUpdate
    public void preAction() {
        this.sku = SkuGenerator.generateSku(product.getProductId(), optionValues);
    }
}