# ELIO TV 1.8.5

Version code 19. Adds 29 Nickelodeon-family variants to Anak & Keluarga. The prior 102 entries are unchanged. Total 131 entries.

41 candidate sources were tested previously with 30 yielding three decoded video frames. All 30 were retested for this release: 29 succeeded; Nick Jr Russia failed with a non-HLS response and was omitted. Downloaded media segments were decoded with FFmpeg, including ordinary HLS AES-128 playback using normally served keys; no DRM extraction or authentication bypass. Preview frames of all 29 successful sources were inspected. Native APK playback on a physical Android device was not tested.

Variant names are based on playlist feed labels, not verified spoken-language recognition. No entry claims Indonesian dubbing. Nickelodeon USA was observed carrying Nick at Nite (Friends), so a note explains its schedule-dependent programme change.

Indonesian candidates: MAXStream/IndiHome DASH returned HTTP 403; MNC DASH and the replacement public HTTP UDP relay linked in iptv-org issue 37808 returned HTTP 503. No verified Indonesian-language IPTV stream was added. These results are environment-specific and do not establish permanent unavailability.

Source: iptv-org categories/kids.m3u. Existing touch controls, TV remote navigation, EPG and logos remain.
