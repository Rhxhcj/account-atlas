package dev.huichen.atlas

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginModelTest {
  @Test fun providerAndViaRoundTrip() {
    val d = AtlasData(
      identities = listOf(
        Identity("p1", "phone", "+86 138 0000 1234"),
        Identity("w1", "login", "atlas_wx", color = "#07c160", provider = "wechat", via = "p1"),
        Identity("a1", "login", "me@icloud.com", color = "#1d1d1f", provider = "apple"),
      ),
      accounts = listOf(Account("x", "美团", "meituan.com", "出行", listOf("w1"), createdAt = 1.0)),
    )
    val text = encodeAtlas(d)
    assertTrue(text, text.contains("\"provider\":\"wechat\""))
    assertTrue(text, text.contains("\"via\":\"p1\""))
    assertEquals(1, Regex("\"via\"").findAll(text).count())          // omitted when empty
    assertEquals(2, Regex("\"provider\"").findAll(text).count())     // omitted on email / phone
    val back = decodeAtlas(text)
    assertEquals("wechat", back.identities[1].provider)
    assertEquals("p1", back.identities[1].via)
    assertTrue(back.identities[1].isWechat)
    assertTrue(back.identities[2].isApple)
    assertNull(back.identities[2].via)
    assertNull(back.identities[0].provider)
    assertEquals(listOf("w1"), back.accounts[0].identityIds)
    assertEquals(Merge.sig(d), Merge.sig(back))
  }

  @Test fun emptyViaAndProviderAreDropped() {
    val d = decodeAtlas("""{"identities":[{"id":"w","type":"login","value":"x","provider":"wechat","via":""},{"id":"e","type":"email","value":"a@b.c","provider":""}],"accounts":[],"deleted":{}}""")
    assertNull(d.identities[0].via)
    assertNull(d.identities[1].provider)
    val text = encodeAtlas(d)
    assertFalse(text, text.contains("\"via\""))
    assertEquals(1, Regex("\"provider\"").findAll(text).count())
  }

  @Test fun oldBackupLoadsUnchanged() {
    val d = decodeAtlas("""{"identities":[{"id":"a","type":"email","value":"a@b.c"},{"id":"b","type":"phone","value":"1"}],"accounts":[],"deleted":{}}""")
    assertFalse(d.identities.any { it.isLogin })
    val text = encodeAtlas(d)
    assertFalse(text.contains("provider")); assertFalse(text.contains("via"))
  }

  @Test fun validation() {
    assertTrue(validLogin("wechat", "a"))
    assertFalse(validLogin("wechat", "  "))
    assertTrue(validLogin("apple", "me@icloud.com"))
    assertTrue(validLogin("apple", "+86 138 0000 1234"))
    assertFalse(validLogin("apple", "1234"))
    assertFalse(validLogin("apple", "me@icloud"))
    assertFalse(validLogin("apple", "hello"))
  }

  @Test fun kindsOrder() {
    assertEquals(listOf("email", "phone", "login"), KINDS)
    assertTrue(kindRank("login") > kindRank("phone"))
    assertEquals("#07c160", providerColor("wechat")); assertEquals("#1d1d1f", providerColor("apple"))
  }
}
