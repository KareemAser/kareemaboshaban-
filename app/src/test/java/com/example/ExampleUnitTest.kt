package com.example

import com.example.data.MathEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun mathEvaluator_calculatesExpressionsAccurately() {
        assertEquals("100", MathEvaluator.evaluate("25 * 4"))
        assertEquals("14", MathEvaluator.evaluate("2 + 3 * 4"))
        assertEquals("20", MathEvaluator.evaluate("(2 + 3) * 4"))
        assertEquals("3.5", MathEvaluator.evaluate("7 / 2"))
        assertNull(MathEvaluator.evaluate("chrome"))
        assertNull(MathEvaluator.evaluate("10 / 0"))
    }
}
