package com.example.forestcollect.game.world;

public enum GraphicsQuality {
    LOW("Низкое", 0.6f, 40),
    MEDIUM("Среднее", 0.9f, 65),
    HIGH("Высокое", 1.2f, 90);

    private final String displayName;
    private final float foliageDensity;
    private final int treeCount;

    GraphicsQuality(String displayName, float foliageDensity, int treeCount) {
        this.displayName = displayName;
        this.foliageDensity = foliageDensity;
        this.treeCount = treeCount;
    }

    public float getFoliageDensity() {
        return foliageDensity;
    }

    public int getTreeCount() {
        return treeCount;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
