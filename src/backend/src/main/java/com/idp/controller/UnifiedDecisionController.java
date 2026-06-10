package com.idp.controller;

import com.idp.model.Decision;
import com.idp.model.DecisionAlert;
import com.idp.model.DecisionCategory;
import com.idp.model.DecisionType;
import com.idp.service.DecisionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Unified Decision API that handles both Investment and Strategy decisions.
 * All decisions are stored in the same table with decision_category distinguishing the type.
 */
@RestController
@RequestMapping("/api/decisions")
public class UnifiedDecisionController {

    private final DecisionService decisionService;

    public UnifiedDecisionController(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    /**
     * Create an investment decision (buy/sell transaction)
     * POST /api/decisions/investment
     */
    @PostMapping("/investment")
    public ResponseEntity<Decision> createInvestmentDecision(
            Authentication auth,
            @RequestBody Map<String, Object> request) {

        String userId = auth.getName();
        String symbol = (String) request.get("symbol");
        String actionStr = (String) request.get("action");
        Object quantityObj = request.get("quantity");
        Object priceObj = request.get("price");
        String transactionDateStr = (String) request.get("transaction_date");

        if (symbol == null || actionStr == null || quantityObj == null || priceObj == null || transactionDateStr == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            BigDecimal quantity = new BigDecimal(quantityObj.toString());
            BigDecimal price = new BigDecimal(priceObj.toString());
            LocalDate transactionDate = LocalDate.parse(transactionDateStr);
            DecisionType action = DecisionType.fromValue(actionStr);

            Decision decision = decisionService.createInvestmentDecision(
                    userId, symbol, action, quantity.intValue(), price, transactionDate);

            // Log thesis/evidence/risks if provided
            if (request.containsKey("thesis") || request.containsKey("evidence") || request.containsKey("risks")) {
                decisionService.logDecisionDetails(
                        decision.getId(),
                        (String) request.get("thesis"),
                        (String) request.get("evidence"),
                        (String) request.get("risks"),
                        (String) request.get("comments"));
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(decision);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create a strategy decision (investment thesis)
     * POST /api/decisions/strategy
     */
    @PostMapping("/strategy")
    public ResponseEntity<Decision> createStrategyDecision(
            Authentication auth,
            @RequestBody Map<String, Object> request) {

        String userId = auth.getName();
        String symbol = (String) request.get("symbol");
        String decisionTypeStr = (String) request.get("decision_type");
        String title = (String) request.get("title");
        String thesis = (String) request.get("thesis");
        String evidence = (String) request.get("evidence");
        String risks = (String) request.get("risks");
        Integer confidence = request.get("confidence") != null ? ((Number) request.get("confidence")).intValue() : null;
        String timeHorizon = (String) request.get("time_horizon");

        if (symbol == null || decisionTypeStr == null || title == null || thesis == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            DecisionType decisionType = DecisionType.fromValue(decisionTypeStr);
            Decision decision = decisionService.createStrategyDecision(
                    userId, symbol, decisionType, title, thesis, evidence, risks, confidence, timeHorizon);

            return ResponseEntity.status(HttpStatus.CREATED).body(decision);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all decisions for current user
     * GET /api/decisions
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllDecisions(Authentication auth) {
        String userId = auth.getName();
        List<Decision> decisions = decisionService.getAllDecisions(userId);
        return ResponseEntity.ok(Map.of("decisions", decisions));
    }

    /**
     * Get open/active decisions for current user
     * GET /api/decisions/open
     */
    @GetMapping("/open")
    public ResponseEntity<List<Decision>> getOpenDecisions(Authentication auth) {
        String userId = auth.getName();
        List<Decision> decisions = decisionService.getOpenDecisions(userId);
        return ResponseEntity.ok(decisions);
    }

    /**
     * Get decisions by category (investment or strategy)
     * GET /api/decisions?category=INVESTMENT
     */
    @GetMapping(params = "category")
    public ResponseEntity<List<Decision>> getDecisionsByCategory(
            Authentication auth,
            @RequestParam String category) {

        String userId = auth.getName();
        DecisionCategory decisionCategory = DecisionCategory.valueOf(category.toUpperCase());
        List<Decision> decisions = decisionService.getDecisionsByCategory(userId, decisionCategory);
        return ResponseEntity.ok(decisions);
    }

    /**
     * Get decisions for a specific symbol
     * GET /api/decisions/symbol/{symbol}
     */
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<List<Decision>> getSymbolDecisions(
            Authentication auth,
            @PathVariable String symbol) {

        String userId = auth.getName();
        List<Decision> decisions = decisionService.getSymbolDecisions(userId, symbol);
        return ResponseEntity.ok(decisions);
    }

    /**
     * Get a single decision
     * GET /api/decisions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Decision> getDecision(
            @PathVariable Long id,
            Authentication auth) {

        Decision decision = decisionService.getDecision(id);
        // Verify ownership
        if (!decision.getUserId().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(decision);
    }

    /**
     * Log decision details (thesis, evidence, risks)
     * POST /api/decisions/{id}/log-details
     */
    @PostMapping("/{id}/log-details")
    public ResponseEntity<Decision> logDecisionDetails(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication auth) {

        Decision decision = decisionService.getDecision(id);
        if (!decision.getUserId().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Decision updated = decisionService.logDecisionDetails(
                id,
                request.get("thesis"),
                request.get("evidence"),
                request.get("risks"),
                request.get("comments"));

        return ResponseEntity.ok(updated);
    }

    /**
     * Add exit criteria alert
     * POST /api/decisions/{id}/exit-criteria
     */
    @PostMapping("/{id}/exit-criteria")
    public ResponseEntity<DecisionAlert> addExitCriteria(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            Authentication auth) {

        Decision decision = decisionService.getDecision(id);
        if (!decision.getUserId().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            String conditionTypeStr = (String) request.get("condition_type");
            BigDecimal conditionValue = new BigDecimal(request.get("condition_value").toString());
            String description = (String) request.get("description");

            DecisionAlert.AlertConditionType conditionType =
                    DecisionAlert.AlertConditionType.valueOf(conditionTypeStr.toUpperCase().replace("-", "_"));

            DecisionAlert alert = decisionService.addExitCriteria(id, conditionType, conditionValue, description);
            return ResponseEntity.status(HttpStatus.CREATED).body(alert);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Close a decision
     * POST /api/decisions/{id}/close
     */
    @PostMapping("/{id}/close")
    public ResponseEntity<Decision> closeDecision(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            Authentication auth) {

        Decision decision = decisionService.getDecision(id);
        if (!decision.getUserId().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            BigDecimal exitPrice = new BigDecimal(request.get("exit_price").toString());
            BigDecimal exitPnl = new BigDecimal(request.get("exit_pnl").toString());
            String closeReason = (String) request.get("close_reason");

            Decision updated = decisionService.closeDecision(id, exitPrice, exitPnl, closeReason);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
