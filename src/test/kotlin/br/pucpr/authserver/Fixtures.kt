package br.pucpr.authserver

import br.pucpr.authserver.roles.Role
import br.pucpr.authserver.training.Training
import br.pucpr.authserver.users.User

fun role(
    id: Long? = 1,
    name: String = "ADMIN",
) = Role(
        id = id,
        name = name,
        description = "",
    )

fun training(
    id: Long? = 1,
    personalId: Long? = 1,
    memberId: Long? = 1,
) = Training(
        id = id,
        personalId = personalId,
        memberId = memberId,
        description = "",
    )

fun user(
    id: Long? = null,
    name: String = "name",
    roles: List<String> = listOf(),
) = User(
        id = id,
        email = "${name}@email.com",
        password = "pass1234",
        name = name,
        roles = roles
            .mapIndexed { i, v -> Role(id = i.toLong(), name = v) }
            .toMutableSet()
    )
