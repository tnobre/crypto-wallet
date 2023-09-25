package com.wallet.engine;

import com.crypto.wallet.coincap.dto.AssetPrice;
import com.crypto.wallet.coincap.dto.CoinCapAsset;
import com.crypto.wallet.coincap.dto.CoinCapAssetHistoryResponse;
import com.crypto.wallet.coincap.gateway.CoinCapGateway;
import com.crypto.wallet.engine.Engine;
import com.crypto.wallet.engine.dto.WalletStatus;
import com.crypto.wallet.locker.WalletStatusLocker;
import com.crypto.wallet.walletparser.WalletParser;
import com.crypto.wallet.walletparser.dto.CryptoAsset;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.crypto.wallet.config.AppConfig.getTestMultipleExecutionsNumber;
import static com.crypto.wallet.config.AppConfig.getThreadPoolSize;
import static com.crypto.wallet.util.NumberUtils.twoDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@Slf4j
@TestInstance(Lifecycle.PER_CLASS)
class EngineTest {

    @Mock
    CoinCapGateway coinCapGateway;

    WalletParser walletParser;

    @Mock
    WalletParser mockedWalletParser;

    @BeforeAll
    void setUp() {
        MockitoAnnotations.initMocks(this);
        walletParser = new WalletParser();
    }

    @Test
    void btcAndEthPosition() {
        CryptoAsset btc = CryptoAsset.builder()
                .symbol("BTC")
                .quantity(new BigDecimal("0.12345"))
                .price(new BigDecimal("37870.5058")).build();
        CryptoAsset eth = CryptoAsset.builder()
                .symbol("ETH")
                .quantity(new BigDecimal("4.89532"))
                .price(new BigDecimal("2004.9774")).build();
        when(mockedWalletParser.getWallet()).thenReturn(Map.of(btc.getSymbol(), btc, eth.getSymbol(), eth));

        final ExecutorService threadPool = Executors.newFixedThreadPool(getThreadPoolSize());
        final WalletStatusLocker locker = new WalletStatusLocker();
        Engine engine = new Engine(threadPool, coinCapGateway, mockedWalletParser, locker);
        when(coinCapGateway.getAllAssets()).thenReturn(mockedAssets());

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        when(coinCapGateway.getAsset(idCaptor.capture())).thenAnswer(invocation -> {
            String assetId = invocation.getArgument(0);
            return mockedAssetBtcEth(assetId);
        });

        WalletStatus status = engine.execute();
        log.debug("{}", status);

        assertEquals(new BigDecimal("16984.619452250185052431319"), status.getTotal().get());
        assertEquals(twoDecimal(new BigDecimal("16984.62")), twoDecimal(status.getTotal().get()));
        assertEquals("BTC", status.getBestAsset());
        assertEquals(twoDecimal(new BigDecimal("1.51")), twoDecimal(status.getBestPerformance().get()));
        assertEquals("ETH", status.getWorstAsset());
        assertEquals(twoDecimal(new BigDecimal("1.01")), twoDecimal(status.getWorstPerformance().get()));
    }

    @Test
    void multipleExecutions() {
        long beginning = System.currentTimeMillis();
        for (int i = 0; i < getTestMultipleExecutionsNumber(); i++) {
            engine();
        }
        long end = System.currentTimeMillis();
        log.info("Completed in: {} s", (end - beginning) / 1000.0 );
    }

    void engine() {
        final ExecutorService threadPool = Executors.newFixedThreadPool(getThreadPoolSize());
        final WalletStatusLocker locker = new WalletStatusLocker();
        Engine engine = new Engine(threadPool, coinCapGateway, walletParser, locker);

        when(coinCapGateway.getAllAssets()).thenReturn(mockedAssets());

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        when(coinCapGateway.getAsset(idCaptor.capture())).thenAnswer(invocation -> {
           String assetId = invocation.getArgument(0);
           return mockedAsset(assetId);
        });

        WalletStatus status = engine.execute();

        assertEquals(new BigDecimal("21.5"), status.getTotal().get());
        log.debug("Passed {} {}", new BigDecimal("21.5"), status.getTotal().get());

        assertEquals("ADA", status.getBestAsset());
        assertEquals(twoDecimal(new BigDecimal("2")), twoDecimal(status.getBestPerformance().get()));
        assertEquals("AAVE", status.getWorstAsset());
        assertEquals(twoDecimal(new BigDecimal("0.5")), twoDecimal(status.getWorstPerformance().get()));

        log.debug("{}", status);
    }

