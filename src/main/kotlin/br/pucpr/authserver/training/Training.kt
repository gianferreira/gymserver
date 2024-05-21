package br.pucpr.authserver.training

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.jetbrains.annotations.NotNull

@Entity
class Training(
    @Id @GeneratedValue
    var id: Long? = null,

    @NotNull
    var personalId: Long? = null,

    @NotNull
    var memberId: Long? = null,

    @NotNull
    var description: String = "",

    @NotNull
    var active: Boolean = true,
)
