package com.idp.service;

import com.idp.dto.AddHoldingRequest;
import com.idp.dto.CreateAccountRequest;
import com.idp.dto.HoldingResponse;
import com.idp.dto.PortfolioAccountResponse;
import com.idp.dto.PortfolioSummaryResponse;
import com.idp.dto.UpdateHoldingRequest;
import com.idp.exception.PortfolioConflictException;
import com.idp.exception.PortfolioNotFoundException;
import com.idp.model.DecisionType;
import com.idp.model.Holding;
import com.idp.model.InvestmentAccount;
import com.idp.repository.HoldingRepository;
import com.idp.repository.InvestmentAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PortfolioService {
    private final InvestmentAccountRepository accountRepository;
    private final HoldingRepository holdingRepository;
    private final MarketDataService marketDataService;
    private final InvestmentDecisionService decisionService;

    public PortfolioService(
        InvestmentAccountRepository accountRepository,
        HoldingRepository holdingRepository,
        MarketDataService marketDataService,
        InvestmentDecisionService decisionService
    ) {
        this.accountRepository = accountRepository;
        this.holdingRepository = holdingRepository;
        this.marketDataService = marketDataService;
        this.decisionService = decisionService;
    }

    @Transactional
    public InvestmentAccount createAccount(String ownerId, CreateAccountRequest request) {
        String name = request.name().trim();
        if (accountRepository.existsByOwnerIdAndNameIgnoreCase(ownerId, name)) {
            throw new PortfolioConflictException("Account name already exists for this user");
        }
        InvestmentAccount account = new InvestmentAccount();
        account.setOwnerId(ownerId);
        account.setName(name);
        account.setAccountType(request.accountType());
        return accountRepository.save(account);
    }

    @Transactional
    public void deleteAccount(String ownerId, Long accountId) {
        InvestmentAccount account = findOwnedAccount(ownerId, accountId);
        accountRepository.delete(account);
    }

    @Transactional
    public Holding addHolding(String ownerId, Long accountId, AddHoldingRequest request) {
        InvestmentAccount account = findOwnedAccount(ownerId, accountId);
        String symbol = normalizeSymbol(request.symbol());
        if (holdingRepository.existsByAccountIdAndSymbol(account.getId(), symbol)) {
            throw new PortfolioConflictException("Symbol already held in this account");
        }
        Holding holding = new Holding();
        holding.setAccount(account);
        holding.setSymbol(symbol);
        holding.setShares(request.shares());
        holding.setCostBasis(request.costBasis());
        holding.setPurchaseDate(request.purchaseDate());
        holding.setManualPrice(request.manualPrice());
        holding = holdingRepository.save(holding);

        // Capture BUY decision in investment decision journal
        if (request.costBasis() != null && request.shares().signum() > 0) {
            BigDecimal price = request.costBasis().divide(request.shares(), 2, java.math.RoundingMode.HALF_UP);
            LocalDate transactionDate = request.purchaseDate() != null ? request.purchaseDate() : LocalDate.now();
            decisionService.createManualDecision(
                ownerId, symbol, DecisionType.BUY, request.shares(), price, transactionDate);
        }

        return holding;
    }

    @Transactional
    public Holding updateHolding(String ownerId, Long accountId, Long holdingId, UpdateHoldingRequest request) {
        findOwnedAccount(ownerId, accountId);
        Holding holding = holdingRepository.findByIdAndAccountId(holdingId, accountId)
            .orElseThrow(PortfolioNotFoundException::new);

        BigDecimal sharesDecreased = holding.getShares().subtract(request.shares());
        boolean isSell = sharesDecreased.signum() > 0;

        holding.setShares(request.shares());
        holding.setCostBasis(request.costBasis());
        holding.setPurchaseDate(request.purchaseDate());
        holding.setManualPrice(request.manualPrice());
        holding = holdingRepository.save(holding);

        // Capture SELL decision if shares decreased
        if (isSell && sharesDecreased.signum() > 0) {
            BigDecimal sellPrice = request.costBasis() != null && request.shares().signum() > 0
                ? request.costBasis().divide(request.shares(), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
            LocalDate transactionDate = request.purchaseDate() != null ? request.purchaseDate() : LocalDate.now();
            decisionService.createManualDecision(
                ownerId, holding.getSymbol(), DecisionType.SELL, sharesDecreased, sellPrice, transactionDate);
        }

        return holding;
    }

    @Transactional
    public void deleteHolding(String ownerId, Long accountId, Long holdingId) {
        findOwnedAccount(ownerId, accountId);
        Holding holding = holdingRepository.findByIdAndAccountId(holdingId, accountId)
            .orElseThrow(PortfolioNotFoundException::new);

        // Capture SELL decision for all shares before deletion
        if (holding.getShares().signum() > 0) {
            BigDecimal sellPrice = holding.getCostBasis() != null && holding.getShares().signum() > 0
                ? holding.getCostBasis().divide(holding.getShares(), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
            LocalDate transactionDate = holding.getPurchaseDate() != null ? holding.getPurchaseDate() : LocalDate.now();
            decisionService.createManualDecision(
                ownerId, holding.getSymbol(), DecisionType.SELL, holding.getShares(), sellPrice, transactionDate);
        }

        holdingRepository.delete(holding);
    }

    @Transactional(readOnly = true)
    public PortfolioSummaryResponse summary(String ownerId) {
        List<InvestmentAccount> accounts = accountRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
        Map<String, MarketDataService.MarketQuote> quotesBySymbol = holdingRepository.findByAccountOwnerIdOrderBySymbol(ownerId).stream()
            .filter(holding -> holding.getManualPrice() == null)
            .map(Holding::getSymbol)
            .distinct()
            .map(symbol -> marketDataService.quote(symbol))
            .flatMap(Optional::stream)
            .collect(Collectors.toMap(MarketDataService.MarketQuote::symbol, Function.identity(), (left, right) -> left));

        List<PortfolioAccountResponse> accountResponses = accounts.stream()
            .map(account -> accountResponse(account, quotesBySymbol))
            .toList();

        BigDecimal totalValue = sum(accountResponses.stream().map(PortfolioAccountResponse::value).toList());
        BigDecimal totalCost = sum(accountResponses.stream().map(PortfolioAccountResponse::cost).toList());
        BigDecimal totalGain = totalValue != null && totalCost != null && totalCost.compareTo(BigDecimal.ZERO) > 0
            ? money(totalValue.subtract(totalCost))
            : null;
        BigDecimal dailyGain = sum(accountResponses.stream()
            .flatMap(account -> account.holdings().stream())
            .map(HoldingResponse::dayGain)
            .toList());
        return new PortfolioSummaryResponse(
            totalValue,
            totalCost,
            totalGain,
            percent(totalGain, totalCost),
            dailyGain,
            percent(dailyGain, totalValue == null || dailyGain == null ? null : totalValue.subtract(dailyGain)),
            accountResponses
        );
    }

    public HoldingResponse holdingResponse(Holding holding) {
        return holdingResponse(holding, Map.of());
    }

    private InvestmentAccount findOwnedAccount(String ownerId, Long accountId) {
        return accountRepository.findByIdAndOwnerId(accountId, ownerId)
            .orElseThrow(PortfolioNotFoundException::new);
    }

    private PortfolioAccountResponse accountResponse(InvestmentAccount account, Map<String, MarketDataService.MarketQuote> quotesBySymbol) {
        List<HoldingResponse> holdings = holdingRepository.findByAccountIdOrderBySymbol(account.getId()).stream()
            .map(holding -> holdingResponse(holding, quotesBySymbol))
            .toList();
        BigDecimal value = sum(holdings.stream().map(HoldingResponse::value).toList());
        BigDecimal cost = sum(holdings.stream().map(HoldingResponse::cost).toList());
        BigDecimal gain = value != null && cost != null && cost.compareTo(BigDecimal.ZERO) > 0
            ? money(value.subtract(cost))
            : null;
        return new PortfolioAccountResponse(
            account.getId(),
            account.getName(),
            account.getAccountType(),
            value,
            cost,
            gain,
            percent(gain, cost),
            holdings
        );
    }

    private HoldingResponse holdingResponse(Holding holding, Map<String, MarketDataService.MarketQuote> quotesBySymbol) {
        MarketDataService.MarketQuote quote = quotesBySymbol.get(holding.getSymbol());
        BigDecimal price = holding.getManualPrice() != null
            ? holding.getManualPrice()
            : quote == null ? null : quote.lastPrice();
        BigDecimal value = price == null ? null : money(price.multiply(holding.getShares()));
        BigDecimal cost = holding.getCostBasis() == null ? null : money(holding.getCostBasis().multiply(holding.getShares()));
        BigDecimal gain = value == null || cost == null ? null : money(value.subtract(cost));
        BigDecimal dayChange = holding.getManualPrice() != null || quote == null ? null : quote.change();
        BigDecimal dayChangePct = holding.getManualPrice() != null || quote == null ? null : quote.percentChange();
        BigDecimal dayGain = dayChange == null ? null : money(dayChange.multiply(holding.getShares()));
        return new HoldingResponse(
            holding.getId(),
            holding.getSymbol(),
            null,
            holding.getShares(),
            holding.getCostBasis(),
            holding.getPurchaseDate(),
            holding.getManualPrice(),
            price,
            value,
            cost,
            gain,
            percent(gain, cost),
            dayChange,
            dayChangePct,
            dayGain,
            holding.getManualPrice() != null
        );
    }

    private String normalizeSymbol(String symbol) {
        return symbol.trim().toUpperCase(Locale.US);
    }

    private BigDecimal sum(List<BigDecimal> values) {
        List<BigDecimal> present = values.stream().filter(value -> value != null).toList();
        if (present.isEmpty()) {
            return null;
        }
        return money(present.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private BigDecimal percent(BigDecimal numerator, BigDecimal denominator) {
        if (numerator == null || denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return numerator.multiply(BigDecimal.valueOf(100)).divide(denominator, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
