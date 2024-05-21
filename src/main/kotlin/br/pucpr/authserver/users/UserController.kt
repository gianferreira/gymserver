package br.pucpr.authserver.users

import br.pucpr.authserver.users.requests.LoginRequest
import br.pucpr.authserver.users.requests.UserRequest
import br.pucpr.authserver.users.responses.UserResponse
import br.pucpr.authserver.utils.SortDir
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
@Tag(name="1. Users")
class UserController(
    val service: UserService
) {
    @PostMapping
    fun insert(@Valid @RequestBody user: UserRequest): ResponseEntity<UserResponse> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(UserResponse(service.insert(user.toUser())))

    @GetMapping
    fun findAll(
        @RequestParam sortDir: String? = null,
        @RequestParam role: String? = null,
    ) =
        SortDir.entries.firstOrNull { it.name == (sortDir ?: "ASC").uppercase() }
            ?.let { service.findAll(it, role) }
            ?.map { UserResponse(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.badRequest().build()

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long) =
        service.findByIdOrNull(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "WebToken")
    fun deleteById(
        @PathVariable id: Long
    ): ResponseEntity<Void> =
        if (service.delete(id)) ResponseEntity.ok().build()
        else ResponseEntity.notFound().build()

    @PutMapping("/{id}/roles/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "WebToken")
    fun grant(
        @PathVariable id: Long,
        @PathVariable role: String
    ): ResponseEntity<Void> =
        if (service.addRole(id, role)) ResponseEntity.ok().build()
        else ResponseEntity.noContent().build()

    @DeleteMapping("/{id}/roles/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "WebToken")
    fun deny(
        @PathVariable id: Long,
        @PathVariable role: String
    ): ResponseEntity<Void> =
        if (service.removeRole(id, role)) ResponseEntity.ok().build()
        else ResponseEntity.noContent().build()

    @PostMapping("/login")
    fun login(@Valid @RequestBody login: LoginRequest) =
        service.login(login.email!!, login.password!!)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
}
