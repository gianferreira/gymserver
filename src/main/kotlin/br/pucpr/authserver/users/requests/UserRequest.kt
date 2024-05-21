package br.pucpr.authserver.users.requests

import br.pucpr.authserver.users.User
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

data class UserRequest(
    @field:NotBlank
    val name: String?,

    @field:Email
    val email: String?,

    @field:Size(min = 6, max = 10)
    val password: String?
) {
    fun toUser() = User(
        email = email!!,
        password = password!!,
        name = name ?: "",
    )
}
