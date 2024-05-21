package br.pucpr.authserver.training.responses

import br.pucpr.authserver.training.Training
import br.pucpr.authserver.users.User

data class TrainingDetailsResponse(
    val id: Long,
    val personal: User,
    val member: User,
    val description: String,
    val active: Boolean,
) {
    constructor(t: Training, personal: User, member: User): this(
        id = t.id!!,
        personal = personal,
        member = member,
        description = t.description,
        active = t.active,
    )
}
