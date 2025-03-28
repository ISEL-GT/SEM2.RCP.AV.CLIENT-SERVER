package org.example

import org.example.client.Client

/**
 * Main program entrypoint
 */
fun main() {

    val client = Client("127.0.0.1")
    print(client.get_page().data)
}