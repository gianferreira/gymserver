package br.pucpr.authserver.users

import br.pucpr.authserver.exception.BadRequestException
import br.pucpr.authserver.exception.NotFoundException
import br.pucpr.authserver.roles.Role
import br.pucpr.authserver.roles.RoleRepository
import br.pucpr.authserver.security.Jwt
import br.pucpr.authserver.training.TrainingRepository
import br.pucpr.authserver.user
import br.pucpr.authserver.utils.SortDir
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull

class UserServiceTest {
    private val userRepository = mockk<UserRepository>()
    private val roleRepository = mockk<RoleRepository>()
    private val trainingRepository = mockk<TrainingRepository>()
    private val jwt = mockk<Jwt>()

    private val service = UserService(
        userRepository,
        roleRepository,
        trainingRepository,
        jwt,
    )

    @BeforeEach
    fun setup() = clearAllMocks()

    @AfterEach
    fun cleanUp() = checkUnnecessaryStub(
        userRepository,
        roleRepository,
        trainingRepository,
        jwt,
    )

    @Test
    fun `insert must throw BadRequestException if an user with the same email is found`() {
        val user = user(id = null)

        every { userRepository.findByEmail(user.email) } returns listOf(user)

        assertThrows<BadRequestException> {
            service.insert(user)
        } shouldHaveMessage "User already exists"
    }

    @Test
    fun `insert must throw BadRequestException if visitor role is not found`() {
        val user = user(id = null)
        every { userRepository.findByEmail(user.email) } returns listOf()

        every { roleRepository.findByName("VISITOR") } returns null

        assertThrows<BadRequestException> {
            service.insert(user)
        } shouldHaveMessage "Visitor role not found!"
    }

    @Test
    fun `insert must return the saved user if it's inserted`() {
        val user = user(id = null)
        every { userRepository.findByEmail(user.email) } returns listOf()

        val visitorRole = Role(name = "VISITOR")
        every { roleRepository.findByName("VISITOR") } returns visitorRole

        val saved = user(id = 1, roles = listOf(visitorRole.name))
        every { userRepository.save(user) } returns saved
        service.insert(user) shouldBe saved
    }

    @Test
    fun `findAll must return an ascending list if SortDir ASC is used`() {
        val sortDir = SortDir.ASC
        val userList = listOf(user(1), user(2), user(3))

        every { userRepository.findAll(Sort.by("name").ascending()) } returns userList

        service.findAll(dir = sortDir, role = null) shouldBe userList
    }

    @Test
    fun `findAll must return an descending list if SortDir DESC is used`() {
        val sortDir = SortDir.DESC
        val userList = listOf(user(3), user(2), user(1))

        every { userRepository.findAll(Sort.by("name").descending()) } returns userList

        service.findAll(dir = sortDir, role = null) shouldBe userList
    }

    @Test
    fun `findAll must return an ASC list filtered by Role if ASC and role is used`() {
        val sortDir = SortDir.ASC
        val userList = listOf(user(1), user(2))

        every { userRepository.findByRole("ADMIN") } returns userList

        service.findAll(sortDir, "ADMIN") shouldBe userList
    }

    @Test
    fun `findAll must return a DESC list filtered by Role if DESC and role is used`() {
        val sortDir = SortDir.DESC
        val userList = listOf(user(2), user(1))

        every { userRepository.findByRole("ADMIN") } returns userList

        service.findAll(sortDir, "ADMIN") shouldBe userList
    }

    @Test
    fun `findByIdOrNull must throw NotFoundException if user is not found`() {
        val user = user(id = 1)

        every { userRepository.findByIdOrNull(user.id) } returns null

        assertThrows<NotFoundException> {
            service.findByIdOrNull(user.id!!)
        } shouldHaveMessage "User does not exists"
    }

    @Test
    fun `delete must throw BadRequestException if user is the unique ADMIN`() {
        val user = user(id = 1, roles = listOf("ADMIN"))

        every { userRepository.findByIdOrNull(user.id) } returns user
        every { userRepository.findByRole("ADMIN") } returns listOf(user)

        assertThrows<BadRequestException> {
            service.delete(user.id!!)
        } shouldHaveMessage "Cannot delete the last system admin"
    }

    @Test
    fun `delete must throw BadRequestException if user is the unique PERSONAL`() {
        val user = user(id = 1, roles = listOf("PERSONAL"))

        every { userRepository.findByIdOrNull(user.id) } returns user
        every { userRepository.findByRole("PERSONAL") } returns listOf(user)

        assertThrows<BadRequestException> {
            service.delete(user.id!!)
        } shouldHaveMessage "Cannot delete the last system personal"
    }

    @Test
    fun `addRole must throw NotFoundException if user is not found`() {
        val user = user(id = 1)

        every { userRepository.findByIdOrNull(user.id) } returns null

        assertThrows<NotFoundException> {
            service.addRole(user.id!!, "")
        } shouldHaveMessage "User ${user.id} not found!"
    }

    @Test
    fun `addRole must throw BadRequestException if role is not found`() {
        val roleName = "INVALID ROLE"
        val user = user(id = 1)

        every { userRepository.findByIdOrNull(user.id) } returns user
        every { roleRepository.findByName(roleName) } returns null

        assertThrows<BadRequestException> {
            service.addRole(user.id!!, roleName)
        } shouldHaveMessage "Invalid role $roleName!"
    }

    @Test
    fun `removeRole must throw NotFoundException if user is not found`() {
        val user = user(id = 1)

        every { userRepository.findByIdOrNull(user.id) } returns null

        assertThrows<NotFoundException> {
            service.removeRole(user.id!!, "")
        } shouldHaveMessage "User ${user.id} not found!"
    }

    @Test
    fun `removeRole must throw BadRequestException if role is not found`() {
        val roleName = "INVALID ROLE"
        val user = user(id = 1)

        every { userRepository.findByIdOrNull(user.id) } returns user
        every { roleRepository.findByName(roleName) } returns null

        assertThrows<BadRequestException> {
            service.removeRole(user.id!!, roleName)
        } shouldHaveMessage "Invalid role $roleName!"
    }

    @Test
    fun `login must return null if user is not found`() {
        every { userRepository.findByEmail("gian@email.com") } returns listOf()

        service.login("gian@email.com", "123456") shouldBe null
    }

    @Test
    fun `login must return null if password not match`() {
        val user = user(id = 1)

        every { userRepository.findByEmail(user.email) } returns listOf(user)

        service.login(user.email, "123456") shouldBe null
    }
}
