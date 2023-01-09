package com.introduct.uma.utils

object StringUtils {

    private const val EMAIL_REGEX = "^[A-Za-z](.*)(@)(.+)(\\.)(.+)"

    fun isEmail(string: String): Boolean = EMAIL_REGEX.toRegex().matches(string)
}