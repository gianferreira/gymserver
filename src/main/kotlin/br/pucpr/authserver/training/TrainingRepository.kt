package br.pucpr.authserver.training

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TrainingRepository: JpaRepository<Training, Long> {
    fun findByPersonalId(personalId: Long): List<Training>

    fun findByMemberId(memberId: Long): List<Training>

    @Query(
        "select distinct t from Training t" +
                " where t.personalId = :personalId" +
                " and t.memberId = :memberId" +
                " order by t.id"
    )
    fun findByPersonalAndMember(personalId: Long, memberId: Long): List<Training>

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
        "update Training t set t.active = false where t.personalId = :personalId"
    )
    fun inactivePersonalTrainings(@Param("personalId") personalId: Long)

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
        "update Training t set t.active = false where t.memberId = :memberId"
    )
    fun inactiveMemberTrainings(@Param("memberId") memberId: Long)

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
        "delete from Training t where t.personalId = :personalId"
    )
    fun deletePersonalTrainings(@Param("personalId") personalId: Long)

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
        "delete from Training t where t.memberId = :memberId"
    )
    fun deleteMemberTrainings(@Param("memberId") memberId: Long)
}
