package br.pucpr.authserver.training

import br.pucpr.authserver.exception.ForbiddenException
import br.pucpr.authserver.security.UserToken
import br.pucpr.authserver.training.requests.TrainingEditingRequest
import br.pucpr.authserver.training.requests.TrainingRequest
import br.pucpr.authserver.training.responses.TrainingDetailsResponse
import br.pucpr.authserver.training.responses.TrainingResponse
import br.pucpr.authserver.utils.SortDir
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RequestMapping("/trainings")
@Tag(name="2. Trainings")
@RestController
class TrainingController(
    val service: TrainingService
) {
    @PostMapping
    @PreAuthorize("hasRole('PERSONAL')")
    @SecurityRequirement(name = "WebToken")
    fun insert(
        @Valid @RequestBody training: TrainingRequest,
        auth: Authentication
    ): ResponseEntity<TrainingDetailsResponse> {
        val authenticatedPersonal = auth.principal as? UserToken
            ?: throw ForbiddenException()

        return training.toTraining(personalId = authenticatedPersonal.id)
            .let { service.insert(it) }
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }
    }

    @GetMapping
    fun findAll(
        @RequestParam sortDir: String? = null,
        @RequestParam personalId: Long? = null,
        @RequestParam memberId: Long? = null,
        @RequestParam active: Boolean? = null,
    ) =
        SortDir.entries.firstOrNull { it.name == (sortDir ?: "ASC").uppercase() }
            ?.let { service.findFiltered(it, personalId, memberId, active) }
            ?.map { TrainingResponse(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.badRequest().build()

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long) =
        service.findByIdOrNull(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('PERSONAL')")
    @SecurityRequirement(name = "WebToken")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody training: TrainingEditingRequest,
        auth: Authentication
    ): ResponseEntity<TrainingResponse> {
        val authenticatedPersonal = auth.principal as? UserToken
            ?: throw ForbiddenException()

        return service.update(
            id,
            authenticatedPersonal.id,
            training.active,
            training.description,
        )
            ?.let { TrainingResponse(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PERSONAL')")
    @SecurityRequirement(name = "WebToken")
    fun deleteById(
        @PathVariable id: Long,
        auth: Authentication
    ): ResponseEntity<Void> {
        val authenticatedPersonal = auth.principal as? UserToken
            ?: throw ForbiddenException()

        return if (service.delete(id, authenticatedPersonal.id))
            ResponseEntity.ok().build()
        else
            ResponseEntity.notFound().build()
    }
}
