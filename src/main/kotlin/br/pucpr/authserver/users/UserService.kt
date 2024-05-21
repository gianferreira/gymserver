package br.pucpr.authserver.users

import br.pucpr.authserver.exception.BadRequestException
import br.pucpr.authserver.exception.NotFoundException
import br.pucpr.authserver.roles.RoleRepository
import br.pucpr.authserver.security.Jwt
import br.pucpr.authserver.training.Training
import br.pucpr.authserver.training.TrainingRepository
import br.pucpr.authserver.users.responses.LoginResponse
import br.pucpr.authserver.users.responses.UserDetailsResponse
import br.pucpr.authserver.users.responses.UserResponse
import br.pucpr.authserver.utils.SortDir
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserService(
    val userRepository: UserRepository,
    val roleRepository: RoleRepository,
    val trainingRepository: TrainingRepository,
    val jwt: Jwt
) {
    fun insert(user: User): User {
        if (userRepository.findByEmail(user.email).isNotEmpty()) {
            throw BadRequestException("User already exists")
        }

        val visitorRole = roleRepository.findByName("VISITOR")
            ?: throw BadRequestException("Visitor role not found!")

        user.roles.add(visitorRole)
            .also { log.info("User created: id={}", user.id) }

        return userRepository.save(user)
    }

    fun findAll(dir: SortDir, role: String?) =
        role?.let { r ->
            when (dir) {
                SortDir.ASC -> userRepository.findByRole(r.uppercase()).sortedBy { it.name }
                SortDir.DESC -> userRepository.findByRole(r.uppercase()).sortedByDescending { it.name }
            }
        } ?: when (dir) {
            SortDir.ASC -> userRepository.findAll(Sort.by("name").ascending())
            SortDir.DESC -> userRepository.findAll(Sort.by("name").descending())
        }

    fun findByIdOrNull(id: Long): UserDetailsResponse? {
        val user = userRepository.findByIdOrNull(id)
            ?: throw NotFoundException("User does not exists")

        var trainings: List<Training> = emptyList()

        if(user.roles.any { it.name == "PERSONAL" }) {
            trainings = trainingRepository.findByPersonalId(id)
        }

        if(user.roles.any { it.name == "MEMBER" }) {
            trainings = trainingRepository.findByMemberId(id)
        }

        return UserDetailsResponse(
            user,
            trainings.filter { it.active },
        )
    }

    fun delete(id: Long): Boolean {
        val user = userRepository.findByIdOrNull(id)
            ?: throw NotFoundException("User does not exists")

        if (user.roles.any { it.name == "ADMIN" }) {
            val count = userRepository.findByRole("ADMIN").size
            if(count == 1) throw BadRequestException("Cannot delete the last system admin")
        }

        if (user.roles.any { it.name == "PERSONAL" }) {
            val count = userRepository.findByRole("PERSONAL").size
            if(count == 1) throw BadRequestException("Cannot delete the last system personal")

            log.warn("Deleted trainings by personal: id={}, name={}", user.id, user.name)
            trainingRepository.deletePersonalTrainings(id)
        }

        if (user.roles.any { it.name == "MEMBER" }) {
            log.warn("Deleted trainings by member: id={}, name={}", user.id, user.name)
            trainingRepository.deleteMemberTrainings(id)
        }

        log.warn("User deleted: id={}, name={}", user.id, user.name)
        userRepository.delete(user)
        return true
    }

    fun addRole(id: Long, roleName: String): Boolean {
        val user = userRepository.findByIdOrNull(id)
            ?: throw NotFoundException("User $id not found!")

        val role = roleRepository.findByName(roleName)
            ?: throw BadRequestException("Invalid role $roleName!")

        if (user.roles.any { it.name == role.name }) return false

        if (role.name == "PERSONAL") {
            if(user.roles.any { it.name == "ADMIN" || it.name == "MEMBER" }) {
                throw BadRequestException("Cannot register an ADMIN or MEMBER as a PERSONAL!")
            }

            user.roles.add(role)
            userRepository.save(user)
            return true
        }

        if(user.roles.any { it.name == "PERSONAL" }) {
            throw BadRequestException("Cannot register a PERSONAL as an ADMIN or a MEMBER!")
        }

        user.roles.add(role)
        userRepository.save(user)
        return true
    }

    fun removeRole(id: Long, roleName: String): Boolean {
        val user = userRepository.findByIdOrNull(id)
            ?: throw NotFoundException("User $id not found!")

        val role = roleRepository.findByName(roleName)
            ?: throw BadRequestException("Invalid role $roleName!")

        if (role.name == "VISITOR") return false

        if (user.roles.any { it.name == role.name }) {
            if (role.name == "MEMBER") {
                trainingRepository.inactiveMemberTrainings(id)
            }

            if (role.name == "PERSONAL") {
                val count = userRepository.findByRole("PERSONAL").size
                if(count == 1) throw BadRequestException("Cannot remove the last system personal")

                trainingRepository.inactivePersonalTrainings(id)
            }

            if (role.name == "ADMIN") {
                val count = userRepository.findByRole("ADMIN").size
                if(count == 1) throw BadRequestException("Cannot remove the last system admin")
            }

            user.roles.remove(role)
            userRepository.save(user)
            return true
        }

        throw  BadRequestException("User does not have this role.")
    }

    fun login(email: String, password: String): LoginResponse? {
        val user = userRepository.findByEmail(email).firstOrNull()

        if(user == null) {
            log.warn("User {} not found!", email)
            return null
        }

        if(password != user.password) {
            log.warn("Invalid password!")
            return null
        }

        log.info("User logged in: id={}, name={}", user.id, user.name)
        return LoginResponse(
            token = jwt.createToken(user),
            UserResponse(user)
        )
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(UserService::class.java)
    }
}
