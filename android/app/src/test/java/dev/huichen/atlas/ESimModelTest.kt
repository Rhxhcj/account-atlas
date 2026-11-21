package dev.huichen.atlas

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ESimModelTest {
  @Test fun esimRoundTripsAndIsOmittedWhenOff() {
    val d = AtlasData(identities = listOf(
      Identity("p1", "phone", "+86 170 0000 5678", esim = true),
      Identity("p2", "phone", "+86 138 0000 1234"),
    ))
    val text = encodeAtlas(d)
    assertTrue(text, text.contains("\"esim\":true"))
    assertEquals(1, Regex("\"esim\"").findAll(text).count())
    val back = decodeAtlas(text)
    assertTrue(back.identities[0].isESim)
    assertNull(back.identities[1].esim)
    assertEquals(Merge.sig(d), Merge.sig(back))
  }

  @Test fun oldDataAndFalseLoadUnchanged() {
    val d = decodeAtlas("""{"identities":[{"id":"a","type":"phone","value":"1"},{"id":"b","type":"phone","value":"2","esim":false}],"accounts":[],"deleted":{}}""")
    assertNull(d.identities[0].esim)
    assertNull(d.identities[1].esim)
    assertTrue(!encodeAtlas(d).contains("esim"))
  }

  @Test fun emailIsNeverESim() {
    assertTrue(!Identity("e", "email", "a@b.c", esim = true).isESim)
  }
}
