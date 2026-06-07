package com.idp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class YahooMarketDataService implements MarketDataService {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/126 Safari/537.36";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final boolean enabled;

    public YahooMarketDataService(ObjectMapper objectMapper, @Value("${idp.market-data.enabled:true}") boolean enabled) {
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();
    }

    @Override
    public Optional<MarketQuote> quote(String symbol) {
        if (!enabled) {
            return Optional.empty();
        }
        try {
            JsonNode meta = chart(symbol, "1d", "1d").path("meta");
            BigDecimal price = decimal(meta, "regularMarketPrice");
            BigDecimal previousClose = decimal(meta, "chartPreviousClose");
            BigDecimal change = price != null && previousClose != null ? price.subtract(previousClose) : null;
            BigDecimal percentChange = change != null && previousClose != null && previousClose.compareTo(BigDecimal.ZERO) != 0
                ? change.divide(previousClose, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : null;
            return Optional.of(new MarketQuote(
                symbol,
                price,
                change,
                percentChange,
                meta.path("regularMarketTime").isNumber() ? meta.path("regularMarketTime").asLong() : null,
                meta.path("marketState").isTextual() ? meta.path("marketState").asText() : null,
                meta.path("regularMarketVolume").isNumber() ? meta.path("regularMarketVolume").asLong() : null
            ));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (RuntimeException | IOException ex) {
            return Optional.empty();
        }
    }

    @Override
    public List<MarketHistoryPoint> history(String symbol, String range) {
        if (!enabled) {
            return List.of();
        }
        try {
            JsonNode result = chart(symbol, normalizeRange(range), "1d");
            JsonNode timestamps = result.path("timestamp");
            JsonNode quote = result.path("indicators").path("quote").path(0);
            JsonNode opens = quote.path("open");
            JsonNode highs = quote.path("high");
            JsonNode lows = quote.path("low");
            JsonNode closes = quote.path("close");
            JsonNode volumes = quote.path("volume");

            List<MarketHistoryPoint> points = new ArrayList<>();
            for (int i = 0; i < timestamps.size(); i++) {
                if (timestamps.path(i).isNumber() && closes.path(i).isNumber()) {
                    BigDecimal open = opens.path(i).isNumber() ? opens.path(i).decimalValue() : closes.path(i).decimalValue();
                    BigDecimal high = highs.path(i).isNumber() ? highs.path(i).decimalValue() : closes.path(i).decimalValue();
                    BigDecimal low = lows.path(i).isNumber() ? lows.path(i).decimalValue() : closes.path(i).decimalValue();
                    BigDecimal close = closes.path(i).decimalValue();
                    Long volume = volumes.path(i).isNumber() ? volumes.path(i).asLong() : null;

                    points.add(new MarketHistoryPoint(
                        timestamps.path(i).asLong(),
                        open,
                        high,
                        low,
                        close,
                        volume
                    ));
                }
            }
            return points;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return List.of();
        } catch (RuntimeException | IOException ex) {
            return List.of();
        }
    }

    private JsonNode chart(String rawSymbol, String range, String interval) throws IOException, InterruptedException {
        String symbol = UriUtils.encodePathSegment(rawSymbol.trim().toUpperCase(Locale.US), StandardCharsets.UTF_8);
        URI uri = URI.create("https://query2.finance.yahoo.com/v8/finance/chart/" + symbol + "?interval=" + interval + "&range=" + range + "&includePrePost=true");
        HttpRequest request = HttpRequest.newBuilder(uri)
            .timeout(Duration.ofSeconds(8))
            .header("User-Agent", USER_AGENT)
            .GET()
            .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Yahoo chart request failed with status " + response.statusCode());
        }
        JsonNode result = objectMapper.readTree(response.body()).path("chart").path("result").path(0);
        if (result.isMissingNode()) {
            throw new IOException("Yahoo chart response missing result");
        }
        return result;
    }

    private String normalizeRange(String range) {
        return switch (range) {
            case "1w" -> "5d";
            case "1mo", "3mo", "6mo", "1y", "2y", "5y" -> range;
            case "3y", "4y" -> "5y";
            default -> "1mo";
        };
    }

    private BigDecimal decimal(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isNumber() ? value.decimalValue() : null;
    }
}
