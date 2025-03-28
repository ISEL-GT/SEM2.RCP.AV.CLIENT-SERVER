package com.github.iselgt.common

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.util.LinkedList


/**
 * This class is responsible for acting as the client, connecting its socket into a given IP address
 * and sending/requesting packets.
 *
 * @param hostname The server hostname to connect to (The IP Address)
 * @param port The server port to use
 */
class Client(val hostname: String, val port: Int = 80) {

    /**
     * The current client connection, aimed at the server, used to receive and send data.
     */
    public var connection: Socket? = null

    init {
        this.reconnect()
    }

    /**
     * Reconnects the socket in case it has been closed.
     */
    fun reconnect(): Boolean {
        val closedFlag = this.connection == null || this.connection!!.isClosed
        this.connection = if (closedFlag) Socket(hostname, port) else this.connection

        // Checks if the reconnection was successful
        if (!connection!!.isConnected || connection!!.isClosed) {
            println("COULD NOT CONNECT TO ${this.hostname}:${this.port} ")
            return false
        }

        if (closedFlag) println("CONNECTED/RECONNECTED TO ${this.hostname}:${this.port}")
        return true
    }

    /**
     * Sends one or more messages into the server through the socket.
     *
     * @param messages An array of messages to send into the socket, in order.
     */
    fun send(vararg messages: String): Boolean {

        // If a reconnection was unsuccessful, return false because we can't send the message
        if(!this.reconnect()) return false

        val writer = PrintWriter(this.connection!!.outputStream)

        // Write every message in the array in order
        for (message in messages)
            writer.write(message + "\r\n")

        writer.write("\r\n")
        writer.flush()
        return true
    }

    /**
     * Creates a response header dictionary mapping headers to values given a raw response string.
     *
     * @param rawResponse The raw response header list
     * @return A dictionary with every header mapped
     */
    fun createHeaderDictionary(rawResponse: LinkedList<String>): HashMap<String, String> {

        val responseDictionary = HashMap<String, String>()

        // Iterates through all the keys, split them by ":" and add them to the dictionary lowercased.
        while (true) {
            val header = rawResponse.pop()

            // Handle the response code differently, by parsing it out and adding it as "code"
            if ("HTTP/1.1" in header) {
                responseDictionary.put("code", header.replace("HTTP/1.1 ", "").substring(0, 3).trim())
                continue
            }

            // The website data will come after an empty line, and is always last, so handle it accordingly.
            if (header == "") {
                responseDictionary.put("data", rawResponse.joinToString("\n"))
                break
            }

            val splitHeader = header.split(":", limit=2)
            responseDictionary.put(splitHeader[0].trim().lowercase(), splitHeader[1].trim())
        }

        return responseDictionary

    }

    /**
     * Sends a GET Request to obtain the current page's data, in a raw manner.
     *
     * @param path The website's path to the page we want (hostname/path, ex: facebook.com/login)
     * @return The response data from the host
     */
    fun sendGetRequest(path: String): Response {

        // Initialises a response variable to store the data and sends the GET request
        var rawResponse = LinkedList<String>()

        if (!this.send("GET /${path} HTTP/1.1", "Host: ${this.hostname}", "Connection: keep-alive"))
            return Response(0, "", "", "", 0, "", "MESSAGE NOT SEND - BAD CONNECTION")

        // Initialises the reader, and until the data ends (null), add it to response
        val reader = BufferedReader(InputStreamReader(this.connection!!.inputStream))
        Thread.sleep(100)

        while(reader.ready()) {
            var data = reader.readLine() ?: break
            rawResponse.add(data)  // Filters the data by the useful data
        }

        // If the raw response is empty, something bad happened
        if (rawResponse.isEmpty()) return Response(0, "", "", "", 0, "", "NO RESPONSE")

        // Parses out the HTTP response headers and builds a Response
        val response = Response.fromDictionary(createHeaderDictionary(rawResponse))
        return response
    }


    /**
     * Sends a GET Request to obtain the current webpage's text. Follows any redirections accordingly.
     *
     * @param path The website's path to the page we want (hostname/path, ex: facebook.com/login)
     * @return The response data from the host
     */
    fun getPage(path: String = ""): Response {

        var response = this.sendGetRequest(path)

        // If a redirection code is returned, go to that location instead.
        if (response.code == 302) {
            val location = response.location.split("//${this.hostname}/")[1]
            response = this.sendGetRequest(location)
        }

        return response
    }

}