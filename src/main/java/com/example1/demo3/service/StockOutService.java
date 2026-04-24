package com.example1.demo3.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example1.demo3.entity.Maker;
import com.example1.demo3.entity.Product;
import com.example1.demo3.entity.StockDetail;
import com.example1.demo3.entity.StockHistory;
import com.example1.demo3.repository.MakerRepository;
import com.example1.demo3.repository.ProductRepository;
import com.example1.demo3.repository.StockDetailRepository;
import com.example1.demo3.repository.StockHistoryRepository;

@Service
public class StockOutService {
    private final ProductRepository productRepository;
    private final StockHistoryRepository stockHistoryRepository;
    private final StockDetailRepository stockDetailRepository;
    private final MakerRepository makerRepository;


    public StockOutService(ProductRepository productRepository, StockHistoryRepository stockHistoryRepository,
        StockDetailRepository stockDetailRepository, MakerRepository makerRepository) {
        this.productRepository = productRepository;
        this.stockHistoryRepository = stockHistoryRepository;
        this.stockDetailRepository = stockDetailRepository;
        this.makerRepository = makerRepository;
    }

    @Transactional
    public void outStock(Integer productId, Integer quantity, String unit, String category, Integer makerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("商品が見つかりません"));

        // 在庫不足チェック
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("在庫が不足しています");
        }

        // 在庫更新
        int afterStock = product.getStock() - quantity;
        product.setStock(afterStock);
        productRepository.save(product);

        // 内訳の更新
        StockDetail detail = stockDetailRepository.findByProductIdAndMakerId(productId, makerId)
                .orElseThrow(() -> new IllegalArgumentException("内訳が見つかりません"));

        int afterDetailStock = detail.getQuantity() - quantity;
        detail.setQuantity(afterDetailStock);
        stockDetailRepository.save(detail);

        // 履歴保存
        Maker maker = makerRepository.findById(makerId).orElseThrow(() -> new IllegalArgumentException("取引先が見つかりません"));

        StockHistory history = new StockHistory();
        history.setProduct(product);
        history.setQuantity(quantity);
        history.setType("OUT");
        history.setUnit(unit);
        history.setCategory(category);
        history.setStock(afterStock);
        history.setMaker(maker);

        stockHistoryRepository.save(history);
    }
}
