package com.example.hkradio

data class ChannelData(
    val id: String,
    val artist: String,
    val name: String,
    val link: String,
    val artwork: Int) {

  companion object {
      val data = listOf(
          ChannelData("RTHK-1", "香港電台", "香港電台第一台", "https://rthkradio1-live.akamaized.net/hls/live/2035313/radio1/index_64_a.m3u8", R.drawable.rthk1),
          ChannelData("RTHK-2", "香港電台", "香港電台第二台", "https://rthkradio2-live.akamaized.net/hls/live/2040078/radio2/index_64_a.m3u8", R.drawable.rthk2),
          ChannelData("RTHK-3", "香港電台", "香港電台第三台", "https://rthkradio3-live.akamaized.net/hls/live/2040079/radio3/index_64_a.m3u8", R.drawable.rthk3),
          ChannelData("RTHK-4", "香港電台", "香港電台第四台", "https://rthkradio4-live.akamaized.net/hls/live/2040080/radio4/index_64_a.m3u8", R.drawable.rthk4),
          ChannelData("RTHK-5", "香港電台", "香港電台第五台", "https://rthkradio5-live.akamaized.net/hls/live/2040081/radio5/index_64_a.m3u8", R.drawable.rthk5),
          ChannelData("Metro", "新城廣播", "新城知訊台", "https://1931121536.rsc.cdn77.org/1931121536/tracks-a1/mono.m3u8", R.drawable.metroinfo),
          ChannelData("Metro-Finance", "新城廣播", "新城財經台", "https://1295896862.rsc.cdn77.org/1295896862/tracks-a1/mono.m3u8", R.drawable.metrofinance),
          ChannelData("Metro-Plus", "新城廣播", "新城采訊台", "https://1435271937.rsc.cdn77.org/1435271937/tracks-a1/mono.m3u8", R.drawable.am1044),
      )
  }
}