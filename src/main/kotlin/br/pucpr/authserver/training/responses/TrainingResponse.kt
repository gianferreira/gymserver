package br.pucpr.authserver.training.responses

import br.pucpr.authserver.training.Training

data class TrainingResponse(
    val id: Long,
    val personalId: Long,
    val memberId: Long,
    val description: String,
    val active: Boolean,
) {
    constructor(t: Training): this(
        id = t.id!!,
        personalId = t.personalId!!,
        memberId = t.memberId!!,
        description = t.description,
        active = t.active,
    )
}
