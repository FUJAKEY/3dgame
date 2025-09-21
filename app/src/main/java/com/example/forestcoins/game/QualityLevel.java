package com.example.forestcoins.game;

public enum QualityLevel {
    LOW(25, 35, 90f, 6f),
    MEDIUM(45, 55, 130f, 8f),
    HIGH(70, 80, 170f, 10f);

    private final int treeCount;
    private final int coinCount;
    private final float worldRadius;
    private final float lightIntensity;

    QualityLevel(int treeCount, int coinCount, float worldRadius, float lightIntensity) {
        this.treeCount = treeCount;
        this.coinCount = coinCount;
        this.worldRadius = worldRadius;
        this.lightIntensity = lightIntensity;
    }

    public int getTreeCount() {
        return treeCount;
    }

    public int getCoinCount() {
        return coinCount;
    }

    public float getWorldRadius() {
        return worldRadius;
    }

    public float getLightIntensity() {
        return lightIntensity;
    }

    public static QualityLevel fromPreference(String value) {
        if ("low".equalsIgnoreCase(value)) {
            return LOW;
        }
        if ("high".equalsIgnoreCase(value)) {
            return HIGH;
        }
        return MEDIUM;
    }
}