    private CoinCapAssetHistoryResponse mockedAsset(String id) {
        if ("ada".equals(id))
            return CoinCapAssetHistoryResponse.builder()
                    .data(List.of(AssetPrice.builder()
                            .priceUsd(new BigDecimal("2"))
                            .build()))
                    .build();

        if ("aave".equals(id))
            return CoinCapAssetHistoryResponse.builder()
                    .data(List.of(AssetPrice.builder()
                            .priceUsd(new BigDecimal("0.5"))
                            .build()))
                    .build();

        return CoinCapAssetHistoryResponse.builder()
                .data(List.of(AssetPrice.builder()
                        .priceUsd(new BigDecimal("1"))
                        .build()))
                .build();
    }

    private CoinCapAssetHistoryResponse mockedAssetBtcEth(String id) {
        if ("btc".equals(id))
            return CoinCapAssetHistoryResponse.builder()
                    .data(List.of(AssetPrice.builder()
                            .priceUsd(new BigDecimal("56999.9728252053067291"))
                            .build()))
                    .build();

        return CoinCapAssetHistoryResponse.builder()
                .data(List.of(AssetPrice.builder()
                        .priceUsd(new BigDecimal("2032.1394325557042107"))
                        .build()))
                .build();
    }

    private List<CoinCapAsset> mockedAssets() {
        return List.of(
                CoinCapAsset.builder().symbol("BTC").id("BTC".toLowerCase()).build(),
                CoinCapAsset.builder().symbol("ETH").id("ETH".toLowerCase()).build(),
                CoinCapAsset.builder().symbol("AAVE").id("AAVE".toLowerCase()).build(),
                CoinCapAsset.builder().symbol("ADA").id("ADA".toLowerCase()).build(),
                CoinCapAsset.builder().symbol("USDT").id("USDT".toLowerCase()).build(),
                CoinCapAsset.builder().symbol("BNB").id("BNB".toLowerCase()).build(),
                CoinCapAsset.builder().symbol("USDC").id("USDC".toLowerCase()).build(),
                CoinCapAsset.builder().symbol("BTCAA").id("BTCAA".toLowerCase()).build(),
                CoinCapAsset.builder().symbol("ETHAA").id("ETHAA".toLowerCase()).build(),
                CoinCapAsset.builder().symbol("AAVEAA").id("AAVEAA".toLowerCase()).build(),
                CoinCapAsset.builder().symbol("ADAAA").id("ADAAA".toLowerCase()).build(),
                CoinCapAsset.builder().symbol("USDTAA").id("USDTAA".toLowerCase()).build(),
                CoinCapAsset.builder().symbol("BNBAA").id("BNBAA".toLowerCase()).build(),
                CoinCapAsset.builder().symbol("USDCAA").id("USDCAA".toLowerCase()).build(),
                CoinCapAsset.builder().symbol("BTCBB").id("BTCBB".toLowerCase()).build(),
                CoinCapAsset.builder().symbol("ETHBB").id("ETHBB".toLowerCase()).build(),
                CoinCapAsset.builder().symbol("AAVEBB").id("AAVEBB".toLowerCase()).build(),
                CoinCapAsset.builder().symbol("ADABB").id("ADABB".toLowerCase()).build(),
                CoinCapAsset.builder().symbol("USDTBB").id("USDTBB".toLowerCase()).build(),
                CoinCapAsset.builder().symbol("BNBBB").id("BNBBB".toLowerCase()).build(),
                CoinCapAsset.builder().symbol("USDCBB").id("USDCBB".toLowerCase()).build()
        );
    }

}