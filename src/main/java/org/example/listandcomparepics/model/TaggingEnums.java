package org.example.listandcomparepics.model;

/// This file groups several small, closely-related enums together.
/// None of them are declared `public`, so Java allows multiple of them
/// to live in one file — they're only visible within this package,
/// which is fine since only the model/Controller code needs them.

/// Tri-state presence flag. Distinct from a plain boolean because
/// "I can't tell if this exists" is a real, common case in this app
/// (e.g. a component hidden from view in a photo) and needs to be
/// tracked differently from "confirmed not present."
enum Presence {
    PRESENT,
    ABSENT,
    UNKNOWN
}

/// Whether a zone's color list has a real dominant-to-accent ranking,
/// or whether the colors are co-equal (e.g. "Equal stripes", or a
/// pattern where colors alternate with no clear dominant color).
enum ColorMode {
    RANKED,
    EQUAL
}

/// The three fixed zone locations a garment piece can have.
/// Deliberately a small, bounded set rather than an open list —
/// see TAGGING_SCHEMA.md for why.
enum ZoneRole {
    INTERIOR,
    BORDER,
    ACCENT
}

/// Top-level pattern category for a zone.
enum PatternCategory {
    SIMPLE,
    MAIN_WITH_COMPLIMENTS
}

/// Subtypes under PatternCategory.SIMPLE.
enum SimpleSubtype {
    FULL_COLOR,
    EQUAL_STRIPES
}

/// Subtypes under PatternCategory.MAIN_WITH_COMPLIMENTS.
/// This list is expected to grow over time as new recurring patterns
/// are identified — Plaid and Dots/Figures were both added this way
/// rather than being anticipated up front. FRINGED was tried and then
/// retired back into OTHER after only ever matching one example —
/// a category needs to recur to earn a permanent spot.
enum ComplimentSubtype {
    DOTS_FIGURES,
    STRIPES,
    FLORAL,
    PLAID,
    ABSTRACT_TIE_DYE,
    OTHER
}

/// The very first decision made about an outfit, before anything
/// else: is this a one-piece garment (dress, jumpsuit, one-piece
/// swimsuit — GarmentPiece.PieceType.WHOLE) or a two-piece outfit
/// (independent Top and Bottom GarmentPieces)?
enum GarmentStructure {
    ONE_PIECE,
    TWO_PIECE
}