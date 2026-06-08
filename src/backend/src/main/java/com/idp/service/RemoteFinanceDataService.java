package com.idp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Primary
public class RemoteFinanceDataService implements MarketDataService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String financeDataBaseUrl;
    private final boolean enabled;

    public RemoteFinanceDataService(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${finance-data.base-url:http://localhost:8082}") String financeDataBaseUrl,
            @Value("${idp.market-data.enabled:true}") boolean enabled) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.financeDataBaseUrl = financeDataBaseUrl;
        this.enabled = enabled;
    }

    @Override
    public Optional<MarketQuote> quote(String symbol) {
        if (!enabled) {
            return Optional.empty();
        }
        try {
            String url = financeDataBaseUrl + "/api/finance/quote?symbols=" + symbol;
            String response = restTemplate.getForObject(url, String.class);
            if (response == null) {
                return Optional.empty();
            }
            JsonNode root = objectMapper.readTree(response);
            JsonNode quotes = root.path("quotes");
            if (quotes.isMissingNode() || !quotes.isObject()) {
                return Optional.empty();
            }
            JsonNode quote = quotes.path(symbol);
            if (quote.isMissingNode()) {
                return Optional.empty();
            }
            return Optional.of(new MarketQuote(
                symbol,
                decimal(quote, "lastPrice"),
                decimal(quote, "change"),
                decimal(quote, "percentChange"),
                quote.path("marketTime").isNumber() ? quote.path("marketTime").asLong() : null,
                quote.path("marketState").isTextual() ? quote.path("marketState").asText() : null,
                quote.path("volume").isNumber() ? quote.path("volume").asLong() : null
            ));
        } catch (RuntimeException | java.io.IOException ex) {
            return Optional.empty();
        }
    }

    @Override
    public List<MarketHistoryPoint> history(String symbol, String range) {
        if (!enabled) {
            return List.of();
        }
        try {
            String url = financeDataBaseUrl + "/api/finance/history/" + symbol + "?range=" + range;
            String response = restTemplate.getForObject(url, String.class);
            if (response == null) {
                return List.of();
            }
            JsonNode root = objectMapper.readTree(response);
            JsonNode bars = root.path("bars");
            if (bars.isMissingNode() || !bars.isArray()) {
                return List.of();
            }
            List<MarketHistoryPoint> points = new ArrayList<>();
            for (JsonNode bar : bars) {
                points.add(new MarketHistoryPoint(
                    bar.path("timestamp").asLong(),
                    decimal(bar, "open"),
                    decimal(bar, "high"),
                    decimal(bar, "low"),
                    decimal(bar, "close"),
                    bar.path("volume").isNumber() ? bar.path("volume").asLong() : null
                ));
            }
            return points;
        } catch (RuntimeException | java.io.IOException ex) {
            return List.of();
        }
    }

    private BigDecimal decimal(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isNumber() ? value.decimalValue() : null;
    }
}
