package com.example.forestcollect.game.world;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;

public class Coin {
    private final ModelInstance modelInstance;
    private final Vector3 position;
    private boolean collected;

    public Coin(ModelInstance instance, Vector3 position) {
        this.modelInstance = instance;
        this.position = position;
        this.collected = false;
    }

    public ModelInstance getModelInstance() {
        return modelInstance;
    }

    public Vector3 getPosition() {
        return position;
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        this.collected = true;
    }
}
