package com.idp.service;

import com.idp.dto.DqsResponse;
import com.idp.dto.PublicProfileRequest;
import com.idp.dto.PublicProfileResponse;
import com.idp.dto.PublicReputationResponse;
import com.idp.dto.PublicStrategySummaryResponse;
import com.idp.exception.PublicProfileConflictException;
import com.idp.exception.PublicProfileNotFoundException;
import com.idp.model.PublicProfile;
import com.idp.model.StrategyPortfolio;
import com.idp.model.StrategyVisibility;
import com.idp.repository.PublicProfileRepository;
import com.idp.repository.StrategyPortfolioRepository;
import com.idp.repository.StrategyTrackedSymbolRepository;
import com.idp.repository.StrategyTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class PublicProfileService {
    private static final Pattern HANDLE_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9_-]{2,39}$");
    private static final Set<String> ALLOWED_METRICS = Set.of("dqs", "researchDiscipline", "riskManagement", "strategyConsistency");

    private final PublicProfileRepository publicProfileRepository;
    private final StrategyPortfolioRepository strategyPortfolioRepository;
    private final StrategyTrackedSymbolRepository trackedSymbolRepository;
    private final StrategyTransactionRepository transactionRepository;
    private final AnalyticsService analyticsService;

    public PublicProfileService(
        PublicProfileRepository publicProfileRepository,
        StrategyPortfolioRepository strategyPortfolioRepository,
        StrategyTrackedSymbolRepository trackedSymbolRepository,
        StrategyTransactionRepository transactionRepository,
        AnalyticsService analyticsService
    ) {
        this.publicProfileRepository = publicProfileRepository;
        this.strategyPortfolioRepository = strategyPortfolioRepository;
        this.trackedSymbolRepository = trackedSymbolRepository;
        this.transactionRepository = transactionRepository;
        this.analyticsService = analyticsService;
    }

    @Transactional
    public PublicProfile upsert(String ownerId, PublicProfileRequest request) {
        String handle = normalizeHandle(request.handle());
        if (!HANDLE_PATTERN.matcher(handle).matches()) {
            throw new PublicProfileConflictException("Handle must be 3-40 characters and use letters, numbers, underscores, or hyphens");
        }
        if (publicProfileRepository.existsByHandleAndOwnerIdNot(handle, ownerId)) {
            throw new PublicProfileConflictException("Handle is already in use");
        }

        PublicProfile profile = publicProfileRepository.findByOwnerId(ownerId).orElseGet(PublicProfile::new);
        profile.setOwnerId(ownerId);
        profile.setHandle(handle);
        profile.setDisplayName(request.displayName().trim());
        profile.setBio(request.bio() == null || request.bio().isBlank() ? null : request.bio().trim());
        profile.setPublishedMetricIds(approvedMetrics(request.publishedMetricIds()));
        return publicProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public PublicProfileResponse getOwned(String ownerId) {
        PublicProfile profile = publicProfileRepository.findByOwnerId(ownerId)
            .orElseThrow(PublicProfileNotFoundException::new);
        return response(profile);
    }

    @Transactional(readOnly = true)
    public PublicProfileResponse getPublic(String rawHandle) {
        PublicProfile profile = publicProfileRepository.findByHandle(normalizeHandle(rawHandle))
            .orElseThrow(PublicProfileNotFoundException::new);
        return response(profile);
    }

    private PublicProfileResponse response(PublicProfile profile) {
        return new PublicProfileResponse(
            profile.getHandle(),
            profile.getDisplayName(),
            profile.getBio(),
            reputation(profile),
            publicStrategies(profile.getOwnerId()),
            profile.getUpdatedAt()
        );
    }

    private PublicReputationResponse reputation(PublicProfile profile) {
        Set<String> metrics = new LinkedHashSet<>(profile.getPublishedMetricIds());
        DqsResponse dqs = analyticsService.dqs(profile.getOwnerId());
        return new PublicReputationResponse(
            metrics.contains("dqs") ? dqs.score() : null,
            metrics.contains("researchDiscipline") ? dqs.components().researchQuality().score() : null,
            metrics.contains("riskManagement") ? dqs.components().riskManagement().score() : null,
            metrics.contains("strategyConsistency") ? dqs.components().strategyConsistency().score() : null
        );
    }

    private List<PublicStrategySummaryResponse> publicStrategies(String ownerId) {
        List<StrategyPortfolio> strategies = strategyPortfolioRepository.findByOwnerIdAndVisibilityOrderByCreatedAtDesc(ownerId, StrategyVisibility.PUBLIC);
        return strategies.stream()
            .map(strategy -> new PublicStrategySummaryResponse(
                strategy.getId(),
                strategy.getName(),
                strategy.getDescription(),
                trackedSymbolRepository.findByStrategyIdAndVisibilityOrderBySymbol(strategy.getId(), com.idp.model.SymbolVisibility.PUBLIC).size(),
                transactionRepository.findByStrategyIdOrderByExecutedAtAsc(strategy.getId()).size(),
                strategy.getCreatedAt()
            ))
            .toList();
    }

    private List<String> approvedMetrics(List<String> rawMetrics) {
        if (rawMetrics == null) {
            return List.of();
        }
        List<String> metrics = new ArrayList<>();
        for (String metric : rawMetrics) {
            if (ALLOWED_METRICS.contains(metric) && !metrics.contains(metric)) {
                metrics.add(metric);
            }
        }
        return metrics;
    }

    private String normalizeHandle(String handle) {
        return handle.trim().toLowerCase(Locale.US);
    }
}
