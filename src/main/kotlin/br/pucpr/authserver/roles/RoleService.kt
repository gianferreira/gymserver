package br.pucpr.authserver.roles

import br.pucpr.authserver.exception.BadRequestException
import br.pucpr.authserver.users.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class RoleService(
    val repository: RoleRepository,
) {
    fun insert(role: Role): Role {
        val roles = repository.findAll().toList()

        if (roles.any { it.name == role.name }) {
            throw BadRequestException("A role with this name already exists")
        }

        log.info("Role created: ${role.name}")
        return repository.save(role)
    }

    fun findAll(): List<Role> = repository.findAll(
        Sort.by("name").ascending()
    )

    companion object {
        val log: Logger = LoggerFactory.getLogger(UserService::class.java)
    }
}
