package com.idp.service;

import com.idp.model.DecisionStatus;
import com.idp.model.DecisionType;
import com.idp.model.InvestmentDecision;
import com.idp.model.InvestmentDecisionAlert;
import com.idp.model.InvestmentDecisionAlert.AlertConditionType;
import com.idp.model.InvestmentDecisionAlert.AlertStatus;
import com.idp.model.InvestmentDecisionEdit;
import com.idp.repository.InvestmentDecisionAlertRepository;
import com.idp.repository.InvestmentDecisionEditRepository;
import com.idp.repository.InvestmentDecisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InvestmentDecisionService.
 * Tests decision creation, editing, closing, and validation.
 */
@ExtendWith(MockitoExtension.class)
class InvestmentDecisionServiceTest {

    @Mock
    private InvestmentDecisionRepository decisionRepository;

    @Mock
    private InvestmentDecisionEditRepository editRepository;

    @Mock
    private InvestmentDecisionAlertRepository alertRepository;

    @InjectMocks
    private InvestmentDecisionService service;

    private String userId = "test-user-123";
    private String symbol = "AAPL";
    private LocalDate transactionDate = LocalDate.of(2026, 6, 7);

    @BeforeEach
    void setUp() {
        // Mocks are automatically reset by MockitoExtension
    }

    @Test
    void testCreateDecision_GeneratesCorrectTitle() {
        // Arrange
        BigDecimal quantity = new BigDecimal("200");
        BigDecimal price = new BigDecimal("150.00");
        when(decisionRepository.findByUserIdAndSymbolAndTransactionDateAndActionAndQuantityAndPrice(
            userId, symbol, transactionDate, DecisionType.BUY, 200, price))
            .thenReturn(Optional.empty());
        when(decisionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        InvestmentDecision decision = service.createDecision(
            userId, symbol, DecisionType.BUY, quantity, price, transactionDate);

        // Assert
        assertEquals("Buy 200 shares of AAPL at $150.00", decision.getTitle());
        assertEquals(DecisionStatus.ACTIVE, decision.getStatus());
        assertEquals(userId, decision.getUserId());
        assertEquals(symbol, decision.getSymbol());
        assertEquals(DecisionType.BUY, decision.getAction());
        verify(decisionRepository, times(1)).save(any());
    }

    @Test
    void testCreateDecision_PreventsDuplicates() {
        // Arrange
        BigDecimal quantity = new BigDecimal("200");
        BigDecimal price = new BigDecimal("150.00");
        InvestmentDecision existing = new InvestmentDecision();
        existing.setSymbol(symbol);
        existing.setAction(DecisionType.BUY);
        when(decisionRepository.findByUserIdAndSymbolAndTransactionDateAndActionAndQuantityAndPrice(
            userId, symbol, transactionDate, DecisionType.BUY, 200, price))
            .thenReturn(Optional.of(existing));

        // Act
        InvestmentDecision decision = service.createDecision(
            userId, symbol, DecisionType.BUY, quantity, price, transactionDate);

        // Assert
        assertEquals(symbol, decision.getSymbol());
        assertEquals(DecisionType.BUY, decision.getAction());
        verify(decisionRepository, never()).save(any());
    }

    @Test
    void testEditDecision_TracksThesisChange() {
        // Arrange
        InvestmentDecision decision = new InvestmentDecision();
        decision.setStatus(DecisionStatus.ACTIVE);
        decision.setThesis("Old thesis");
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(decision));
        when(decisionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(editRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        service.editDecision(1L, "New thesis", null, null, null);

        // Assert
        assertEquals("New thesis", decision.getThesis());
        verify(editRepository, times(1)).save(any());
    }

    @Test
    void testEditDecision_FailsOnClosedDecision() {
        // Arrange
        InvestmentDecision decision = new InvestmentDecision();
        decision.setStatus(DecisionStatus.CLOSED);
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(decision));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            service.editDecision(1L, "New thesis", null, null, null);
        });
        verify(decisionRepository, never()).save(any());
    }

    @Test
    void testAddExitCriteria_CreatesAlert() {
        // Arrange
        InvestmentDecision decision = new InvestmentDecision();
        decision.setStatus(DecisionStatus.ACTIVE);
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(decision));
        when(alertRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        InvestmentDecisionAlert alert = service.addExitCriteria(
            1L, AlertConditionType.PRICE_ABOVE, new BigDecimal("165.00"), "Price ≥ $165 (take profit)");

        // Assert
        assertEquals(AlertConditionType.PRICE_ABOVE, alert.getConditionType());
        assertEquals(new BigDecimal("165.00"), alert.getConditionValue());
        assertEquals(AlertStatus.PENDING, alert.getStatus());
        verify(alertRepository, times(1)).save(any());
    }

    @Test
    void testCloseDecision_SetsStatusAndPnL() {
        // Arrange
        InvestmentDecision decision = new InvestmentDecision();
        decision.setStatus(DecisionStatus.ACTIVE);
        decision.setQuantity(200);
        decision.setPrice(new BigDecimal("150.00"));
        when(decisionRepository.findById(1L)).thenReturn(Optional.of(decision));
        when(alertRepository.findByDecisionId(1L)).thenReturn(java.util.List.of());
        when(decisionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        InvestmentDecision closed = service.closeDecision(
            1L, new BigDecimal("165.00"), new BigDecimal("3000.00"));

        // Assert
        assertEquals(DecisionStatus.CLOSED, closed.getStatus());
        assertEquals(new BigDecimal("165.00"), closed.getExitPrice());
        assertEquals(new BigDecimal("3000.00"), closed.getExitPnl());
        assertNotNull(closed.getClosedAt());
        verify(decisionRepository, times(1)).save(any());
    }

    @Test
    void testTriggerAlert_UpdatesStatus() {
        // Arrange
        InvestmentDecisionAlert alert = new InvestmentDecisionAlert();
        alert.setStatus(AlertStatus.PENDING);
        when(alertRepository.findById(1L)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        InvestmentDecisionAlert triggered = service.triggerAlert(1L, new BigDecimal("165.00"));

        // Assert
        assertEquals(AlertStatus.TRIGGERED, triggered.getStatus());
        assertEquals(new BigDecimal("165.00"), triggered.getTriggeredPrice());
        assertNotNull(triggered.getTriggeredAt());
        verify(alertRepository, times(1)).save(any());
    }

    @Test
    void testSellDecisionTitle() {
        // Arrange
        BigDecimal quantity = new BigDecimal("100");
        when(decisionRepository.findByUserIdAndSymbolAndTransactionDateAndActionAndQuantityAndPrice(
            userId, symbol, transactionDate, DecisionType.SELL, 100, new BigDecimal("160.00")))
            .thenReturn(Optional.empty());
        when(decisionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        InvestmentDecision decision = service.createDecision(
            userId, symbol, DecisionType.SELL, quantity, new BigDecimal("160.00"), transactionDate);

        // Assert
        assertEquals("Sell 100 shares of AAPL at $160.00", decision.getTitle());
    }
}
