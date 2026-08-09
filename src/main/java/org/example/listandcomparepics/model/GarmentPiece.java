package org.example.listandcomparepics.model;

import java.util.ArrayList;
import java.util.List;

/// Represents one position in the outfit — Top, Bottom, or Whole (for
/// one-piece garments like a dress). Holds an ordered, outer-to-inner
/// list of Layers, since some photos show more than one garment
/// stacked at the same position (a shirt over a fully-visible bikini
/// top, shorts over a visible bikini bottom).
///
/// Almost always this list has exactly one Layer. It only grows when
/// a second garment at the same position is BOTH visible and taggable
/// — a merely-suspected under-layer (a strap peeking out, an inferred
/// bikini top under an opaque shirt) stays a note on the single
/// visible Layer's rawNotes instead of becoming its own Layer.
public class GarmentPiece {

    public enum PieceType {
        TOP,
        BOTTOM,
        WHOLE   // one-piece garments: dress, jumpsuit, one-piece swimsuit
    }

    public PieceType pieceType;

    /// Outer-to-inner order. layers.get(0) is the outermost/visible
    /// garment; later entries are layers beneath it that are ALSO
    /// visible and taggable in this particular photo.
    public List<Layer> layers = new ArrayList<>();

    public GarmentPiece(PieceType pieceType) {
        this.pieceType = pieceType;
    }

    public Layer addLayer(String clothesType) {
        Layer layer = new Layer(clothesType);
        layers.add(layer);
        return layer;
    }

    /// Convenience for the common case — most GarmentPieces have
    /// exactly one layer, so this avoids callers writing
    /// piece.layers.get(0) everywhere. Returns null if no layer has
    /// been added yet.
    public Layer getPrimaryLayer() {
        return layers.isEmpty() ? null : layers.get(0);
    }

    /// Flattens every color from every zone across every layer at this
    /// position into one list. Same tradeoff as before: good for a
    /// general "does this photo contain color X anywhere" search, but
    /// loses which layer/zone each color came from. For anything more
    /// specific, walk layers and call Layer.getZone(...) directly.
    public List<String> getAllColorsFlattened() {
        List<String> all = new ArrayList<>();
        for (Layer layer : layers) {
            for (Zone zone : layer.zones) {
                all.addAll(zone.colors);
            }
        }
        return all;
    }
}