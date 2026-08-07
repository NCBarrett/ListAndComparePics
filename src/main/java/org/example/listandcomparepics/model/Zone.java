package org.example.listandcomparepics.model;

import java.util.ArrayList;
import java.util.List;

/// A Zone is a region of the garment's own printed/dyed fabric design —
/// NOT a physically attached piece (see GarmentComponent for that).
///
/// A garment piece (top or bottom) can have any combination of the three
/// ZoneRole values present at once. Each is tracked completely
/// independently — a panel might have an Interior zone, a Border zone,
/// and an Accent zone, each with its own pattern and colors.
public class Zone {

    public ZoneRole role;

    /// Free-text note on where this zone sits, mainly useful for
    /// ACCENT zones, since "accent" doesn't imply a fixed position
    /// the way Interior/Border do.
    /// Examples: "center, radiating outward", "left side", "inset triangle"
    public String locationNote;

    public PatternCategory patternCategory;

    /// Only meaningful if patternCategory == SIMPLE
    public SimpleSubtype simpleSubtype;

    /// Only meaningful if patternCategory == MAIN_WITH_COMPLIMENTS
    public ComplimentSubtype complimentSubtype;

    /// Only used if complimentSubtype == OTHER
    public String otherSubtypeNote;

    public ColorMode colorMode = ColorMode.RANKED;

    /// Ordered list of colors. If colorMode == RANKED, index 0 is the
    /// most prominent/dominant color, and so on. Capped at 5 entries —
    /// per the schema's rule, anything beyond the 5 most prominent
    /// colors is dropped rather than stored.
    public List<String> colors = new ArrayList<>();

    /// Optional free-text note for anything worth remembering that
    /// doesn't belong as a structured field — used sparingly, per the
    /// "drop fine detail" rule in TAGGING_SCHEMA.md.
    public String materialNote;

    public Zone() {
    }

    public Zone(ZoneRole role) {
        this.role = role;
    }

    /// Adds a color to this zone's list, silently ignoring anything
    /// past the 5-color cap rather than throwing — matches the
    /// schema's "cap at 5, drop the rest" rule.
    public void addColor(String color) {
        if (colors.size() < 5) {
            colors.add(color);
        }
    }
}
