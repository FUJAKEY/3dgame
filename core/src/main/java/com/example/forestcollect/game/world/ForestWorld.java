package com.example.forestcollect.game.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import java.util.Random;

public class ForestWorld implements Disposable {

    private final Array<ModelInstance> staticInstances = new Array<>();
    private final Array<Coin> coins = new Array<>();
    private final Environment environment;

    private final Model groundModel;
    private final Model treeModel;
    private final Model coinModel;

    private final float mapRadius = 120f;

    public ForestWorld(GraphicsQuality quality) {
        ModelBuilder builder = new ModelBuilder();

        groundModel = builder.createBox(mapRadius * 2f, 1f, mapRadius * 2f,
                new Material(ColorAttribute.createDiffuse(new Color(0.26f, 0.58f, 0.33f, 1f))),
                GL20.GL_TRIANGLES);

        treeModel = createTreeModel();
        coinModel = createCoinModel();

        ModelInstance groundInstance = new ModelInstance(groundModel);
        groundInstance.transform.setToTranslation(0f, -0.5f, 0f);
        staticInstances.add(groundInstance);

        generateForest(quality);

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.75f, 0.75f, 0.75f, 1f));
        environment.add(new DirectionalLight().set(new Color(1f, 1f, 0.95f, 1f), new Vector3(-0.8f, -1f, -0.6f)));
    }

    private void generateForest(GraphicsQuality quality) {
        Random random = new Random(42);
        int treeCount = quality.getTreeCount();
        for (int i = 0; i < treeCount; i++) {
            float angle = random.nextFloat() * MathUtils.PI2;
            float distance = mapRadius * (0.35f + random.nextFloat() * 0.6f);
            float x = MathUtils.cos(angle) * distance;
            float z = MathUtils.sin(angle) * distance;

            if (Math.abs(x) < 8f && Math.abs(z) < 8f) {
                i--;
                continue;
            }

            ModelInstance treeInstance = new ModelInstance(treeModel);
            float scale = quality.getFoliageDensity() + random.nextFloat() * 0.4f;
            treeInstance.transform.setToTranslation(x, 0f, z);
            treeInstance.transform.scale(scale, scale, scale);
            staticInstances.add(treeInstance);

            float offsetX = -3f + random.nextFloat() * 6f;
            float offsetZ = -3f + random.nextFloat() * 6f;
            Vector3 coinPos = new Vector3(x + offsetX, 0.5f, z + offsetZ);
            ModelInstance coinInstance = new ModelInstance(coinModel);
            coinInstance.transform.setToTranslation(coinPos);
            coins.add(new Coin(coinInstance, coinPos));
        }
    }

    private Model createTreeModel() {
        ModelBuilder builder = new ModelBuilder();
        builder.begin();
        Material trunkMat = new Material(ColorAttribute.createDiffuse(new Color(0.52f, 0.32f, 0.17f, 1f)));
        MeshPartBuilder trunkBuilder = builder.part("trunk", GL20.GL_TRIANGLES,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, trunkMat);
        Matrix4 trunkTransform = new Matrix4().setToTranslation(0f, 6f, 0f);
        trunkBuilder.setVertexTransform(trunkTransform);
        trunkBuilder.cylinder(1.6f, 12f, 1.6f, 16);
        trunkBuilder.setVertexTransform(new Matrix4());

        Material leavesMat = new Material(ColorAttribute.createDiffuse(new Color(0.09f, 0.56f, 0.28f, 1f)));
        MeshPartBuilder leavesBuilder = builder.part("leaves", GL20.GL_TRIANGLES,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, leavesMat);
        Matrix4 leavesTransform = new Matrix4().setToTranslation(0f, 17f, 0f);
        leavesBuilder.setVertexTransform(leavesTransform);
        leavesBuilder.cone(12f, 14f, 12f, 20);
        leavesBuilder.setVertexTransform(new Matrix4());

        return builder.end();
    }

    private Model createCoinModel() {
        ModelBuilder builder = new ModelBuilder();
        Material material = new Material(ColorAttribute.createDiffuse(new Color(0.96f, 0.82f, 0.13f, 1f)));
        return builder.createCylinder(1.6f, 0.35f, 1.6f, 20, material,
                GL20.GL_TRIANGLES);
    }

    public Array<ModelInstance> getStaticInstances() {
        return staticInstances;
    }

    public Array<Coin> getCoins() {
        return coins;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public float getMapRadius() {
        return mapRadius;
    }

    @Override
    public void dispose() {
        groundModel.dispose();
        treeModel.dispose();
        coinModel.dispose();
    }
}
