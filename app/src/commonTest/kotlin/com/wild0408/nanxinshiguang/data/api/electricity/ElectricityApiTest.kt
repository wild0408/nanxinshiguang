package com.wild0408.nanxinshiguang.data.api.electricity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ElectricityApiTest {
    @Test
    fun extractsTicketFromUnifiedAuthenticationLandingUrl() {
        assertEquals(
            "ST-1234-example",
            extractIcardSsoTicket(
                "https://icard.nuist.edu.cn/plat-pc/?name=loginTransit&ticket=ST-1234-example",
            ),
        )
    }

    @Test
    fun decodesTicketAndRejectsMissingTicket() {
        assertEquals(
            "ST/with+symbols=",
            extractIcardSsoTicket(
                "https://icard.nuist.edu.cn/plat-pc/?ticket=ST%2Fwith%2Bsymbols%3D",
            ),
        )
        assertNull(extractIcardSsoTicket("https://icard.nuist.edu.cn/plat-pc/"))
        assertNull(extractIcardSsoTicket("not a url"))
    }
}
