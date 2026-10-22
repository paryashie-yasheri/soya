package itmo.soa.bands.model

import itmo.soa.bands.tools.PageMetadata

data class MusicBandPage(
    val bands: List<MusicBand>,
    val metadata: PageMetadata
)
