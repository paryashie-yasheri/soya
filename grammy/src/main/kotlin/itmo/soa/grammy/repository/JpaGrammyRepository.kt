package itmo.soa.grammy.repository

import itmo.soa.grammy.model.NominationEntity
import itmo.soa.grammy.model.SingleEntity
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext

@ApplicationScoped
class JpaGrammyRepository : GrammyRepository {
    @PersistenceContext(unitName = "grammyPU")
    private lateinit var entityManager: EntityManager

    override fun saveSingle(single: SingleEntity) {
        entityManager.persist(single)
    }

    override fun saveNomination(nomination: NominationEntity) {
        entityManager.persist(nomination)
    }
}
