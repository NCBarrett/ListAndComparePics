package org.example.listandcomparepics.model;

import java.util.ArrayList;
import java.util.List;

/// A GarmentComponent is a physically distinct, attached piece of the
/// garment — a string, hip band, strap, trim, bow, etc. — as opposed to
/// a Zone, which describes the fabric's own printed/dyed pattern.
///
/// A single garment piece can have any number of components (a top might
/// have a neck string, a back string, and a front-tie, each tracked as a
/// separate instance).
public class GarmentComponent {

    /// Open-ended by design. Common values seen so far: "string",
    /// "hip-band", "strap", "trim", "bow", "clasp". New types get added
    /// as new garments require them — this isn't a fixed enum on
    /// purpose, since new component types kept appearing throughout
    /// the design session.
    public String type;

    /// Where on the garment this specific instance is, when it matters —
    /// especially relevant for strings/straps. Examples: "neck", "back",
    /// "side", "left hip", "front-tie".
    public String subLocation;

    public Presence presence = Presence.UNKNOWN;

    /// Ordered list of colors. A single entry means one solid color;
    /// multiple entries describe a transition along the component's
    /// length (e.g., ["gold", "black"] for a string that starts gold
    /// and transitions to black).
    public List<String> colors = new ArrayList<>();

    /// If true, this component's color should be treated as inherited
    /// from a zone rather than read from `colors` directly. See
    /// matchesZoneRole to know which zone, and matchesFromIndex /
    /// matchesToIndex if only part of that zone's color sequence
    /// applies (e.g., a string matching only the middle "leopard"
    /// segment of a three-part gradient panel).
    public boolean matchesFabric = false;
    public ZoneRole matchesZoneRole;

    /// Optional: if matchesFabric is true but only PART of the
    /// matched zone's color sequence applies, these mark that range
    /// (inclusive) within the zone's colors list. Leave both -1
    /// (default) to mean "matches the zone's full color sequence."
    public int matchesFromIndex = -1;
    public int matchesToIndex = -1;

    /// Free-text note for anything about this component worth
    /// remembering that doesn't fit the structured fields — e.g.,
    /// "converges into a shared back strap", "hangs loosely at hip".
    public String routeNote;

    public GarmentComponent() {
    }

    public GarmentComponent(String type, String subLocation) {
        this.type = type;
        this.subLocation = subLocation;
    }

    public void addColor(String color) {
        colors.add(color);
    }
}
