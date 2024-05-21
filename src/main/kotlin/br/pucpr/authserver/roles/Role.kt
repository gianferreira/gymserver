package br.pucpr.authserver.roles

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.jetbrains.annotations.NotNull

@Entity
class Role(
    @Id @GeneratedValue
    var id: Long? = null,

    @Column(unique = true, nullable = false)
    var name: String,

    @NotNull
    var description: String = "",
)
