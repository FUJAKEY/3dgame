package com.example.forestcollect.game.world;

public enum GraphicsQuality {
    LOW(0.6f, 40),
    MEDIUM(0.9f, 65),
    HIGH(1.2f, 90);

    private final float foliageDensity;
    private final int treeCount;

    GraphicsQuality(float foliageDensity, int treeCount) {
        this.foliageDensity = foliageDensity;
        this.treeCount = treeCount;
    }

    public float getFoliageDensity() {
        return foliageDensity;
    }

    public int getTreeCount() {
        return treeCount;
    }
}
