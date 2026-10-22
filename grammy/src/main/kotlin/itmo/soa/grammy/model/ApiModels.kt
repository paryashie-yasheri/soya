package itmo.soa.grammy.model

enum class MusicGenre { PSYCHEDELIC_ROCK, HIP_HOP, SOUL, POST_ROCK, BRIT_POP }

data class SingleAddedResponse(val bandId: Long, val singleName: String)
data class NominationResponse(val bandId: Long, val genre: MusicGenre, val status: String = "NOMINATED")
data class ErrorResponse(val status: Int, val error: String, val code: String, val message: String, val path: String)
