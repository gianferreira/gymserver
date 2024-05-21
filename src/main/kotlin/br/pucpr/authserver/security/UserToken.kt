package br.pucpr.authserver.security

import br.pucpr.authserver.users.User

data class UserToken(
    val id: Long,
    val name: String,
    val roles: Set<String>
) {
    constructor(user: User): this(
        id = user.id!!,
        name = user.name,
        roles = user.roles.map { it.name }.toSortedSet()
    )
}
