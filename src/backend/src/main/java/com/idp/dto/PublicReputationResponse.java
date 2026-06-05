package com.idp.dto;

public record PublicReputationResponse(
    Integer decisionQualityScore,
    Integer researchDiscipline,
    Integer riskManagement,
    Integer strategyConsistency
) {
}
