package br.pucpr.authserver.training.requests

import br.pucpr.authserver.training.Training
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class TrainingRequest(
    @field:NotNull
    val memberId: Long?,

    @field:Size(min = 40)
    val description: String?
) {
    fun toTraining(personalId: Long?) = Training(
        personalId = personalId!!,
        memberId = this.memberId!!,
        description = this.description!!,
    )
}
