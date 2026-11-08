# Improved Overheads

Customize overhead chat, HP, prayer points, hitsplats, and head icons from the Chat, Bars, Icons, Hits and More sidebar tabs. Select My player, Other players or NPCs before editing. Each actor group keeps its own appearance settings; Universal applies only the selected component when you click Apply to all.

* Health, Prayer, Run energy and Special energy have separate sections. Choose Numbers only, Bar only or Both for each supported resource.
* Hide HP independently for yourself, NPCs, or other players. Choose current, current / maximum, or percentage readouts for your own HP and prayer.
* Place overheads above, below, left, or right of the actor, with adjustable offsets and optional camera zoom scaling.
* Use horizontal or vertical HP and prayer bars, with OSRS artwork or a modern appearance.
* Give prayer its own style, fill direction, background, border, and corner radius.
* Customize fonts, colors, opacity, gradients, borders, damage trails, and HP segments.
* Resize prayer icons and add round or square backgrounds with separate background and border colors.
* Customize overhead chat fonts, colors, shadows, effects, and relationship colors.
* Adjust hitsplat size, placement, layout, lifetime, opacity, and visibility.
* Choose OSRS, Modern, or numbers-only damage and miss hitsplats. Special encounter indicators retain their artwork.
* Customize Modern hitsplat backgrounds for damage, misses, poison, venom, and burn, with separate damage/miss/status number colors, borders, rounded corners, and shadows.
* Scroll hitsplats upward or downward in one to four columns, with adjustable travel distance and spacing.
* Open Hits → Hitsplat themes to start from a preset or save up to 12 named themes. Copy and import theme JSON with a change preview; themes leave hit filters and other overhead components unchanged.
* Preview HP and prayer restored by the inventory item under your pointer, using RuneLite's Item Stats calculations.
* Add low-HP and low-prayer colors, plus an optional prayer tick sweep that flashes when your points decrease.
* Optionally hide the local run-energy and special-attack displays independently while full. Numbers and bars return as soon as energy drops.
* Add local run-energy and special-attack meters, each with numbers, a bar, both, or hidden; independent styling, position, orientation, opacity, and zoom scaling.
* Show names independently for yourself, other players, and NPCs, with always, current-target, in-combat, and hidden choices.
* Restrict NPC and other-player HP to your target or combat, and exclude ordinary NPC HP by ID.
* Place prayer icons independently of skulls and other head icons.
* Show local poison (P), venom (V), and disease (D) badges, or a target (+) badge on the NPC you are currently interacting with.
* Avoid overlaps on every side of an actor, including text beside vertical bars. Continuously visible components retain their spacing as chat and hitsplats appear or expire. Adjust the gap or use Health → Spacing → Reflow spacing to compact the layout again.
* Save up to 12 named setups per RuneLite profile in the Setups window. Export and copy JSON, paste it into Setups on another profile, preview the changes, then apply them immediately.
* Use Undo last apply in the Setups or Hitsplat themes window to reverse its latest applied changes. Unrelated settings stay as they are; conflicting edits or a profile switch prevent the undo.
* Copy HP, name, and hitsplat visibility between actor groups through the same preview. Shared appearance controls remain shared.

* Reset individual position offsets with the reset button. Health → Spacing provides local alignment outlines and optional viewport containment.
* Use separate name colors for yourself, friends, clan/friends-chat members, RuneLite party members, other players, shops and followers.
* Show your active offensive prayers with the same configurable prayer frames and positioning. Other players retain only their visible overhead icons.
* Add estimated HP regeneration and poison/venom timers as numbers, bars, or both. HP timing requires RuneLite's Regen Meter to be enabled; independent HP-cape and Rejuvenation timing stays hidden. Poison timing begins after a visible poison/venom hit and stops at cure or when an expected hit is overdue.
* Opt into My player → More → Timers and party to share your name, HP, prayer, run and special energy through your existing RuneLite party. Customize received resources under Other players → Bars, independently for each resource. Values marked `~` are last received; RuneLite rounds run energy. Old values expire after the configured lifetime; Refresh party data requests a fresh snapshot. Setup imports never enable or disable this sharing option.
* Preserve complete original overheads for additional NPCs using Native NPC UI IDs, including transformed IDs. This is separate from hiding ordinary HP.

HP numbers are exact for your own player. Ordinary NPC and other-player numbers show visible health percentages; opted-in party displays show received member values. Special encounter shields and charge bars retain their native display.

The prayer sweep follows observed game ticks; it does not predict when prayer will drain. The target badge follows your current interaction. Restore previews show immediate positive restoration; the extra amount remains readable in number displays when it exceeds the normal bar maximum.

Chat Filter's removed or replaced actor text is respected. Locally typed chat color/effect prefixes are supported; other players' original color/effect prefixes are not available through RuneLite's public API.

This plugin replaces the actor overhead display. Turning off a component hides it; disabling the plugin restores the native display. Other complete overhead renderers, including Customize a Lot and Custom HP Bar, are declared conflicts.

Based in part on Customize a Lot by Ruined Heir, under the BSD 2-Clause license. See LICENSE and bundled third-party license notices.
