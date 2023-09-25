package com.crypto.wallet.walletparser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CryptoAsset {

    private String symbol;

    private BigDecimal quantity;

    private BigDecimal price;

}