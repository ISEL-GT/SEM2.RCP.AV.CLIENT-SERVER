package com.github.iselgt.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * This data class is responsible for holding and providing data related to HTTP responses
 */
data class Response(val code: Int, private val date: String,  val server: String,
                     val location: String,  val length: Int,  val type: String, val data: String) {

    companion object {

        /**
         * Creates a response from a given dictionary of values.
         *
         * @param map The dictionary used to create the Response object
         * @return The new response object
         */
        fun fromDictionary(map: HashMap<String, String>): Response =
            Response(map.getOrDefault("code", "").toInt(), map.getOrDefault("date", ""),
                     map.getOrDefault("server", ""), map.getOrDefault("location", ""),
                     map.getOrDefault("content-length", "").toInt(), map.getOrDefault("content-type", ""),
                     map.getOrDefault("data", ""))
    }

    /**
     * Gets a Date object from the date returned in the HTTP Response
     *
     * @return A Date object with the date of the HTTP Request
     */
    fun getDate(): Date {

        val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
        return format.parse(this.date)
    }

}
