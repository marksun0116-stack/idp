package com.idp.controller;

import com.idp.model.DecisionType;
import com.idp.model.InvestmentDecision;
import com.idp.model.InvestmentDecisionAlert;
import com.idp.model.InvestmentDecisionAlert.AlertConditionType;
import com.idp.service.InvestmentDecisionService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/investment-decisions")
public class InvestmentDecisionController {

    private final InvestmentDecisionService decisionService;

    public InvestmentDecisionController(InvestmentDecisionService decisionService) {
        this.decisionService = decisionService;
    }

    /**
     * POST /api/decisions/manual — Create manual decision from Investment section
     * User specifies price; can be added anytime
     */
    @PostMapping("/manual")
    public ResponseEntity<?> createManualDecision(
            Authentication auth,
            @RequestBody Map<String, Object> request) {

        String userId = auth.getName();
        String symbol = (String) request.get("symbol");
        String actionStr = (String) request.get("action");
        Object quantityObj = request.get("quantity");
        Object priceObj = request.get("price");
        String transactionDateStr = (String) request.get("transaction_date");

        if (symbol == null || actionStr == null || quantityObj == null || priceObj == null || transactionDateStr == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Missing required fields: symbol, action, quantity, price, transaction_date"));
        }

        try {
            BigDecimal quantity = new BigDecimal(quantityObj.toString());
            BigDecimal price = new BigDecimal(priceObj.toString());
            LocalDate transactionDate = LocalDate.parse(transactionDateStr);
            DecisionType action = DecisionType.fromValue(actionStr);

            InvestmentDecision decision = decisionService.createManualDecision(
                userId, symbol, action, quantity, price, transactionDate);

            return ResponseEntity.status(HttpStatus.CREATED).body(decision);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid request format: " + e.getMessage()));
        }
    }

    /**
     * POST /api/decisions/auto — Create automatic decision from Strategy execution
     * System uses latest market price; user cannot override
     */
    @PostMapping("/auto")
    public ResponseEntity<?> createAutoDecision(
            Authentication auth,
            @RequestBody Map<String, Object> request) {

        String userId = auth.getName();
        String symbol = (String) request.get("symbol");
        String actionStr = (String) request.get("action");
        Object quantityObj = request.get("quantity");
        Object latestPriceObj = request.get("latest_price");
        String transactionDateStr = (String) request.get("transaction_date");

        if (symbol == null || actionStr == null || quantityObj == null || latestPriceObj == null || transactionDateStr == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Missing required fields: symbol, action, quantity, latest_price, transaction_date"));
        }

        try {
            BigDecimal quantity = new BigDecimal(quantityObj.toString());
            BigDecimal latestPrice = new BigDecimal(latestPriceObj.toString());
            LocalDate transactionDate = LocalDate.parse(transactionDateStr);
            DecisionType action = DecisionType.fromValue(actionStr);

            InvestmentDecision decision = decisionService.createAutoDecision(
                userId, symbol, action, quantity, latestPrice, transactionDate);

            return ResponseEntity.status(HttpStatus.CREATED).body(decision);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid request format: " + e.getMessage()));
        }
    }

    /**
     * POST /api/decisions — Create decision (legacy endpoint, defaults to manual)
     */
    @PostMapping
    public ResponseEntity<?> createDecision(
            Authentication auth,
            @RequestBody Map<String, Object> request) {

        return createManualDecision(auth, request);
    }

    /**
     * POST /api/decisions/{id}/log-details — Log thesis/evidence/risks
     */
    @PostMapping("/{id}/log-details")
    public ResponseEntity<InvestmentDecision> logDecisionDetails(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        InvestmentDecision decision = decisionService.logDecisionDetails(
            id,
            request.get("thesis"),
            request.get("evidence"),
            request.get("risks"),
            request.get("comments")
        );

        return ResponseEntity.ok(decision);
    }

