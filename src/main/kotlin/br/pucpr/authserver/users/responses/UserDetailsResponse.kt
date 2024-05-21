package br.pucpr.authserver.users.responses

import br.pucpr.authserver.roles.Role
import br.pucpr.authserver.training.Training
import br.pucpr.authserver.users.User

data class UserDetailsResponse(
    val id: Long,
    val name: String,
    val email: String,
    val roles : List<Role>,
    val trainings: List<Training>,
) {
    constructor(u: User, trainings: List<Training>): this(
        id = u.id!!,
        name = u.name,
        email = u.email,
        roles = u.roles.toList(),
        trainings = trainings,
    )
}
