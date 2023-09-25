package com.crypto.wallet.coincap.gateway;

import com.crypto.wallet.coincap.dto.CoinCapAsset;
import com.crypto.wallet.coincap.dto.CoinCapAssetHistoryResponse;
import com.crypto.wallet.coincap.dto.CoinCapAssetsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;

import static com.crypto.wallet.config.AppConfig.getCoinCapHistoryEnd;
import static com.crypto.wallet.config.AppConfig.getCoinCapHistoryInterval;
import static com.crypto.wallet.config.AppConfig.getCoinCapHistoryStart;
import static com.crypto.wallet.config.AppConfig.getCoincapApiBaseUrl;

@Slf4j
@NoArgsConstructor
public class CoinCapGateway {
    public List<CoinCapAsset> getAllAssets() {
        try {
            URI uri = new URI("https", getCoincapApiBaseUrl(), "/v2/assets", null);
            HttpRequest request = HttpRequest.newBuilder().uri(uri).build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, BodyHandlers.ofString());
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(response.body(), CoinCapAssetsResponse.class).getData();
        } catch (Exception e) {
            log.error("Cant get coincap assets {}", e.getMessage());
            System.exit(-1);
        }
        return null;
    }

    public CoinCapAssetHistoryResponse getAsset(String id) {
        try {
            URI uri = new URI("https", getCoincapApiBaseUrl(), "/v2/assets/" + id + "/history",
                    "interval=" + getCoinCapHistoryInterval() + "&start="+ getCoinCapHistoryStart() +
                            "&end=" + getCoinCapHistoryEnd(), null);
            HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, BodyHandlers.ofString());
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(response.body(), CoinCapAssetHistoryResponse.class);
        } catch (Exception e) {
            log.error("Cant get coincap history price {}", e.getMessage());
        }
        return null;
    }

}