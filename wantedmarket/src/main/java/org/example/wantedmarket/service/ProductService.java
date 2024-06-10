package org.example.wantedmarket.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.wantedmarket.dto.product.ProductCreateDto;
import org.example.wantedmarket.dto.product.ProductDetailDto;
import org.example.wantedmarket.dto.product.ProductInfoDto;
import org.example.wantedmarket.model.Order;
import org.example.wantedmarket.model.Product;
import org.example.wantedmarket.model.User;
import org.example.wantedmarket.repository.OrderRepository;
import org.example.wantedmarket.repository.ProductRepository;
import org.example.wantedmarket.repository.UserRepository;
import org.example.wantedmarket.status.OrderStatus;
import org.example.wantedmarket.status.ProductStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    /* 제품 등록 */
    @Transactional
    public ProductCreateDto.Response saveProduct(Long userId, ProductCreateDto.Request request) {
        User seller = userRepository.findById(userId).orElseThrow(
                () ->  new RuntimeException("해당 사용자가 존재하지 않습니다."));

        Product newProduct = productRepository.save(Product.builder()
                        .name(request.getName())
                        .price(request.getPrice())
                        .status(ProductStatus.FOR_SALE)
                        .seller(seller)
                        .build());

        return ProductCreateDto.Response.from(newProduct);
    }

    /* 제품 전체 목록 조회 */
    @Transactional(readOnly = true)
    public List<ProductInfoDto> findAllProductList() {
        return productRepository.findAll().stream()
                .map(product -> new ProductInfoDto(
                        product.getId(),
                        product.getName(),
                        product.getPrice(),
                        product.getStatus()
                )).collect(Collectors.toList());
    }

    /* 제품 상세 조회 */
    @Transactional(readOnly = true)
    public ProductDetailDto findDetailProduct(Long productId) {
        Product findProduct = productRepository.findById(productId).orElseThrow(
                () ->  new RuntimeException("해당 제품이 존재하지 않습니다."));

        return ProductDetailDto.builder()
                .id(findProduct.getId())
                .name(findProduct.getName())
                .price(findProduct.getPrice())
                .status(findProduct.getStatus())
                .sellerId(findProduct.getSeller().getId())
                .build();
    }

    /* 구매한 제품 목록 조회 */
    @Transactional(readOnly = true)
    public List<ProductInfoDto> findOrderedProductList(Long userId) {
        List<Order> orders = orderRepository.findAllByBuyerIdAndStatus(userId, OrderStatus.COMPLETED);
        List<ProductInfoDto> productInfoDtoList = new ArrayList<>();

        for (Order order : orders) {
            ProductInfoDto productInfoDto = new ProductInfoDto();
            productInfoDto.setId(order.getProduct().getId());
            productInfoDto.setName(order.getProduct().getName());
            productInfoDto.setPrice(order.getProduct().getPrice());
            productInfoDto.setStatus(order.getProduct().getStatus());

            productInfoDtoList.add(productInfoDto);
        }
        return productInfoDtoList;
    }

    /* 예약중인 제품 목록 조회 */
    @Transactional(readOnly = true)
    public List<ProductInfoDto> findReservedProductList(Long userId) {
        List<Order> orders = orderRepository.findAllByBuyerIdAndStatus(userId, OrderStatus.IN_PROGRESS);
        List<ProductInfoDto> productInfoDtoList = new ArrayList<>();

        for (Order order : orders) {
            ProductInfoDto productInfoDto = new ProductInfoDto();
            productInfoDto.setId(order.getProduct().getId());
            productInfoDto.setName(order.getProduct().getName());
            productInfoDto.setPrice(order.getProduct().getPrice());
            productInfoDto.setStatus(order.getProduct().getStatus());

            productInfoDtoList.add(productInfoDto);
        }
        return productInfoDtoList;
    }

}