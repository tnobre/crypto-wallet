package com.crypto.wallet.engine;

import com.crypto.wallet.coincap.dto.CoinCapAsset;
import com.crypto.wallet.coincap.gateway.CoinCapGateway;
import com.crypto.wallet.engine.dto.WalletStatus;
import com.crypto.wallet.locker.WalletStatusLocker;
import com.crypto.wallet.walletparser.WalletParser;
import com.crypto.wallet.walletparser.dto.CryptoAsset;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.crypto.wallet.config.AppConfig.getThreadPoolSize;
import static com.crypto.wallet.config.AppConfig.getThreadPoolTerminationTimeout;
import static java.math.RoundingMode.HALF_UP;
import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public class Engine {
    private final ExecutorService threadPool;
    private final CoinCapGateway coinCapGateway;
    private final WalletParser walletParser;
    private final WalletStatusLocker locker;

    public Engine(ExecutorService threadPool, CoinCapGateway coinCapGateway, WalletParser walletParser, WalletStatusLocker locker) {
        this.threadPool = threadPool;
        this.coinCapGateway = coinCapGateway;
        this.walletParser = walletParser;
        this.locker = locker;
    }

    public WalletStatus execute() {
        final Map<String, CryptoAsset> wallet = walletParser.getWallet();
        log.debug("# Wallet {}", wallet.size());

        final List<CoinCapAsset> coinCapAssets = getCoinCapAssets(wallet);
        log.debug("# Assets {}", coinCapAssets.size());

        if (wallet.size() > coinCapAssets.size())
            log.info("Wallet assets {} is bigger than Coin cap assets found {}", wallet.size(), coinCapAssets.size());

        coinCapAssets.forEach(asset -> evaluateNewAssetPrice(asset, wallet));

        shutdownThreadPool();

        WalletStatus status = locker.readResourceAndUnlock();
        log.info("{}", status);

        return status;
    }

    private void evaluateNewAssetPrice(final CoinCapAsset asset, final Map<String, CryptoAsset> wallet) {
         threadPool.submit(() -> {
             try {
                 log.info("Submitted request asset {}", asset.getSymbol());

                 BigDecimal assetPrice = coinCapGateway.getAsset(asset.getId()).getAssetPrice();

                 log.debug("{} {}", asset.getSymbol(), assetPrice);

                 CryptoAsset walletAsset = wallet.get(asset.getSymbol());
                 BigDecimal oldTotal = walletAsset.getQuantity().multiply(walletAsset.getPrice());

                 BigDecimal currentTotal = walletAsset.getQuantity().multiply(assetPrice);
                 WalletStatus status = locker.readResource();
                 addToWalletTotal(status, currentTotal);

                 BigDecimal currentPerformance = currentTotal.divide(oldTotal, 20, HALF_UP);
                 checkBestPerformance(asset.getSymbol(), status, currentPerformance);
                 checkWorstPerformance(asset.getSymbol(), status, currentPerformance);

                 locker.unlockReadResource();
                 locker.writeResource(status);

                 log.debug("{} current total {}", asset.getSymbol(), currentTotal);
             } catch (Exception e) {
                 log.error("Asset {} not calculated correctly due to: {}", asset.getSymbol(), e.getMessage());
             }
        });
    }

    private void addToWalletTotal(WalletStatus status, BigDecimal currentTotal) {
        status.getTotal().updateAndGet(currentValue -> currentValue.add(currentTotal));
        log.debug("total {}", status.getTotal().get());
    }

    private void checkWorstPerformance(String assetSymbol, WalletStatus status, BigDecimal currentPerformance) {
        if (status.getWorstPerformance().get().compareTo(currentPerformance) > 0) {
            status.getWorstPerformance().set(currentPerformance);
            status.setWorstAsset(assetSymbol);
        }
    }
    private void checkBestPerformance(String assetSymbol, WalletStatus status, BigDecimal currentPerformance) {
        if (status.getBestPerformance().get().compareTo(currentPerformance) < 0) {
            status.getBestPerformance().set(currentPerformance);
            status.setBestAsset(assetSymbol);
        }
    }

    private List<CoinCapAsset> getCoinCapAssets(Map<String, CryptoAsset> wallet) {
        final List<CoinCapAsset> coinCapAssets = coinCapGateway.getAllAssets();
        return coinCapAssets.stream().filter(a -> wallet.containsKey(a.getSymbol())).toList();
    }

    private void shutdownThreadPool() {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(getThreadPoolTerminationTimeout(), SECONDS))
                threadPool.shutdownNow();
        } catch (InterruptedException e) {
            log.error("Thread pool was not closed correctly {} ", e.getMessage());
        }
    }

    public static void main(String[] args) {
        final ExecutorService threadPool = Executors.newFixedThreadPool(getThreadPoolSize());
        final CoinCapGateway coinCapGateway = new CoinCapGateway();
        final WalletParser walletParser = new WalletParser();
        final WalletStatusLocker locker = new WalletStatusLocker();
        Engine engine = new Engine(threadPool, coinCapGateway, walletParser, locker);

        long beginning = System.currentTimeMillis();
        engine.execute();
        long end = System.currentTimeMillis();
        log.info("Completed in: {} s", (end - beginning) / 1000.0 );
    }

}