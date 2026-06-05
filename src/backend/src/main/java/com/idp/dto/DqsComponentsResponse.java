package com.idp.dto;

public record DqsComponentsResponse(
    DqsComponentResponse researchQuality,
    DqsComponentResponse decisionDiscipline,
    DqsComponentResponse riskManagement,
    DqsComponentResponse strategyConsistency,
    DqsComponentResponse outcomeQuality
) {
}
