package com.project.spring.service.impl;

import com.project.spring.Specifications.ProductSpecification;
import com.project.spring.dto.PaginationProductResponse;
import com.project.spring.model.Category;
import com.project.spring.model.Manufacture;
import com.project.spring.model.Product;
import com.project.spring.repositories.ProductRepository;
import com.project.spring.service.ProductService;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
public class ProductServiceImpl implements ProductService {
    @Autowired
    ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public PaginationProductResponse getAllProduct(Pageable pageable) {
        List<Product> products = productRepository.findAll(pageable).getContent();
        return PaginationProductResponse.builder()
                .products(products)
                .numberOfItems(Long.parseLong(String.valueOf(products.size())))
                .numberOfPages(pageable.getPageNumber() + 1)
                .numberTotalPages(productRepository.findAll(pageable).getTotalPages())
                .build();
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Product addOrUpdate(Product product) {
        return productRepository.save(product);
    }

    @Override
    public List<Product> findByProductName(String name) {
        return productRepository.findByName(name);
    }

    @Override
    public boolean deleteProductById(Long id) {
        boolean exist = productRepository.existsById(id);
        if (exist) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<Product> getAllProductByCategory(Long id) {
        return this.productRepository.findProductByCategory_Id(id);
    }

    @Override
    public List<Product> getAllProductByManufacture(Long id) {
        return this.productRepository.findAllProductByManufacture(id);
    }

    //    Filter
    @Override
    public PaginationProductResponse filterProducts(List<Double> price, String color, Category category, Set<Manufacture> manufactureSet, Pageable pageable) {
        Specification<Product> spec = Specification.where(null);
        if (category != null) {
            spec = spec.and(ProductSpecification.hasCategory(category));
        }
        if (manufactureSet != null && manufactureSet.size() >= 1) {
            spec = spec.and(ProductSpecification.hasManufactureSet(manufactureSet));
        }
        if (price.size() == 2) {
            spec = spec.and(ProductSpecification.priceInRange(price.get(0), price.get(1)));
        }
        List<Product> products = productRepository.findAll(spec, pageable).getContent();
        return PaginationProductResponse.builder()
                .products(products)
                .numberOfItems(Long.parseLong(String.valueOf(products.size())))
                .numberOfPages(pageable.getPageNumber() + 1)
                .numberTotalPages((int) Math.ceil((double) productRepository.findAll(spec, pageable).getTotalElements() / pageable.getPageSize()))
                .build();
    }

    //    Search
    @Override
    public PaginationProductResponse searchProducts(String keyword, @Nullable Pageable pageable) {
        int getSizeProducts = this.productRepository.searchProducts(keyword, null).size();
        List<Product> products = this.productRepository.searchProducts(keyword, pageable);
        return PaginationProductResponse.builder()
                .numberOfItems(Long.parseLong(String.valueOf(products.size())))
                .numberOfPages(pageable.getPageNumber() + 1)
                .products(products)
                .numberTotalPages((int) Math.ceil((double) getSizeProducts / pageable.getPageSize()))
                .build();
//        return this.productRepository.searchProducts(keyword);
    }

    /*Count comments of Products by Id */
    @Override
    public Integer countCommentProduct(Long id) {
        return this.productRepository.findCommentCountByProduct_comment(id);
    }

    @Override
    public BigDecimal rating(Long id) {
        List<Integer[]> integer = this.productRepository.findStarByProduct(id);
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (Integer[] o : integer) {
            BigDecimal rating = BigDecimal.valueOf(o[0]);
            BigDecimal occurrences = BigDecimal.valueOf(o[1]);
            sum = sum.add(rating.multiply(occurrences));
            count += o[1];
        }
        try {
            return sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return new BigDecimal(0);
        }
    }

    /* update view */
    @Override
    public void incrementViewCount(Long id){
        Product product = this.productRepository.findProductById(id);
        if(product != null){
            product.setViewCount(product.getViewCount()+1);
            this.productRepository.save(product);
        }
    }

}
//Store

// Specification
//        Specification<Product> specification = ProductSpecification.priceLessThan(price);
//        specification.and(ProductSpecification.categoryLike(category));
//        return productRepository.findAll(specification);
//        Pageable pageable;
//        Page<Product> products = productRepository.findAllByNameContains("123",pageable).and()


//    @Override
//    public List<Product> getAllProduct(Integer pageNo, Integer pageSize, String sortBy, String order) {
//        Sort sort = Sort.by(sortBy);
//        if (order.equals("desc")) {
//            sort = sort.descending();
//        } else {
//            sort = sort.ascending();
//        }
//        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
//        Page<Product> products = productRepository.findAll(pageable);
//        if (products.hasContent()) {
//            return products.getContent();
//        }
//        return new ArrayList<Product>();
//    }

// get all products
//        Sort sort = Sort.by(sortBy);
//        if (order.equals("desc")) {
//            sort = sort.descending();
//        } else {
//            sort = sort.ascending();
//        }
//        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
//        Page<Product> products = productRepository.findAll(pageable);
//        List<Product> rs = new ArrayList<Product>();
//        if (products.hasContent()) {
//            List<Product> products1 = products.getContent();
//            for (Product product : products1) {
//                if (product.getCategory().getId().equals(id)) {
//                    rs.add(product);
//                }
//            }
//        }