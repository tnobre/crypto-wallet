package com.crypto.wallet.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;

import static com.crypto.wallet.util.NumberUtils.twoDecimal;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class WalletStatus {

    public WalletStatus() {
        this.total = new AtomicReference<>(BigDecimal.ZERO);
        this.bestPerformance = new AtomicReference<>(BigDecimal.valueOf(Double.MIN_VALUE));
        this.worstPerformance = new AtomicReference<>(BigDecimal.valueOf(Double.MAX_VALUE));
    }

    private AtomicReference<BigDecimal> total;
    private AtomicReference<BigDecimal> bestPerformance;
    private AtomicReference<BigDecimal> worstPerformance;
    private String bestAsset;
    private String worstAsset;

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        joiner.add("total=" + twoDecimal(this.total.get()))
                .add("best_asset=" + this.bestAsset)
                .add("best_performance=" + twoDecimal(this.bestPerformance.get()))
                .add("worst_asset=" + this.worstAsset)
                .add("worst_performance=" + twoDecimal(this.worstPerformance.get()));
        return joiner.toString();
    }

}