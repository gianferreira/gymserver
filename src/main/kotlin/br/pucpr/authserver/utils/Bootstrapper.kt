package br.pucpr.authserver.utils

import br.pucpr.authserver.roles.Role
import br.pucpr.authserver.roles.RoleRepository
import br.pucpr.authserver.training.Training
import br.pucpr.authserver.training.TrainingRepository
import br.pucpr.authserver.users.User
import br.pucpr.authserver.users.UserRepository
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component

@Component
class Bootstrapper(
    val userRepository: UserRepository,
    val rolesRepository: RoleRepository,
    val trainingRepository: TrainingRepository,
    val properties: BootstrapperProperties,
): ApplicationListener<ContextRefreshedEvent> {
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        val adminRole = rolesRepository.findByName(properties.admin)
            ?: rolesRepository.save(Role(name = properties.admin, description = "Gym Administrator"))

        val personalRole = rolesRepository.findByName(properties.personal)
            ?: rolesRepository.save(Role(name = properties.personal, description = "Gym Personal"))

        val memberRole = rolesRepository.findByName(properties.member)
            ?: rolesRepository.save(Role(name = properties.member, description = "Gym Member"))

        val visitorRole = rolesRepository.findByName(properties.visitor)
            ?: rolesRepository.save(Role(name = properties.visitor, description = "Gym Visitor"))

        if(userRepository.findByRole(properties.admin).isEmpty()) {
            val admin = User(
                name = "Gym Administrator 01",
                email = "admin01@gym.com",
                password = "admin01"
            )

            admin.roles.add(visitorRole)
            admin.roles.add(adminRole)
            userRepository.save(admin)
        }

        if(userRepository.findByRole(properties.personal).isEmpty()) {
            val personal = User(
                name = "Gym Personal 01",
                email = "personal01@gym.com",
                password = "personal01"
            )

            personal.roles.add(visitorRole)
            personal.roles.add(personalRole)
            userRepository.save(personal)
        }

        if(properties.isDevelopment) {
            val secondAdmin = User(
                name = "Gym Administrator 02",
                email = "admin02@gym.com",
                password = "admin02"
            )

            secondAdmin.roles.add(visitorRole)
            secondAdmin.roles.add(adminRole)
            userRepository.save(secondAdmin)

            val secondPersonal = User(
                name = "Gym Personal 02",
                email = "personal02@gym.com",
                password = "personal02"
            )

            secondPersonal.roles.add(visitorRole)
            secondPersonal.roles.add(personalRole)
            userRepository.save(secondPersonal)

            val firstMember = User(
                name = "Gym Member 01",
                email = "member01@gym.com",
                password = "member01"
            )

            firstMember.roles.add(visitorRole)
            firstMember.roles.add(memberRole)
            userRepository.save(firstMember)

            val secondMember = User(
                name = "Gym Member 02",
                email = "member02@gym.com",
                password = "member02"
            )

            secondMember.roles.add(visitorRole)
            secondMember.roles.add(memberRole)
            userRepository.save(secondMember)

            val firstVisitor = User(
                name = "Gym Visitor 01",
                email = "visitor01@gym.com",
                password = "visitor01"
            )

            firstVisitor.roles.add(visitorRole)
            userRepository.save(firstVisitor)

            val secondVisitor = User(
                name = "Gym Visitor 02",
                email = "visitor02@gym.com",
                password = "visitor02"
            )

            secondVisitor.roles.add(visitorRole)
            userRepository.save(secondVisitor)

            val firstTraining = Training(
                personalId = 2L,
                memberId = 5L,
                description = trainingDescription(2L, 5L)
            )

            trainingRepository.save(firstTraining)

            val secondTraining = Training(
                personalId = 2L,
                memberId = 6L,
                description = trainingDescription(2L, 6L)
            )

            trainingRepository.save(secondTraining)

            val thirdTraining = Training(
                active = false,
                personalId = 2L,
                memberId = 5L,
                description = trainingDescription(2L, 5L)
            )

            trainingRepository.save(thirdTraining)

            val fourthTraining = Training(
                active = false,
                personalId = 2L,
                memberId = 6L,
                description = trainingDescription(2L, 6L)
            )

            trainingRepository.save(fourthTraining)

            val fifthTraining = Training(
                personalId = 4L,
                memberId = 5L,
                description = trainingDescription(4L, 5L)
            )

            trainingRepository.save(fifthTraining)

            val sixth = Training(
                personalId = 4L,
                memberId = 6L,
                description = trainingDescription(4L, 6L)
            )

            trainingRepository.save(sixth)

            val seventhTraining = Training(
                active = false,
                personalId = 4L,
                memberId = 5L,
                description = trainingDescription(4L, 5L)
            )

            trainingRepository.save(seventhTraining)

            val eighthTraining = Training(
                active = false,
                personalId = 4L,
                memberId = 6L,
                description = trainingDescription(4L, 6L)
            )

            trainingRepository.save(eighthTraining)
        }
    }

    companion object {
        fun trainingDescription(personalId: Long, memberId: Long): String {
            return "" +
                    " Training sheet " +
                    "\n PersonalID: $personalId" +
                    "\n MemberID: $memberId" +
                    "\n\n" +
                    " Under construction"
        }
    }
}
