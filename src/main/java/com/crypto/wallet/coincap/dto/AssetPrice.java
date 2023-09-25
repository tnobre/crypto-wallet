package com.crypto.wallet.coincap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AssetPrice {
    private BigDecimal priceUsd;
    private long time;
    private String date;
}