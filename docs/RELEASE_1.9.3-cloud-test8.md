# ELIO TV 1.9.3-cloud-test8

Owner-test fix for remote category navigation and preview lifecycle after the TV redesign. Do not publish this APK until the owner checks it on their TV.

## Remote filters

The category row is now a distinct focus target. From the first channel row press Up to enter filters, Left/Right to select Semua/Nasional/Internasional/Olahraga/Favorit, then OK to apply. Down returns to channel cards. From the hero controls, Down also enters filters. Up from filters returns to hero; empty results still permit changing category. A white border marks the focused filter, separate from the active category underline. Long OK on a filter does not change a channel favorite.

## Preview

Header/filter navigation no longer releases the selected channel preview. Preview stops for actual playback, Film, backgrounding or an empty category. Channel changes debounce for 350 ms. The last server that delivered a first frame is tried first, followed by all remaining servers in a fixed circular order. Preview does not inherit the player's temporary failure blacklist. Cold start allows 9 seconds per server instead of 5. DRM sources remain excluded. No stream URL or source authentication was changed.

The exhausted-preview message now describes the preview state instead of substituting an availability warning. Actual source availability, geographical restrictions and Android TV codec/device behavior can still prevent a preview. This fix does not guarantee every channel works.

## Verification

158 JVM logic checks pass, including 41 new checks for filter traversal/bounds, preview lifecycle eligibility and preferred-server permutations. MainActivity routing was reviewed separately. Android release compilation, signing and package checks are recorded with delivery. No emulator or physical device is available for remote clicks or actual preview playback; runtime results remain unverified.

## Owner check

1. Install over test7, focus the first channel and press Up.
2. Press Right twice to Internasional and OK. Confirm only international channels remain.
3. Press Down into channels; return Up to change category, including empty Favorit.
4. On a channel with a working preview, navigate hero → filters → header and confirm navigation itself no longer restarts the video.
5. Change channel and allow up to 9 seconds per server. Compare Preview with Tonton for any source still failing; report the channel and server.

No new GitHub release is published by this task.
