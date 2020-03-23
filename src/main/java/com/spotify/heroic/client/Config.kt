package com.spotify.heroic.client

class Config(
    val clientId: String,
    val connectTimeoutSeconds: Int,
    val readTimeoutSeconds: Int
) {
    private constructor(builder: Builder) :
        this(builder.clientId,
            builder.connectTimeoutSeconds,
            builder.readTimeoutSeconds)

    class Builder {
        var clientId: String = "heroic-client-java"
            private set
        fun setClientId(clientId: String)  = apply { this.clientId = clientId }

        var connectTimeoutSeconds: Int = 60
            private set
        fun setConnectTimeoutSeconds(connectTimeoutSeconds: Int)  = apply {
            this.connectTimeoutSeconds = connectTimeoutSeconds
        }

        var readTimeoutSeconds: Int = 300
            private set
        fun setReadTimeoutSeconds(readTimeoutSeconds: Int)  = apply {
            this.readTimeoutSeconds = readTimeoutSeconds
        }

        fun build() = Config(this)
    }
}
