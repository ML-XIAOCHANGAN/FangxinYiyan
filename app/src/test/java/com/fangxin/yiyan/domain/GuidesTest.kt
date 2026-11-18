package com.fangxin.yiyan.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GuidesTest {
    @Test
    fun `has six guides`() {
        assertEquals(6, Guides.ALL.size)
    }

    @Test
    fun `every guide has steps and copy`() {
        for (guide in Guides.ALL) {
            assertTrue(guide.id.isNotBlank())
            assertTrue(guide.title.isNotBlank())
            assertTrue(guide.minutes.isNotBlank())
            assertTrue(guide.steps.isNotEmpty())
            for (step in guide.steps) {
                assertTrue(step.text.isNotBlank())
                assertTrue(step.tip.isNotBlank())
            }
        }
    }

    @Test
    fun `every category has at least one guide`() {
        for (category in Guides.CATEGORIES) {
            assertTrue(Guides.byCategory(category.name).isNotEmpty())
        }
    }

    @Test
    fun `unknown guide is null`() {
        assertNull(Guides.find("not-here"))
    }
}
