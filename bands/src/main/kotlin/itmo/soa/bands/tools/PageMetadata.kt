package itmo.soa.bands.tools

data class PageMetadata(
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
