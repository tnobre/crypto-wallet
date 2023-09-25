package com.crypto.wallet.coincap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CoinCapAssetHistoryResponse {

    private List<AssetPrice> data;

    private long timestamp;

    public BigDecimal getAssetPrice() {
        try {
            return this.data.get(0).getPriceUsd();
        } catch (Exception e) {
            throw new RuntimeException("asset price not found");
        }
    }

}