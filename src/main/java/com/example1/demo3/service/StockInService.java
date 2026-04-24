package com.example1.demo3.service;

import org.springframework.stereotype.Service;

import com.example1.demo3.entity.Maker;
import com.example1.demo3.entity.Product;
import com.example1.demo3.entity.StockDetail;
import com.example1.demo3.entity.StockHistory;
import com.example1.demo3.repository.MakerRepository;
import com.example1.demo3.repository.ProductRepository;
import com.example1.demo3.repository.StockDetailRepository;
import com.example1.demo3.repository.StockHistoryRepository;

import jakarta.transaction.Transactional;

@Service
public class StockInService {
    private final ProductRepository productRepository;
    private final StockHistoryRepository stockHistoryRepository;
    private final MakerRepository makerRepository;
    private final StockDetailRepository stockDetailRepository;

    public StockInService(ProductRepository productRepository, StockDetailRepository stockDetailRepository,
                        StockHistoryRepository stockHistoryRepository, MakerRepository makerRepository) {
        this.productRepository = productRepository;
        this.stockHistoryRepository = stockHistoryRepository;
        this.makerRepository = makerRepository;
        this.stockDetailRepository = stockDetailRepository;
    }
    // 商品取得
    private Product findProduct(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("商品が見つかりません"));
    }

    // 入庫処理
    @Transactional
    public void inStock(Integer productId, Integer quantity, Integer makerId, String unit, String category) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("商品が見つかりません"));
        Maker maker = makerRepository.findById(makerId).orElseThrow(() -> new IllegalArgumentException("メーカーが見つかりません"));

        //在庫更新
        int afterStock = product.getStock() + quantity;
        product.setStock(afterStock);
        productRepository.save(product);

        //メーカー別在庫の更新
        StockDetail detail = stockDetailRepository.findByProductIdAndMakerId(productId, makerId).orElseGet(() -> {
            StockDetail d = new StockDetail();
            d.setProduct(product);
            d.setMaker(maker);
            d.setQuantity(0);
            return d;
        });

        detail.setQuantity(detail.getQuantity() + quantity);
        stockDetailRepository.save(detail);

        //履歴保存
        StockHistory history = new StockHistory();
        history.setProduct(product);
        history.setQuantity(quantity);
        history.setType("IN");
        history.setMaker(maker);
        history.setUnit(unit);
        history.setCategory(category);
        history.setStock(afterStock);

        stockHistoryRepository.save(history);
    }
    
}
