package com.crypto.wallet.walletparser;

import com.crypto.wallet.walletparser.dto.CryptoAsset;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor
public class WalletParser {

    public Map<String, CryptoAsset> getWallet() {
        List<CryptoAsset> cryptoAssets = parseCsvWallet();
        return cryptoAssets.stream().collect(Collectors.toMap(
                CryptoAsset::getSymbol,
                c -> c
        ));
    }

    public List<CryptoAsset> parseCsvWallet() {
        List<CryptoAsset> result = new LinkedList<>();
        try (BufferedReader br = getCsvWallet()) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] record = line.split(",");
                result.add(getCryptoAsset(record));
            }
        } catch (IOException e) {
            log.error("Cant read csv wallet file {}", e.getMessage());
        }
        return result;
    }

    public BufferedReader getCsvWallet() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File csvFile = new File(classLoader.getResource("crypto-wallet-assets.csv").getFile());
            return new BufferedReader(new FileReader(csvFile));
        } catch (IOException e) {
            log.error("Cant open csv wallet file {}", e.getMessage());
            System.exit(-1);
        }
        return null;
    }

    private CryptoAsset getCryptoAsset(String[] record) {
        return CryptoAsset.builder()
                .symbol(record[0])
                .quantity(new BigDecimal(record[1]))
                .price(new BigDecimal(record[2]))
                .build();
    }

}