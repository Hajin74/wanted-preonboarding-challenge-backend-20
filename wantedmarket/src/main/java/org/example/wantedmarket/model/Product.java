package org.example.wantedmarket.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.wantedmarket.status.ProductStatus;

@Entity
@Getter
@ToString
@NoArgsConstructor
@Table(name="products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer price;

    private Integer stock;

    private Long owner;

    @Enumerated(EnumType.STRING)
    private ProductStatus productStatus;

    @Builder
    public Product(String name, Integer price, Integer stock, ProductStatus productStatus, Long sellerId) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.productStatus = productStatus;
        this.sellerId = sellerId;
    }

    public void modifyProductStatus(ProductStatus productStatus) {
        this.productStatus = productStatus;
    }

    public void decreaseStock(Integer quantity) {
        this.stock -= quantity;
    }

    public void modifyPrice(Integer price) {
        this.price = price;
    }

}
