package com.example.hkradio

object Config {
    // Trailing slash so relative paths ("images/rthk1.png", "channels.json") concatenate cleanly.
    const val CDN_BASE = "https://d3lrawfon3stql.cloudfront.net/"
    const val CHANNELS_URL = CDN_BASE + "channels.json"
}
