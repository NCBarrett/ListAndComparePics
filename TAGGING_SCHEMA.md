# Bikini Tagging Schema — Design Reference

This document captures the tagging data model developed through extensive
example-driven testing. It's meant to be read alongside the Java data model
classes (`Zone.java`, `GarmentComponent.java`, `GarmentPiece.java`,
`SubjectProfile.java`, `TaggingEnums.java`).

## 1. Subject-Level Traits

Tracked once per Girl ID, constant across every photo of that subject.
Already implemented in `Controller.lookupAndPopulateSubjectTraits()`, which
scans historical files sharing the same ID and reuses any trait found on a
prior photo, even if the current photo doesn't show it.

- Eye color
- Hair color
- Bra size

Each is either a real value or `Unknown` — never left ambiguous with "not yet
entered."

## 2. Per-Garment-Piece Structure

Every garment piece (Top, Bottom) is described independently, and is broken
into two parallel, independent categories:

### 2a. Zones — the fabric's own printed/dyed design

A **zone** is a region of the fabric itself, with its own pattern and colors.
Three zone roles are used:

- **Interior** — the main body of the panel
- **Border** — the panel's edge treatment
- **Accent** — a secondary patterned area elsewhere on the panel (location
  noted per instance — center-radiating, inset-triangle, left-side, etc. —
  never assumed to be literally centered)

A panel can have any combination of these zones present at once (e.g.,
Interior + Border + Accent, all simultaneously, all independently
searchable).

Each present zone records:

- **Pattern category:** Simple, or Main with Compliments
- **Simple subtype** (if Simple): Full color, or Equal stripes
- **Compliments subtype** (if Main with Compliments): Dots/Figures, Stripes,
  Floral, Plaid, Fringed, Other (+ free-text note if Other)
- **Color mode:** Ranked (there's a real dominant-to-accent hierarchy) or
  Equal (colors are co-dominant — used for Equal stripes, but also valid for
  Main with Compliments zones where nothing reads as more dominant, e.g.
  gold/blue "alternating fluidly" leopard spots)
- **Colors:** an ordered list, capped at 5, ranked by prominence when Ranked
  mode applies. Anything beyond 5 distinct colors is dropped, keeping only
  the 5 most prominent.
- **Material note:** optional free text, used sparingly (see Rule: Drop Fine
  Detail below) — not for texture/translucence, but for genuinely
  identity-relevant notes you want preserved for your own reference.

### 2b. Components — physically distinct attached pieces

A **component** is something attached to or wrapping the garment,
independent of the fabric's own printed pattern: strings, hip bands, straps,
trim, bows/clasps, etc. The type/role list is intentionally open — new types
get added as new garments require them (trim and hanging-trim were both
added this way).

Each component instance records:

- **Type/role:** string, hip-band, strap, trim, bow/clasp, etc.
- **Sub-location** (for strings especially): neck, back, side, front-tie
- **Presence:** Present / Absent / **Unknown** (tri-state — see Rule: Unknown
  Is a Real Value)
- **Colors:** an ordered list — supports a single color, or a transition
  sequence along the component's length (e.g., gold → black on a neck
  string)
- **Matches-fabric flag:** if true, the component's color(s) are inherited
  from a specific zone rather than entered separately. This can point at
  part of a zone's color sequence, not necessarily the whole thing (e.g.,
  strings matching only the leopard segment of a black→leopard→white
  gradient panel).
- **Location/route note:** free text where useful (e.g., "converges into a
  shared back strap," "left side only")

### 3. Scene

Closes out the tag string. Unchanged from the original design — not
elaborated on during this session.

## General Rules

**Unknown is a real value.** Distinct from "absent" and from "not yet
entered." Applies everywhere: subject traits, zone colors, component
presence. A component that's simply out of frame in a photo is Unknown, not
Absent.

**Infer consistency only from multiple, independent confirmations.** If a
pattern is visible on two or more separate, non-adjacent parts of the same
garment (e.g., both the panel and the visible strings), it's reasonable to
infer the same pattern continues into a hidden part (e.g., an obscured
string). A single data point is not evidence of a repeating design choice —
don't extrapolate from just one visible edge.

**Closest-resemblance categorization.** Pick the nearest existing subtype
rather than inventing a new one for a minor visual quirk. Expand the
subtype list only when a genuinely recurring new category shows up (Plaid,
Dots/Figures, and Fringed were all added this way, after repeated real
examples, not preemptively).

**Drop fine detail that doesn't aid searching.** Exact shape, texture
(e.g., "frilly"), color gradients, and precise measurements/proportions
are consciously left out of the structured data. If they matter to you at
all, they belong in an optional free-text note, never as their own
structured field.

**Best-guess-and-move-on for close calls.** When a color is ambiguous
between two close candidates (e.g., white vs. silver), pick one and
optionally note the ambiguity in free text. Don't add multi-value
complexity to a field for this.

**Cap colors at 5 per zone,** ranked by prominence. Anything beyond that is
dropped, not stored.

**Salience drives the tag *string*, not the underlying data.** Every zone
and component that's present gets recorded in the data regardless of visual
prominence, so zone-specific and component-specific search stays possible
(e.g., "border color = red" as a distinct query from "any color = red").
The human-readable sentence generated *from* that data can lead with
whatever's most visually striking, but nothing is dropped from the data
itself to achieve that phrasing.

**A single physical piece can be more than one thing at once.** A strap
that also forms a panel's edge doesn't need to be classified as either "a
string" or "part of the panel" — it can be recorded as a string component
*and* contribute to that zone's fabric coloring, without needing an exact
geometric boundary drawn between the two.

## Open Items / Deliberately Deferred

- **Emblem/appliqué patterns:** on inspection, cases that looked like a
  separate decorative emblem turned out to just be a second zone (an
  Accent zone) — dyed into the same fabric as everything else, not a
  distinct attached piece. No separate "emblem" component type was added;
  use an Accent zone instead.
- **Ambiguous zone-vs-component elements:** if you can't tell whether
  something is fabric pattern or an attached piece (e.g., "trim or accent,
  I can't determine" on a striped bikini), it's fine to record the color
  and location without forcing a category decision. Leave the
  classification field blank/unknown rather than guessing.
- **Multi-region zones beyond Interior/Border/Accent:** not built. If a
  garment genuinely needs more than three named zones, that's a real
  schema extension to design deliberately, not something to force into the
  current three.
