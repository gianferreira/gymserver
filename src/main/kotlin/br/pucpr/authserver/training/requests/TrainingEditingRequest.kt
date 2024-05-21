package br.pucpr.authserver.training.requests

import jakarta.validation.constraints.Size

data class TrainingEditingRequest(
    val active: Boolean?,

    @field:Size(min = 40)
    val description: String?
)
