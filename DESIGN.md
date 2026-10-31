# ELIO TV television and film browser

## Remote/filter correction · test8

The category row has its own remote focus state between hero controls and channel grid. Its white focus outline is distinct from the baby-blue active underline. It remains reachable when a category is empty. Moving focus through header/filter controls must not itself stop the selected live preview. This corrects the navigation omission in the earlier design without changing the approved composition.

## Shared header correction · test7

Owner screenshots showed that test6 scale alignment alone did not make the header consistent: native Film controls still used different artwork dimensions, menu positions, filled button treatment and date format. TV and Film now invoke the exact same header renderer; Film supplies native transparent hit/focus targets over the shared positions. Categories use underline tabs in both sections. This replaces the separate film masthead described in older revisions.

## Viewport correction · test6

Owner device screenshots revealed inconsistent scaling between the TV Canvas and density-based native Film page. Both now share the centered 1280×720 design frame and uniform scale. Film no longer uses density-scaled dp/sp within that frame. Native touch coordinates are transformed with the parent view rather than manually adjusted. Wide screens keep side space; content is not stretched. Film clock/date are right aligned. This supersedes the test5 short-screen/density-based film layout description below.

## Approved concept · test5

The owner approved the TV + Film concept image: large preview left, programme details and watch/favorite actions right, six station cards below; a distinct film page with poster browsing and search. Test5 implements this composition using the owner's existing ELIO artwork. No concept-image video, fictional movie or illustrated poster is presented as live data.

TV top navigation is reduced to the five visible destinations. Channel categories remain below the hero. A single station row reserves space for a genuine 16:9 preview; vertical scrolling replaces the earlier two-row viewport. White borders identify the current remote target. Text-only category tabs avoid repeated capsules.

The film page uses a device clock, a separate heading/search band, actual provider posters and a compact settings menu. Its short-screen layout hides the heading and secondary header action, retains search/category controls, and keeps the poster grid scrollable. It combines installed providers by default; no new third-party extension is bundled in this design build.

## Direction supplied by the owner

ELIO TV is for Android TV, STB remote controls and Android phones. The owner requested an elegant, professional, simple interface without generic AI decoration, retaining ELIO branding and its baby-blue direction. The film page should show actual movie posters, categories and search, alongside the existing television app. A movie-app reference was offered as inspiration, not a request to copy its code or branding.

## Product decisions

- Screen job: choose a provider, browse or search its titles, inspect details, then watch.
- Dark navy follows the existing ELIO viewing environment and avoids a bright screen next to playback. Baby blue marks selection and keyboard focus.
- Posters are the primary content, not decorative illustrations. Missing artwork explicitly says “Tanpa poster”.
- A uniform poster grid gives predictable D-pad movement between equivalent titles. Details use a different poster/text composition because the task changes to evaluating one title.
- System sans-serif keeps Indonesian labels readable and uses no extra font download. Titles have more weight than provider metadata.
- Touch controls reserve 44 dp targets; short landscape screens collapse the extra category row into a category selector so content retains height.
- No auto-playing decorative hero, looping animation, fabricated rating, trending badge, or invented film count. Counts come from the loaded response.
- Categories use actual provider content types. Genres appear only when the detail response contains tags. Collection names come from the provider.
- Dials inferred from the owner's brief: ENERGY 2, RHYTHM 2, MOTION 1. Contrast, poster content and a compact ELIO header carry identity; focus feedback replaces decorative animation.

## Scope

This direction now includes television home, station cards, playback overlays, channel drawer and app dialogs. Station logos and ELIO branding are retained. The APK remains a test build until the owner checks device navigation and rendering.

## Television revision · test4

- A solid graphite canvas replaces the decorative navy background gradient. Navy panels separate the selected station from the catalog without competing with the live preview.
- The selected channel, current programme, next programme and watch actions have separate baselines. Long titles truncate within the text column; they never cross into the preview.
- Small rectangular category controls replace repeated capsules. A white outer focus ring marks the remote destination; baby blue marks the active category.
- Muted programme text remains readable against the dark cards. Artwork is the channel identity; no substitute illustration is generated.
- Player shading remains a functional contrast scrim over unpredictable video. Dialog colors carry the same identity into settings and source selection.
- Geometry and D-pad routes are preserved in this revision to avoid introducing a new navigation model while fixing provider fallback. Canvas controls still need device accessibility and touch-size validation; no AA compliance claim is made.
