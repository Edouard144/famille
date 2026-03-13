package com.famille.utils;

import java.util.List;

public class MealSuggestions {

    /**
     * Return age-appropriate meal suggestions for a child.
     * Based on Rwanda nutrition guidelines.
     *
     * @param ageInMonths  the child's age in months
     * @return             a list of meal suggestion strings
     */
    public static List<String> getSuggestions(int ageInMonths) {

        // ── 0–5 months: breastfeeding only ──────────────────────────
        if (ageInMonths < 6) {
            return List.of(
                    "Konka gusa (Breastfeeding only)",
                    "Ntukongere amazi cyangwa ibindi (No water or other food yet)",
                    "Konka inshuro 8–12 ku munsi (Feed 8–12 times a day)"
            );
        }

        // ── 6–8 months: start soft foods ────────────────────────────
        if (ageInMonths < 9) {
            return List.of(
                    "Isombe ryoroheje (Soft mashed cassava leaves)",
                    "Igikoma cy'ingano (Porridge — wheat or sorghum)",
                    "Ubuto bworoheje (Mashed banana)",
                    "Indimu cyangwa mango yoroheje (Mashed mango or avocado)",
                    "Konka ukomeze (Continue breastfeeding)"
            );
        }

        // ── 9–11 months: more variety ────────────────────────────────
        if (ageInMonths < 12) {
            return List.of(
                    "Igikoma cy'ibinyampeke (Mixed grain porridge)",
                    "Ibijumba bitoroheje (Mashed sweet potato)",
                    "Ibiharage bitoroheje (Mashed beans)",
                    "Inyama yoroheje (Minced meat — small amount)",
                    "Amagi (Egg yolk)",
                    "Fruits: umuneke, indimu, mango"
            );
        }

        // ── 12–23 months: family foods ───────────────────────────────
        if (ageInMonths < 24) {
            return List.of(
                    "Ugali n'isombe (Ugali with cassava leaves)",
                    "Ibiharage n'isombe (Beans with greens)",
                    "Amagi (Eggs — whole)",
                    "Inyama nke (Small portions of meat/fish)",
                    "Icunga, mango, umuneke (Fruits daily)",
                    "Milk — ikivuguto (fermented milk)",
                    "Konka ukomeze (Continue breastfeeding if possible)"
            );
        }

        // ── 24–59 months: toddler / preschool ───────────────────────
        if (ageInMonths < 60) {
            return List.of(
                    "Ugali n'ibiharage (Ugali and beans)",
                    "Isombe n'inyama (Cassava leaves with meat)",
                    "Amagi 2–3 ku munsi (2–3 eggs per day)",
                    "Imboga zitandukanye (Mixed vegetables)",
                    "Fruits buri munsi (Daily fruits)",
                    "Amata (Milk every day)",
                    "Avoid sugar and salty snacks"
            );
        }

        // ── 5+ years: school age ─────────────────────────────────────
        return List.of(
                "Ifunguro rya saa moya (Breakfast before school)",
                "Ugali, isombe, ibiharage (Balanced plate)",
                "Imboga zitandukanye (Variety of vegetables)",
                "Amagi n'inyama (Eggs and meat 3x per week)",
                "Amata buri munsi (Milk every day)",
                "Fruits: icunga, mango, umuneke",
                "Amazi menshi (Plenty of water — 6–8 glasses)"
        );
    }
}