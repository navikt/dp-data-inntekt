package no.nav.dagpenger.data.inntekt.grunnbeløp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class GGrunnbeløpTest {
    private val g = GGrunnbeløp()

    @Test
    fun `gir riktig grunnbeløp`() {
        val `2022` = LocalDate.of(2022, 1, 1)
        assertEquals(106399.0, g.gjeldendeGrunnbeløp(`2022`))
        assertEquals(101351.0, g.gjeldendeGrunnbeløp(`2022`.minusYears(1)))
        assertEquals(99858.0, g.gjeldendeGrunnbeløp(`2022`.minusYears(2)))
        assertEquals(106399.0, g.gjeldendeGrunnbeløp(`2022`))
    }
}
