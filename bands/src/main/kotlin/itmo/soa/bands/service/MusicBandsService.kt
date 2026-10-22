package itmo.soa.bands.service

import itmo.soa.bands.dto.request.MusicBandRequest
import itmo.soa.bands.model.MusicBand
import itmo.soa.bands.model.MusicBandPage
import itmo.soa.bands.tools.MusicBandsFilter

interface MusicBandsService {

    fun createMusicBand(band : MusicBandRequest) : MusicBand
    fun getMusicBands(page: Int, size: Int, sort: String?, filter: MusicBandsFilter): MusicBandPage
    fun getMusicBandById(id: Long): MusicBand
    fun updateMusicBandById(id: Long, musicBand: MusicBandRequest): MusicBand
    fun deleteMusicBandById(id: Long)
    fun getAverageNumberOfParticipants(): Double
    fun getCountByNumberOfParticipants(value: Int): Long
    fun searchByName(substring: String): List<MusicBand>
}