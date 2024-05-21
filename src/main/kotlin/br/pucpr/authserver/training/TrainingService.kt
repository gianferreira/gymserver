package br.pucpr.authserver.training

import br.pucpr.authserver.exception.BadRequestException
import br.pucpr.authserver.exception.NotFoundException
import br.pucpr.authserver.training.responses.TrainingDetailsResponse
import br.pucpr.authserver.users.UserRepository
import br.pucpr.authserver.users.UserService
import br.pucpr.authserver.utils.SortDir
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class TrainingService(
    val trainingRepository: TrainingRepository,
    val userRepository: UserRepository,
) {
    fun insert(training: Training): TrainingDetailsResponse {
        val personal = userRepository.findByIdOrNull(training.personalId)
            ?: throw NotFoundException("Personal ${training.personalId} not found!")

        val member = userRepository.findByIdOrNull(training.memberId)
            ?: throw NotFoundException("Member ${training.memberId} not found!")

        if (member.roles.any { it.name == "MEMBER" }) {
            val createdTraining = trainingRepository.save(training)

            return TrainingDetailsResponse(
                createdTraining,
                personal,
                member,
            )
        }

        throw BadRequestException("The user passed as a memberId does not have the MEMBER role.")
    }

    fun findFiltered(dir: SortDir, personalId: Long?, memberId: Long?, active: Boolean?): List<Training> {
        var disorderlyList: List<Training>

        disorderlyList = if(personalId != null && memberId != null) {
            trainingRepository.findByPersonalAndMember(personalId, memberId)
        } else if (personalId != null) {
            trainingRepository.findByPersonalId(personalId)
        } else if (memberId != null) {
            trainingRepository.findByMemberId(memberId)
        } else {
            trainingRepository.findAll()
        }

        if(active != null) {
            disorderlyList = disorderlyList.filter { it.active == active }
        }

        return when (dir) {
            SortDir.ASC -> disorderlyList.sortedBy { it.id }
            SortDir.DESC -> disorderlyList.sortedByDescending { it.id }
        }
    }

    fun findByIdOrNull(id: Long): TrainingDetailsResponse? {
        val training = trainingRepository.findByIdOrNull(id)
            ?: throw NotFoundException("Training is not found!")

        if(!training.active) throw BadRequestException("Training is inactive!")

        val personal = userRepository.findByIdOrNull(training.personalId)
            ?: throw BadRequestException("Personal ${training.personalId} from this training not found!")

        val member = userRepository.findByIdOrNull(training.memberId)
            ?: throw BadRequestException("Member ${training.memberId} from this training not found!")

        return TrainingDetailsResponse(
            training,
            personal,
            member,
        )
    }

    fun update(id: Long, personalId: Long, active: Boolean?, description: String?): Training? {
        val training = trainingRepository.findByIdOrNull(id)
            ?: throw NotFoundException("Training $id not found!")

        if(training.personalId != personalId)
            throw BadRequestException("Authenticated personal $personalId cannot update this training!")

        if(active == null && description == null) return null

        if(active == true) {
            val member = userRepository.findByIdOrNull(training.memberId)
                ?: throw BadRequestException("Member ${training.memberId} from this training not found!")

            if (!member.roles.any { it.name == "MEMBER" })
                throw BadRequestException("Member ${training.memberId} from this training is not a MEMBER anymore!")
        }

        training.active = active ?: training.active

        if(description != null) {
            training.description = description
        }

        return trainingRepository.save(training)
    }

    fun delete(id: Long, personalId: Long): Boolean {
        val training = trainingRepository.findByIdOrNull(id)
            ?: throw NotFoundException("Training $id not found!")

        if(training.personalId != personalId)
            throw BadRequestException("Authenticated personal $personalId cannot delete this training!")

        if (training.active) throw BadRequestException("Cannot delete an active training")

        log.warn("Training deleted: id={}", training.id)
        trainingRepository.delete(training)
        return true
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(UserService::class.java)
    }
}
