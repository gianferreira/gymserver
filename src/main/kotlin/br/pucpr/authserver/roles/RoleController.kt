package br.pucpr.authserver.roles

import br.pucpr.authserver.roles.requests.RoleRequest
import br.pucpr.authserver.roles.responses.RoleResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/roles")
@Tag(name="3. Roles")
@RestController
class RoleController(
    val service: RoleService
) {
    @PostMapping
    fun insert(@Valid @RequestBody role: RoleRequest) =
        role.toRole()
            .let { service.insert(it) }
            .let { RoleResponse(it) }
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @GetMapping
    fun findAll() = service.findAll()
        .map { RoleResponse(it) }
        .let { ResponseEntity.ok(it) }
}
