package com.example

import com.example.data.model.TurkeyLocationData
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testTurkeyProvincesCount() {
    assertEquals(81, TurkeyLocationData.PROVINCES.size)
  }

  @Test
  fun testIstanbulAndSileCoordinates() {
    val istanbul = TurkeyLocationData.getProvinceByName("İstanbul")
    assertNotNull(istanbul)
    assertEquals("İstanbul", istanbul?.name)
    assertTrue(istanbul!!.districts.isNotEmpty())

    val sile = TurkeyLocationData.getDistrictByName("İstanbul", "Şile")
    assertNotNull(sile)
    assertEquals("Şile", sile?.name)
    assertEquals(41.175, sile!!.lat, 0.05)
    assertEquals(29.612, sile.lon, 0.05)
  }

  @Test
  fun testSearchWithTurkishNormalization() {
    val results1 = TurkeyLocationData.searchProvinces("ist")
    assertTrue(results1.any { it.name == "İstanbul" })

    val results2 = TurkeyLocationData.searchProvinces("İZMİR")
    assertTrue(results2.any { it.name == "İzmir" })

    val districts = TurkeyLocationData.searchDistricts("Muğla", "MARMAR")
    assertTrue(districts.any { it.name == "Marmaris" })
  }
}

