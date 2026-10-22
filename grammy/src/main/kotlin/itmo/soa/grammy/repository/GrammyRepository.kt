package itmo.soa.grammy.repository

import itmo.soa.grammy.model.NominationEntity
import itmo.soa.grammy.model.SingleEntity

/** Persistence boundary: the service layer does not contain SQL. */
interface GrammyRepository {
    fun saveSingle(single: SingleEntity)
    fun saveNomination(nomination: NominationEntity)
}
