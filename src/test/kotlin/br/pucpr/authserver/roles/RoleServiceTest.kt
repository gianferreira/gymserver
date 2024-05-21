package br.pucpr.authserver.roles

import br.pucpr.authserver.exception.BadRequestException
import br.pucpr.authserver.role
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.mockk.checkUnnecessaryStub
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.Sort

class RoleServiceTest {
    private val roleRepository = mockk<RoleRepository>()

    private val service = RoleService(
        roleRepository,
    )

    @BeforeEach
    fun setup() = clearAllMocks()

    @AfterEach
    fun cleanUp() = checkUnnecessaryStub(
        roleRepository,
    )

    @Test
    fun `insert must throw BadRequestException if a role with the same name is found`() {
        val role = role()

        every { roleRepository.findAll() } returns listOf(role)
        assertThrows<BadRequestException> {
            service.insert(role)
        } shouldHaveMessage "A role with this name already exists"
    }

    @Test
    fun `insert must return the saved role if it's inserted`() {
        val role = role()

        every { roleRepository.findAll() } returns listOf()

        every { roleRepository.save(role) } returns role
        service.insert(role) shouldBe role
    }

    @Test
    fun `findAll should delegate to repository sorting by name`() {
        val roles = listOf(role())

        every { roleRepository.findAll(Sort.by("name").ascending()) } returns roles

        service.findAll() shouldBe roles
    }
}
