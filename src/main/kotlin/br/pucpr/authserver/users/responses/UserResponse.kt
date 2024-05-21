package br.pucpr.authserver.users.responses

import br.pucpr.authserver.users.User

data class UserResponse(
    val id: Long,
    val name: String,
    val email: String
) {
    constructor(u: User): this(
        id = u.id!!,
        name = u.name,
        email = u.email,
    )
}
