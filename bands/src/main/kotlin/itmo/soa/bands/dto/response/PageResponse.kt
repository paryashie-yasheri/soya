package itmo.soa.bands.dto.response

import itmo.soa.bands.tools.PageMetadata

data class PageResponse<T>(
    val content: List<T>,
    val metadata: PageMetadata
)
