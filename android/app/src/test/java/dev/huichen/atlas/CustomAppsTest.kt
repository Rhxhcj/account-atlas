package dev.huichen.atlas

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CustomAppsTest {
  private val png = "data:image/png;base64,iVBORw0KGgo="
  private val presets = setOf("微信", "qq")
  private fun merge(saved: List<List<String>>, accounts: List<Account>, hidden: Set<String> = emptySet()) =
    mergeCustomApps(saved, accounts, hidden) { it.lowercase() in presets }

  @Test fun savedFirstThenAccountsNewestFirstWithoutPresets() {
    val r = merge(listOf(listOf("Foo", "foo.com", "工具", "", png), listOf("微信", "", "社交")),
      listOf(Account("a", "Bar", "bar.com", "开发", icon = png), Account("b", "baz"), Account("c", "QQ"), Account("d", "foo")))
    assertEquals(listOf("Foo", "baz", "Bar"), r.map { it.preset.name })
    assertEquals(png, r[0].icon)
    assertEquals("工具", r[0].preset.category)
    assertNull(r[1].icon)
    assertEquals("其他", r[1].preset.category)
    assertEquals(png, r[2].icon)
  }

  @Test fun hiddenNamesAreLeftOutEvenWithAccounts() {
    val r = merge(listOf(listOf("Foo", "foo.com", "工具")), listOf(Account("a", "Bar")), hidden = setOf("foo", "bar"))
    assertEquals(emptyList<String>(), r.map { it.preset.name })
  }

  @Test fun oldThreeElementRowsAndBadIconsStillWork() {
    val r = merge(listOf(listOf("Foo"), listOf("X", "", "", "", "not-an-image"), emptyList()), emptyList())
    assertEquals(listOf("Foo", "X"), r.map { it.preset.name })
    assertNull(r[1].icon)
    assertEquals("其他", r[1].preset.category)
  }
}
