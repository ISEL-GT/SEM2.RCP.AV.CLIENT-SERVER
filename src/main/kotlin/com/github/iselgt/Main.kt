package com.github.iselgt

import com.github.iselgt.common.Client

/**
 * Main program entrypoint
 */
fun main() {

    val client = Client("127.0.0.1")
    print(client.getPage().data)
}