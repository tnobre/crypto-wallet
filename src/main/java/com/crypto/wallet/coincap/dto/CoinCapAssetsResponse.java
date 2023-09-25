package com.crypto.wallet.coincap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CoinCapAssetsResponse {

    private List<CoinCapAsset> data;

    private long timestamp;

}