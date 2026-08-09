package org.example.listandcomparepics.model;

import java.util.ArrayList;
import java.util.List;

/// A Layer represents ONE physical garment worn at a given position
/// (Top, Bottom, or Whole for one-piece outfits). Most of the time a
/// GarmentPiece has exactly one Layer — but some photos show a garment
/// worn over another (e.g. a shirt over a fully-visible bikini top,
/// or shorts over a visible bikini bottom), and when BOTH are visible
/// and taggable, each gets its own Layer rather than being squeezed
/// into one.
///
/// A garment that's merely SUSPECTED to be underneath (a strap peeking
/// out, an inferred bikini top under an opaque shirt) does NOT get its
/// own Layer — it stays a note on the visible layer's rawNotes field,
/// since there's nothing concrete enough to tag as a real Zone.
/// Promote it to a real Layer only once it's actually visible enough
/// to describe.
public class Layer {

    /// e.g. "Shirt", "Bikini top", "Lingerie", "Pants"
    public String clothesType;

    public List<Zone> zones = new ArrayList<>();
    public List<GarmentComponent> components = new ArrayList<>();

    /// Free-text fallback for anything about THIS layer that the
    /// current UI/fields can't yet capture structurally, or that isn't
    /// worth full structured tagging (e.g. a garment glimpsed but not
    /// clearly enough to build a real Zone from).
    public String rawNotes;

    public Layer() {
    }

    public Layer(String clothesType) {
        this.clothesType = clothesType;
    }

    public Zone addZone(ZoneRole role) {
        Zone zone = new Zone(role);
        zones.add(zone);
        return zone;
    }

    public GarmentComponent addComponent(String type, String subLocation) {
        GarmentComponent component = new GarmentComponent(type, subLocation);
        components.add(component);
        return component;
    }

    public Zone getZone(ZoneRole role) {
        for (Zone zone : zones) {
            if (zone.role == role) {
                return zone;
            }
        }
        return null;
    }
}