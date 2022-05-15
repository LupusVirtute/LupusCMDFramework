package org.lupus.commands.core.utils

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.lupus.commands.core.messages.I18n
import java.util.*

class StringUtilTest {
    val keyTested = "Something"
    val keyTestedWithSyntax = "<$keyTested>"
    val valueTested = "I love cats"
    val localeTested = "en"

    @Before
    fun beforeTest() {
        val props = Properties()
        props[keyTested] = valueTested
        I18n[null] = mutableMapOf()
        I18n[null]!![localeTested] = props
    }

    @Test
    fun `Is i18n syntax correct`() {
        val i18nSyntax = keyTestedWithSyntax
        assertTrue( StringUtil.isThatI18nSyntax(i18nSyntax) )
    }

    @Test
    fun `Is i18n syntax bad`() {
        val i18nSyntax = keyTestedWithSyntax.drop(1)
        assertFalse( StringUtil.isThatI18nSyntax(i18nSyntax) )
    }

    @Test
    fun `Is i18n syntax parsed correctly?`() {
        val i18nSyntax = keyTestedWithSyntax
        val synt = StringUtil.getI18nSyntax(null, mutableListOf(i18nSyntax))
        assertEquals(valueTested, synt.getI18nUnformatted())
    }

    @Test
    fun `getI18nSyntax recognizes bad syntax`() {
        val i18nSyntax = keyTestedWithSyntax.drop(1)
        assertThrows(
            IllegalArgumentException::class.java) {
            StringUtil.getI18nSyntax(null, mutableListOf(i18nSyntax))
        }
    }

    @Test
    fun `getI18nSyntax empty input`() {
        val i18nSyntax = ""
        assertThrows(java.lang.IllegalArgumentException::class.java) {
            StringUtil.getI18nSyntax(null, mutableListOf( i18nSyntax ))
        }
    }

    @After
    fun clear() {
        I18n.clear()
    }
}