package com.example.data

import kotlin.math.abs

object MathEvaluator {
    fun evaluate(rawInput: String): String? {
        val expr = rawInput.trim().replace("×", "*").replace("÷", "/")
        if (expr.isEmpty() || !expr.any { it.isDigit() }) return null
        if (!expr.any { it == '+' || it == '-' || it == '*' || it == '/' || it == '%' }) return null

        return try {
            val result = Parser(expr).parseExpression()
            if (result.isNaN() || result.isInfinite()) return null
            if (abs(result - result.toLong()) < 1e-9) {
                result.toLong().toString()
            } else {
                String.format("%.4f", result).trimEnd('0').trimEnd('.')
            }
        } catch (_: Exception) {
            null
        }
    }

    private class Parser(private val input: String) {
        private var pos = 0

        private fun skipWhitespace() {
            while (pos < input.length && input[pos].isWhitespace()) pos++
        }

        fun parseExpression(): Double {
            var value = parseTerm()
            while (true) {
                skipWhitespace()
                if (pos >= input.length) break
                when (input[pos]) {
                    '+' -> { pos++; value += parseTerm() }
                    '-' -> { pos++; value -= parseTerm() }
                    else -> break
                }
            }
            return value
        }

        private fun parseTerm(): Double {
            var value = parseFactor()
            while (true) {
                skipWhitespace()
                if (pos >= input.length) break
                when (input[pos]) {
                    '*' -> { pos++; value *= parseFactor() }
                    '/' -> {
                        pos++
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Division by zero")
                        value /= divisor
                    }
                    '%' -> {
                        pos++
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Modulo by zero")
                        value %= divisor
                    }
                    else -> break
                }
            }
            return value
        }

        private fun parseFactor(): Double {
            skipWhitespace()
            if (pos >= input.length) throw IllegalArgumentException("Unexpected end")
            return when (val ch = input[pos]) {
                '+' -> { pos++; parseFactor() }
                '-' -> { pos++; -parseFactor() }
                '(' -> {
                    pos++
                    val value = parseExpression()
                    skipWhitespace()
                    if (pos < input.length && input[pos] == ')') pos++
                    value
                }
                else -> {
                    if (!ch.isDigit() && ch != '.') throw IllegalArgumentException("Unexpected char: $ch")
                    val start = pos
                    while (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) pos++
                    input.substring(start, pos).toDouble()
                }
            }
        }
    }
}
