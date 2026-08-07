package org.example.listandcomparepics.model;

/// Traits that describe the subject herself, not any one photo or
/// outfit. These stay constant across every photo sharing the same
/// Girl ID — the existing Controller.lookupAndPopulateSubjectTraits()
/// method already implements the cross-photo lookup this class is
/// meant to back: if trait data exists on ANY historical photo for a
/// given ID, it can be reused for a new photo of that same subject,
/// even if the new photo itself doesn't clearly show that trait.
///
/// Unlike GarmentPiece fields, these are simple single values rather
/// than lists — a girl has one eye color, not an ordered list of them.
/// A value of null means "not yet entered"; the literal string
/// "Unknown" means "confirmed unknown" (e.g. a photo where her eyes
/// simply aren't visible in any available picture of her). Keeping
/// these as two different states matters for the same reason Presence
/// is tri-state on components — see TAGGING_SCHEMA.md.
public class SubjectProfile {

    public static final String UNKNOWN = "Unknown";

    public String girlId;
    public String eyeColor;
    public String hairColor;
    public String braSize;

    public SubjectProfile(String girlId) {
        this.girlId = girlId;
    }

    public boolean isEyeColorKnown() {
        return eyeColor != null && !eyeColor.equals(UNKNOWN);
    }

    public boolean isHairColorKnown() {
        return hairColor != null && !hairColor.equals(UNKNOWN);
    }

    public boolean isBraSizeKnown() {
        return braSize != null && !braSize.equals(UNKNOWN);
    }
}
