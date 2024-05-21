package br.pucpr.authserver.training

import br.pucpr.authserver.exception.BadRequestException
import br.pucpr.authserver.exception.NotFoundException
import br.pucpr.authserver.training
import br.pucpr.authserver.user
import br.pucpr.authserver.users.UserRepository
import br.pucpr.authserver.utils.SortDir
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull

class TrainingServiceTest {
    private val trainingRepository = mockk<TrainingRepository>()
    private val userRepository = mockk<UserRepository>()

    private val service = TrainingService(
        trainingRepository,
        userRepository,
    )

    @BeforeEach
    fun setup() = clearAllMocks()

    @AfterEach
    fun cleanUp() = checkUnnecessaryStub(
        trainingRepository,
        userRepository,
    )

    @Test
    fun `insert must throw NotFoundException if the personal is not found`() {
        val training = training()

        every { userRepository.findByIdOrNull(training.personalId) } returns null

        assertThrows<NotFoundException> {
            service.insert(training)
        } shouldHaveMessage "Personal ${training.personalId} not found!"
    }

    @Test
    fun `insert must throw NotFoundException if the member is not found`() {
        val user = user(id = 1)
        val training = training(personalId = user.id, memberId = 2)

        every { userRepository.findByIdOrNull(training.personalId) } returns user
        every { userRepository.findByIdOrNull(training.memberId) } returns null

        assertThrows<NotFoundException> {
            service.insert(training)
        } shouldHaveMessage "Member ${training.memberId} not found!"
    }

    @Test
    fun `findFiltered must return an ascending list if SortDir ASC is used`() {
        val sortDir = SortDir.ASC
        val trainingList = listOf(training(1), training(2), training(3))

        every { trainingRepository.findAll() } returns trainingList

        service.findFiltered(
            sortDir,
            null,
            null,
            null,
        ) shouldBe trainingList
    }

    @Test
    fun `findFiltered must return an descending list if SortDir DESC is used`() {
        val sortDir = SortDir.DESC
        val trainingList = listOf(training(3), training(2), training(1))

        every { trainingRepository.findAll() } returns trainingList

        service.findFiltered(
            sortDir,
            null,
            null,
            null,
        ) shouldBe trainingList
    }

    @Test
    fun `findByIdOrNull must throw NotFoundException if training is not found`() {
        every { trainingRepository.findByIdOrNull(1) } returns null

        assertThrows<NotFoundException> {
            service.findByIdOrNull(1)
        } shouldHaveMessage "Training is not found!"
    }

    @Test
    fun `findByIdOrNull must throw BadRequestException if training is inactive`() {
        val training = training()
        training.active = false

        every { trainingRepository.findByIdOrNull(training.id) } returns training

        assertThrows<BadRequestException> {
            service.findByIdOrNull(training.id!!)
        } shouldHaveMessage "Training is inactive!"
    }

    @Test
    fun `update must throw NotFoundException if training is not found`() {
        every { trainingRepository.findByIdOrNull(1) } returns null

        assertThrows<NotFoundException> {
            service.update(1, 0, false, "")
        } shouldHaveMessage "Training 1 not found!"
    }

    @Test
    fun `update must throw BadRequestException if authenticated user is not the personal`() {
        val training = training()

        every { trainingRepository.findByIdOrNull(1) } returns training

        assertThrows<BadRequestException> {
            service.update(1, 2, false, "")
        } shouldHaveMessage "Authenticated personal 2 cannot update this training!"
    }

    @Test
    fun `delete must throw NotFoundException if training is not found`() {
        every { trainingRepository.findByIdOrNull(1) } returns null

        assertThrows<NotFoundException> {
            service.delete(1, 0)
        } shouldHaveMessage "Training 1 not found!"
    }

    @Test
    fun `delete must throw BadRequestException if authenticated user is not the personal`() {
        val training = training()

        every { trainingRepository.findByIdOrNull(1) } returns training

        assertThrows<BadRequestException> {
            service.delete(1, 2)
        } shouldHaveMessage "Authenticated personal 2 cannot delete this training!"
    }
}
