package com.idp.controller;

import com.idp.dto.AddHoldingRequest;
import com.idp.dto.CreateAccountRequest;
import com.idp.dto.CreateAccountResponse;
import com.idp.dto.HoldingResponse;
import com.idp.dto.PortfolioSummaryResponse;
import com.idp.dto.UpdateHoldingRequest;
import com.idp.model.Holding;
import com.idp.model.InvestmentAccount;
import com.idp.service.PortfolioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {
    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/summary")
    public PortfolioSummaryResponse summary(Authentication authentication) {
        return portfolioService.summary(authentication.getName());
    }

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateAccountResponse createAccount(@Valid @RequestBody CreateAccountRequest request, Authentication authentication) {
        InvestmentAccount account = portfolioService.createAccount(authentication.getName(), request);
        return new CreateAccountResponse(account.getId(), account.getName(), account.getAccountType());
    }

    @DeleteMapping("/accounts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@PathVariable("id") Long id, Authentication authentication) {
        portfolioService.deleteAccount(authentication.getName(), id);
    }

    @PostMapping("/accounts/{accountId}/holdings")
    @ResponseStatus(HttpStatus.CREATED)
    public HoldingResponse addHolding(
        @PathVariable("accountId") Long accountId,
        @Valid @RequestBody AddHoldingRequest request,
        Authentication authentication
    ) {
        Holding holding = portfolioService.addHolding(authentication.getName(), accountId, request);
        return portfolioService.holdingResponse(holding);
    }

    @PutMapping("/accounts/{accountId}/holdings/{id}")
    public HoldingResponse updateHolding(
        @PathVariable("accountId") Long accountId,
        @PathVariable("id") Long id,
        @Valid @RequestBody UpdateHoldingRequest request,
        Authentication authentication
    ) {
        Holding holding = portfolioService.updateHolding(authentication.getName(), accountId, id, request);
        return portfolioService.holdingResponse(holding);
    }

    @DeleteMapping("/accounts/{accountId}/holdings/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHolding(
        @PathVariable("accountId") Long accountId,
        @PathVariable("id") Long id,
        Authentication authentication
    ) {
        portfolioService.deleteHolding(authentication.getName(), accountId, id);
    }
}
