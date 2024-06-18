package org.example.wantedmarket.service;

import org.example.wantedmarket.dto.order.TransactionResponse;
import org.example.wantedmarket.dto.product.ProductCreateRequest;
import org.example.wantedmarket.dto.product.ProductDetailResponse;
import org.example.wantedmarket.dto.product.ProductResponse;
import org.example.wantedmarket.dto.product.ProductUpdateRequest;
import org.example.wantedmarket.exception.CustomException;
import org.example.wantedmarket.exception.ErrorCode;
import org.example.wantedmarket.model.Order;
import org.example.wantedmarket.model.Product;
import org.example.wantedmarket.model.User;
import org.example.wantedmarket.repository.OrderRepository;
import org.example.wantedmarket.repository.ProductRepository;
import org.example.wantedmarket.repository.UserRepository;
import org.example.wantedmarket.status.OrderStatus;
import org.example.wantedmarket.status.ProductStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductServiceTest {

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    ProductService productService;

    User buyer1;
    User buyer2;
    User seller1;
    User seller2;
    Product product1;
    Product product2;

    @BeforeEach
    void setUp() {
        buyer1 = new User();
        buyer1.setUsername("buyer1");
        buyer1.setPassword(bCryptPasswordEncoder.encode("1234"));
        buyer1.setRole("ROLE_USER");
        userRepository.save(buyer1);

        buyer2 = new User();
        buyer2.setUsername("buyer2");
        buyer2.setPassword(bCryptPasswordEncoder.encode("1234"));
        buyer2.setRole("ROLE_USER");
        userRepository.save(buyer2);

        seller1 = new User();
        seller1.setUsername("seller1");
        seller1.setPassword(bCryptPasswordEncoder.encode("1234"));
        seller1.setRole("ROLE_USER");
        userRepository.save(seller1);

        seller2 = new User();
        seller2.setUsername("seller2");
        seller2.setPassword(bCryptPasswordEncoder.encode("1234"));
        seller2.setRole("ROLE_USER");
        userRepository.save(seller2);

        product1 = productRepository.save(Product.builder()
                .name("product1")
                .price(1000)
                .quantity(10)
                .seller(seller1)
                .status(ProductStatus.FOR_SALE)
                .build());

        product2 = productRepository.save(Product.builder()
                .name("product2")
                .price(1000)
                .quantity(10)
                .seller(seller2)
                .status(ProductStatus.FOR_SALE)
                .build());
    }


    @Test
    @DisplayName("제품 등록 - 성공")
    @Transactional
    void saveProductTest_성공() {
        // given
        ProductCreateRequest productCreateRequest = ProductCreateRequest.builder()
                        .name("product1")
                        .price(1000)
                        .quantity(10)
                        .build();

        // when
        ProductResponse productResponse = productService.saveProduct(seller1.getId(), productCreateRequest);
        Product savedProduct = productRepository.findById(productResponse.getId()).orElse(null);

        // then
        Assertions.assertNotNull(savedProduct);
        Assertions.assertEquals(productCreateRequest.getName(), savedProduct.getName());
        Assertions.assertEquals(productCreateRequest.getPrice(), savedProduct.getPrice());
        Assertions.assertEquals(productCreateRequest.getQuantity(), savedProduct.getQuantity());
        Assertions.assertEquals(seller1.getId(), savedProduct.getSeller().getId());
    }

    @Test
    @DisplayName("제품 등록 - 수량을 1개 미만으로 등록한 경우")
    @Transactional
    void saveProductTest_수량을_1개_미만으로_등록한_경우() {
        // given
        ProductCreateRequest productCreateRequest = ProductCreateRequest.builder()
                .name("product1")
                .price(1000)
                .quantity(0)
                .build();

        // when
        CustomException customException = Assertions.assertThrows(CustomException.class, () -> {
            productService.saveProduct(seller1.getId(), productCreateRequest);
        });

        // then
        Assertions.assertEquals(customException.getErrorCode(), ErrorCode.PRODUCT_NOT_ENOUGH);
    }

    @Test
    @DisplayName("제품 전체 목록 조회 - 성공")
    @Transactional
    void findAllProductListTest_성공() {
        // given
        int previousSize = productService.findAllProductList().size();

        for (int i = 0; i < 5; i++) {
            Product product = Product.builder()
                    .name("product" + i)
                    .price(1000)
                    .quantity(10)
                    .seller(seller1)
                    .build();

            productRepository.save(product);
        }

        // when
        List<ProductResponse> productList = productService.findAllProductList();

        // then
        Assertions.assertEquals(previousSize + 5, productList.size());
    }

    @Test
    @DisplayName("제품 상세 조회 - 비회원 성공")
    @Transactional
    void findDetailProductTest_비회원_성공() {
        // when
        ProductResponse findProduct = productService.findDetailProduct(product1.getId());

        // then
        Assertions.assertNotNull(findProduct);
        Assertions.assertEquals(product1.getName(), findProduct.getName());
        Assertions.assertEquals(product1.getPrice(), findProduct.getPrice());
        Assertions.assertEquals(product1.getQuantity(), findProduct.getQuantity());
        Assertions.assertEquals(seller1.getId(), findProduct.getSeller().getId());
    }

    @Test
    @DisplayName("제품 상세 조회 - 거래내역 포함할 경우 성공")
    @Transactional
    void findDetailProductTest_회원_거래내역_포함_성공() {
        // given
        Order order = orderRepository.save(Order.builder()
                        .product(product1)
                        .seller(product1.getSeller())
                        .buyer(buyer1)
                        .confirmedPrice(product1.getPrice())
                        .quantity(9)
                        .status(OrderStatus.PENDING)
                        .build());

        // when
        ProductDetailResponse findProduct = productService.findDetailProductWithTransaction(seller1.getId(), product1.getId());

        // then
        Assertions.assertNotNull(findProduct);
        Assertions.assertEquals(product1.getName(), findProduct.getProduct().getName());
        Assertions.assertEquals(product1.getPrice(), findProduct.getProduct().getPrice());
        Assertions.assertEquals(product1.getQuantity(), findProduct.getProduct().getQuantity());
        Assertions.assertEquals(product1.getSeller().getId(), findProduct.getProduct().getSeller().getId());

        List<TransactionResponse> transactions = findProduct.getTransactions();
        Assertions.assertNotNull(transactions);
        Assertions.assertEquals(1, transactions.size());

        TransactionResponse transaction = transactions.get(0);
        Assertions.assertEquals(transaction.getId(), order.getId());
        Assertions.assertEquals(transaction.getQuantity(), order.getQuantity());
        Assertions.assertEquals(transaction.getConfirmedPrice(), order.getConfirmedPrice());
        Assertions.assertEquals(transaction.getOrderStatus(), order.getStatus());
        Assertions.assertEquals(transaction.getBuyer().getId(), order.getBuyer().getId());
    }

    @Test
    @DisplayName("제품 상세 조회 - 거래내역 없을 경우 성공")
    @Transactional
    void findDetailProductTest_회원_거래내역_없을_경우_성공() {
        // when
        ProductDetailResponse findDetailProduct = productService.findDetailProductWithTransaction(seller1.getId(), product1.getId());

        // then
        Assertions.assertNotNull(findDetailProduct);
        Assertions.assertEquals(product1.getName(), findDetailProduct.getProduct().getName());
        Assertions.assertEquals(product1.getPrice(), findDetailProduct.getProduct().getPrice());
        Assertions.assertEquals(product1.getQuantity(), findDetailProduct.getProduct().getQuantity());
        Assertions.assertEquals(seller1.getId(), findDetailProduct.getProduct().getSeller().getId());

        Assertions.assertNotNull(findDetailProduct.getTransactions());
        Assertions.assertTrue(findDetailProduct.getTransactions().isEmpty());
    }

    @Test
    @DisplayName("내가 등록한 제품 조회 - 성공")
    @Transactional
    void findMyProductListTest_성공() {
        // given
        for (int i = 0; i < 5; i++) {
            Product product = Product.builder()
                    .name("product" + i)
                    .price(1000)
                    .quantity(10)
                    .seller(seller1)
                    .build();

            productRepository.save(product);
        }

        // when
        List<ProductResponse> myProductList = productService.findMyProductList(seller1.getId());

        // then
        Assertions.assertEquals(6, myProductList.size());
    }

    @Test
    @DisplayName("제품 가격 수정 - 성공")
    @Transactional
    void modifyProductPriceTest_성공() {
        // given
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setId(product1.getId());
        request.setPrice(2000);

        // when
        ProductResponse updatedProduct = productService.modifyProductPrice(seller1.getId(), request);

        // then
        Assertions.assertNotNull(updatedProduct);
        Assertions.assertEquals(product1.getId(), updatedProduct.getId());
        Assertions.assertEquals(request.getPrice(), updatedProduct.getPrice());
    }

    @Test
    @DisplayName("제품 가격 수정 - 판매자가 아닌 경우")
    @Transactional
    void modifyProductPriceTest_판매자가_아닌_경우() {
        // given
        User seller2 = new User();
        seller2.setUsername("seller2");
        seller2.setPassword(bCryptPasswordEncoder.encode("1234"));
        seller2.setRole("ROLE_USER");
        userRepository.save(seller2);

        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setId(product1.getId());
        request.setPrice(2000);

        // when
        CustomException customException = Assertions.assertThrows(CustomException.class, () -> {
            productService.modifyProductPrice(seller2.getId(), request);
        });

        // then
       Assertions.assertEquals(customException.getErrorCode(), ErrorCode.USER_NOT_SELLER);
    }

    @Test
    @DisplayName("제품 가격 수정 - 똑같은 가격으로 수정을 시도했을 경우")
    @Transactional
    void modifyProductPriceTest_이전_가격과_똑같은_경우() {
        // given
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setId(product1.getId());
        request.setPrice(1000);

        // when
        CustomException customException = Assertions.assertThrows(CustomException.class, () -> {
            productService.modifyProductPrice(seller1.getId(), request);
        });

        // then
        Assertions.assertEquals(customException.getErrorCode(), ErrorCode.SAME_AS_PREVIOUS_PRICE);
    }

    @Test
    @DisplayName("제품 가격 수정 - 이미 판매완료된 제품일 경우")
    @Transactional
    void modifyProductPriceTest_판매완료된_제품일_경우() {
        // given
        product1.modifyStatus(ProductStatus.SOLD_OUT);

        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setId(product1.getId());
        request.setPrice(2000);

        // when
        CustomException customException = Assertions.assertThrows(CustomException.class, () -> {
            productService.modifyProductPrice(seller1.getId(), request);
        });

        // then
        Assertions.assertEquals(customException.getErrorCode(), ErrorCode.PRODUCT_SOLD_OUT);
    }

    @Test
    @DisplayName("구매한 제품 목록 조회 - 성공")
    @Transactional
    void findOrderedProductListTest_성공() {
        // when
        List<ProductResponse> confirmedProductList = productService.findOrderedProductList(buyer2.getId());

        // then
        Assertions.assertEquals(confirmedProductList.size(), 1);
    }

    @Test
    @DisplayName("예약중인 제품 목록 조회 - 성공")
    @Transactional
    void findReservedProductListTest_성공() {
        // given
        orderRepository.save(Order.builder()
                .quantity(5)
                .confirmedPrice(product1.getPrice())
                .product(product1)
                .seller(seller1)
                .buyer(buyer1)
                .status(OrderStatus.PENDING)
                .build());

        product1.modifyQuantity(5);

        orderRepository.save(Order.builder()
                .quantity(5)
                .confirmedPrice(product2.getPrice())
                .product(product2)
                .seller(seller2)
                .buyer(buyer1)
                .status(OrderStatus.APPROVED)
                .build());

        product2.modifyQuantity(5);

        // when
        List<ProductResponse> reservedProductList = productService.findReservedProductList(buyer1.getId());

        // then
        Assertions.assertEquals(2, reservedProductList.size());
    }

}