    /**
     * PUT /api/decisions/{id} — Edit open decision
     */
    @PutMapping("/{id}")
    public ResponseEntity<InvestmentDecision> editDecision(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        InvestmentDecision decision = decisionService.editDecision(
            id,
            (String) request.get("thesis"),
            (String) request.get("evidence"),
            (String) request.get("risks"),
            (String) request.get("comments")
        );

        return ResponseEntity.ok(decision);
    }

    /**
     * POST /api/decisions/{id}/exit-criteria — Add exit criteria alert
     */
    @PostMapping("/{id}/exit-criteria")
    public ResponseEntity<InvestmentDecisionAlert> addExitCriteria(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        String conditionTypeStr = (String) request.get("condition_type");
        BigDecimal conditionValue = new BigDecimal(request.get("condition_value").toString());
        String description = (String) request.get("description");

        AlertConditionType conditionType = AlertConditionType.valueOf(
            conditionTypeStr.toUpperCase().replace("-", "_"));

        InvestmentDecisionAlert alert = decisionService.addExitCriteria(
            id, conditionType, conditionValue, description);

        return ResponseEntity.status(HttpStatus.CREATED).body(alert);
    }

    /**
     * DELETE /api/decisions/{decisionId}/exit-criteria/{alertId} — Remove exit criteria
     */
    @DeleteMapping("/{decisionId}/exit-criteria/{alertId}")
    public ResponseEntity<Void> removeExitCriteria(
            @PathVariable Long decisionId,
            @PathVariable Long alertId) {

        decisionService.removeExitCriteria(alertId);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/decisions/{id}/close — Close decision
     */
    @PostMapping("/{id}/close")
    public ResponseEntity<InvestmentDecision> closeDecision(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        BigDecimal exitPrice = new BigDecimal(request.get("exit_price").toString());
        BigDecimal exitPnl = new BigDecimal(request.get("exit_pnl").toString());
        String closeReason = (String) request.get("close_reason");

        InvestmentDecision decision = decisionService.closeDecision(id, exitPrice, exitPnl, closeReason);

        return ResponseEntity.ok(decision);
    }

    /**
     * GET /api/decisions/{id} — Get single decision with edit history
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDecision(@PathVariable Long id) {
        InvestmentDecision decision = decisionService.getDecision(id);

        var response = Map.of(
            "decision", decision,
            "edit_history", decisionService.getEditHistory(id),
            "alerts", decisionService.getAlerts(id)
        );

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/decisions/journal — Get decision journal (paginated)
     */
    @GetMapping("/journal")
    public ResponseEntity<Page<InvestmentDecision>> getJournal(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String userId = auth.getName();
        Page<InvestmentDecision> decisions = decisionService.getDecisionJournal(userId, page, size);

        return ResponseEntity.ok(decisions);
    }

    /**
     * GET /api/decisions/symbol/{symbol} — Get decisions for a symbol
     */
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<java.util.List<InvestmentDecision>> getSymbolDecisions(
            Authentication auth,
            @PathVariable String symbol) {

        String userId = auth.getName();
        var decisions = decisionService.getSymbolDecisions(userId, symbol);

        return ResponseEntity.ok(decisions);
    }

    /**
     * GET /api/decisions/open — Get open decisions
     */
    @GetMapping("/open")
    public ResponseEntity<java.util.List<InvestmentDecision>> getOpenDecisions(Authentication auth) {
        String userId = auth.getName();
        var decisions = decisionService.getOpenDecisions(userId);

        return ResponseEntity.ok(decisions);
    }

    /**
     * GET /api/decisions/pending-alerts-count — Get count of pending alerts
     */
    @GetMapping("/pending-alerts-count")
    public ResponseEntity<Map<String, Long>> getPendingAlertsCount(Authentication auth) {
        String userId = auth.getName();
        long count = decisionService.getPendingAlertCount(userId);

        return ResponseEntity.ok(Map.of("pending_alerts", count));
    }

    /**
     * GET /api/decisions — Get all decisions for current user
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllDecisions(Authentication auth) {
        String userId = auth.getName();
        var decisions = decisionService.getAllDecisions(userId);

        return ResponseEntity.ok(Map.of("decisions", decisions));
    }
}
