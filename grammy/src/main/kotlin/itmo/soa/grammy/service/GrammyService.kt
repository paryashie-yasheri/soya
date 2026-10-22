package itmo.soa.grammy.service

import itmo.soa.grammy.model.MusicGenre
import itmo.soa.grammy.model.NominationEntity
import itmo.soa.grammy.model.NominationResponse
import itmo.soa.grammy.model.SingleEntity
import itmo.soa.grammy.model.SingleAddedResponse
import itmo.soa.grammy.repository.GrammyRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.PersistenceException
import jakarta.transaction.Transactional

@ApplicationScoped
class GrammyService @Inject constructor(private val musicBands: MusicBandsClient, private val repository: GrammyRepository) {

    @Transactional
    fun addSingle(bandId: Long, name: String): SingleAddedResponse {
        requireBandExists(bandId)
        return SingleAddedResponse(bandId, name).also { response -> save { repository.saveSingle(SingleEntity(bandId = response.bandId, singleName = response.singleName)) } }
    }

    @Transactional
    fun nominate(bandId: Long, genre: MusicGenre): NominationResponse {
        requireBandExists(bandId)
        return NominationResponse(bandId, genre).also { response -> save { repository.saveNomination(NominationEntity(bandId = response.bandId, genre = response.genre)) } }
    }

    private fun save(action: () -> Unit) = try { action() } catch (exception: PersistenceException) {
        throw ApiException(503, "GRAMMY_DATABASE_UNAVAILABLE", exception.message ?: "Grammy database is unavailable", exception)
    }

    private fun requireBandExists(bandId: Long) {
        if (!musicBands.bandExists(bandId)) {
            throw ApiException(404, "BAND_NOT_FOUND", "Music band with id $bandId was not found")
        }
    }
}
