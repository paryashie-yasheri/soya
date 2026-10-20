package itmo.soa.bands.controller

import itmo.soa.bands.dto.response.MusicBandPageResponse
import itmo.soa.bands.dto.request.MusicBandRequest
import itmo.soa.bands.dto.response.AverageNumberOfParticipantsResponse
import itmo.soa.bands.dto.response.CountResponse
import itmo.soa.bands.dto.response.MusicBandResponse
import itmo.soa.bands.tools.MusicBandsFilter
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/music-bands")
class MusicBandsController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createMusicBand(@RequestBody musicBand: MusicBandRequest) : MusicBandResponse {
        return TODO("Provide the return value")
    }

    @GetMapping
    fun getMusicBands(
        @RequestParam(name = "page", defaultValue = "0") page: Int,
        @RequestParam(name = "size", defaultValue = "10") size: Int,
        @RequestParam(name = "sort", required = false) sort: String?,
        @ModelAttribute filter: MusicBandsFilter,
    ): MusicBandPageResponse {
        return TODO("Provide the return value")
    }

    @GetMapping("/{id}")
    fun getMusicBandById(@PathVariable("id") id : Long) : MusicBandResponse {
        return TODO("Provide the return value")
    }

    @PutMapping("/{id}")
    fun updateMusicBandById(@PathVariable("id") id : Long, @RequestBody musicBand: MusicBandRequest) : MusicBandResponse {
        return TODO("Provide the return value")
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteMusicBandById(@PathVariable("id") id : Long)  {
        return TODO("Provide the return value")
    }

    @GetMapping("/average-number-of-participants")
    fun getAverageNumberOfParticipants(): AverageNumberOfParticipantsResponse {
        return TODO("Provide the return value")
    }

    @GetMapping("/count-by-number-of-participants")
    fun getCountByNumberOfParticipants(@RequestParam(name = "value") value: Int, ): CountResponse {
        return TODO("Provide the return value")
    }

    @GetMapping("/search-by-name")
    fun searchByName(@RequestParam(name = "substring") substring: String): List<MusicBandResponse> {
        return TODO("Provide the return value")
    }
}
