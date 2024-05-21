package br.pucpr.authserver.utils

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties("bootstrapper")
data class BootstrapperProperties @ConstructorBinding constructor(
    val isDevelopment: Boolean,
    val admin: String,
    val personal: String,
    val member: String,
    val visitor: String
)
