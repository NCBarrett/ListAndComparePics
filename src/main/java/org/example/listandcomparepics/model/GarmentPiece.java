package org.example.listandcomparepics.model;

import java.util.ArrayList;
import java.util.List;

/// Represents one piece of the garment — a Top or a Bottom — holding
/// every Zone (fabric pattern regions) and GarmentComponent (attached
/// pieces) that describe it.
public class GarmentPiece {

    public enum PieceType {
        TOP,
        BOTTOM
    }

    public PieceType pieceType;

    public List<Zone> zones = new ArrayList<>();
    public List<GarmentComponent> components = new ArrayList<>();

    /// Free-text fallback for anything observed on this piece that the
    /// current UI/fields can't yet capture structurally — e.g. a photo
    /// has an Accent zone or an unusual component before that part of
    /// the UI has been built. This lets tagging proceed on ALL photos
    /// immediately, without needing to pre-sort which ones are "simple
    /// enough" for whatever fields currently exist.
    ///
    /// Treat this as a queue: once the UI/model grows to cover a kind
    /// of detail that's been piling up here in plain language, go back
    /// through existing entries with non-empty rawNotes and convert the
    /// relevant ones into proper structured fields (Zones, Components,
    /// etc.), then clear or trim the note.
    public String rawNotes;

    public GarmentPiece(PieceType pieceType) {
        this.pieceType = pieceType;
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

    /// Convenience lookup — returns the zone with the given role if one
    /// exists on this piece, or null if that zone wasn't recorded.
    /// Useful for a future string-builder that needs to check "does
    /// this piece even have a Border zone?" before describing one.
    public Zone getZone(ZoneRole role) {
        for (Zone zone : zones) {
            if (zone.role == role) {
                return zone;
            }
        }
        return null;
    }

    /// Flattens every color from every present zone into one list.
    /// This is the simplification the design session settled on for
    /// general "does this photo contain color X anywhere" searches —
    /// it deliberately loses which zone each color came from. If
    /// zone-specific search is needed later (e.g. "border color = red"
    /// as distinct from "any color = red"), search the zones directly
    /// via getZone(...) instead of using this method.
    public List<String> getAllColorsFlattened() {
        List<String> all = new ArrayList<>();
        for (Zone zone : zones) {
            all.addAll(zone.colors);
        }
        return all;
    }
}
