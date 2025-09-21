package com.example.forestcoins.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.Random;

public class ForestCoinGame extends ApplicationAdapter {

    private final QualityLevel qualityLevel;

    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;
    private Model groundModel;
    private Model treeModel;
    private Model coinModel;
    private ModelInstance groundInstance;
    private Array<ModelInstance> trees;
    private Array<Coin> coins;
    private final Vector2 movementVector = new Vector2();
    private Vector3 playerPosition;
    private float yaw = 180f;
    private float pitch = -10f;
    private Stage hudStage;
    private Touchpad movementPad;
    private Label scoreLabel;
    private Label timeLabel;
    private Texture touchpadBackgroundTexture;
    private Texture touchpadKnobTexture;
    private BitmapFont hudFont;
    private final Vector2 lastLookPoint = new Vector2();
    private int lookPointer = -1;
    private int collectedCoins;
    private float elapsedTime;
    private final float moveSpeed = 12f;
    private QualityLevelProfile profile;

    private static final float CAMERA_HEIGHT = 2.2f;
    private static final float LOOK_SENSITIVITY = 0.12f;

    public ForestCoinGame(QualityLevel qualityLevel) {
        this.qualityLevel = qualityLevel;
    }

    @Override
    public void create() {
        profile = new QualityLevelProfile(qualityLevel);
        modelBatch = new ModelBatch();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.85f, 0.85f, 0.85f, 1f));
        environment.add(new DirectionalLight().set(profile.lightIntensity, profile.lightIntensity, profile.lightIntensity,
                -0.4f, -1f, -0.3f));
        environment.add(new DirectionalLight().set(0.25f, 0.3f, 0.35f, 0.9f, -0.2f, 0.1f));

        buildModels();
        buildWorld();
        setupCamera();
        setupHud();

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(hudStage);
        multiplexer.addProcessor(new LookInputProcessor());
        Gdx.input.setInputProcessor(multiplexer);
    }

    private void setupCamera() {
        camera = new PerspectiveCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 0.1f;
        camera.far = profile.worldRadius * 3f;
        playerPosition = new Vector3(0f, CAMERA_HEIGHT, 10f);
        updateCameraVectors();
    }

    private void buildModels() {
        ModelBuilder builder = new ModelBuilder();
        groundModel = builder.createRect(
                -profile.worldRadius, 0f, -profile.worldRadius,
                -profile.worldRadius, 0f, profile.worldRadius,
                profile.worldRadius, 0f, profile.worldRadius,
                profile.worldRadius, 0f, -profile.worldRadius,
                0f, 1f, 0f,
                new Material(ColorAttribute.createDiffuse(new Color(0.35f, 0.7f, 0.35f, 1f))),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        builder.begin();
        MeshPartBuilder trunkBuilder = builder.part("trunk", GL20.GL_TRIANGLES,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(new Color(0.4f, 0.25f, 0.1f, 1f))));
        trunkBuilder.cylinder(0.7f, 5f, 0.7f, 20);
        MeshPartBuilder foliageBuilder = builder.part("foliage", GL20.GL_TRIANGLES,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(new Color(0.15f, 0.45f, 0.1f, 1f))));
        foliageBuilder.setVertexTransform(new Matrix4().setToTranslation(0f, 3.5f, 0f));
        foliageBuilder.cone(5f, 7f, 5f, 24);
        foliageBuilder.setVertexTransform(null);
        treeModel = builder.end();

        coinModel = builder.createCylinder(1.2f, 0.25f, 1.2f, 32,
                new Material(ColorAttribute.createDiffuse(new Color(1f, 0.84f, 0.2f, 1f))),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        groundInstance = new ModelInstance(groundModel);
    }

    private void buildWorld() {
        trees = new Array<>(qualityLevel.getTreeCount());
        coins = new Array<>(qualityLevel.getCoinCount());
        Random random = new Random(qualityLevel.ordinal() * 9137L + 42L);
        float radius = qualityLevel.getWorldRadius() - 5f;

        for (int i = 0; i < qualityLevel.getTreeCount(); i++) {
            float angle = random.nextFloat() * MathUtils.PI2;
            float distance = 20f + random.nextFloat() * (radius - 20f);
            float x = MathUtils.cos(angle) * distance;
            float z = MathUtils.sin(angle) * distance;
            ModelInstance instance = new ModelInstance(treeModel);
            instance.transform.setToTranslation(x, 0f, z);
            float randomScale = 0.8f + random.nextFloat() * 0.6f;
            instance.transform.scale(randomScale, randomScale, randomScale);
            trees.add(instance);
            if (i < qualityLevel.getCoinCount()) {
                Vector3 coinPosition = new Vector3(
                        x + MathUtils.random(-2.5f, 2.5f),
                        0.35f,
                        z + MathUtils.random(-2.5f, 2.5f));
                coins.add(createCoinInstance(coinPosition));
            }
        }

        while (coins.size < qualityLevel.getCoinCount()) {
            float x = MathUtils.random(-radius, radius);
            float z = MathUtils.random(-radius, radius);
            coins.add(createCoinInstance(new Vector3(x, 0.35f, z)));
        }
    }

    private Coin createCoinInstance(Vector3 position) {
        ModelInstance instance = new ModelInstance(coinModel);
        instance.transform.setToTranslation(position);
        return new Coin(instance, position);
    }

    private void setupHud() {
        hudStage = new Stage(new ScreenViewport());
        Touchpad.TouchpadStyle style = createTouchpadStyle();
        movementPad = new Touchpad(10f, style);
        float padSize = Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()) * 0.32f;
        movementPad.setBounds(32f, 32f, padSize, padSize);
        hudStage.addActor(movementPad);

        hudFont = new BitmapFont();
        hudFont.getData().setScale(Gdx.graphics.getDensity() > 2.0f ? 1.6f : 1.2f);
        Label.LabelStyle labelStyle = new Label.LabelStyle(hudFont, Color.WHITE);

        scoreLabel = new Label("", labelStyle);
        timeLabel = new Label("", labelStyle);
        Table table = new Table();
        table.top().left();
        table.setFillParent(true);
        table.pad(24f);
        table.add(scoreLabel).left();
        table.row();
        table.add(timeLabel).left().padTop(8f);
        hudStage.addActor(table);
    }

    private Touchpad.TouchpadStyle createTouchpadStyle() {
        Pixmap bg = new Pixmap(240, 240, Pixmap.Format.RGBA8888);
        bg.setColor(0f, 0f, 0f, 0.25f);
        bg.fillCircle(120, 120, 120);
        Pixmap knob = new Pixmap(100, 100, Pixmap.Format.RGBA8888);
        knob.setColor(0.9f, 0.72f, 0.15f, 0.9f);
        knob.fillCircle(50, 50, 50);
        knob.setColor(1f, 1f, 1f, 0.45f);
        knob.fillCircle(50, 50, 32);
        touchpadBackgroundTexture = new Texture(bg);
        touchpadKnobTexture = new Texture(knob);
        Drawable background = new TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(touchpadBackgroundTexture));
        Drawable knobDrawable = new TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(touchpadKnobTexture));
        bg.dispose();
        knob.dispose();
        Touchpad.TouchpadStyle style = new Touchpad.TouchpadStyle();
        style.background = background;
        style.knob = knobDrawable;
        return style;
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        elapsedTime += delta;
        updateMovement(delta);
        updateCoins(delta);
        updateCameraVectors();
        updateHud();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.53f, 0.81f, 0.92f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(camera);
        modelBatch.render(groundInstance, environment);
        for (ModelInstance tree : trees) {
            modelBatch.render(tree, environment);
        }
        for (Coin coin : coins) {
            if (!coin.collected) {
                modelBatch.render(coin.instance, environment);
            }
        }
        modelBatch.end();

        hudStage.act(delta);
        hudStage.draw();
    }

    private void updateHud() {
        scoreLabel.setText(String.format("Монеты: %d", collectedCoins));
        timeLabel.setText(String.format("Время: %.1f c", elapsedTime));
    }

    private void updateMovement(float delta) {
        movementVector.set(movementPad.getKnobPercentX(), movementPad.getKnobPercentY());
        if (movementVector.len2() > 0.01f) {
            Vector3 forward = new Vector3(MathUtils.sinDeg(yaw), 0f, MathUtils.cosDeg(yaw));
            Vector3 right = new Vector3(-forward.z, 0f, forward.x);
            Vector3 move = new Vector3(forward).scl(movementVector.y).add(right.scl(movementVector.x));
            if (move.len2() > 0f) {
                move.nor().scl(moveSpeed * profile.speedMultiplier * delta);
                playerPosition.add(move);
            }
        }

        float radius = qualityLevel.getWorldRadius() - 4f;
        playerPosition.x = MathUtils.clamp(playerPosition.x, -radius, radius);
        playerPosition.z = MathUtils.clamp(playerPosition.z, -radius, radius);
        playerPosition.y = CAMERA_HEIGHT;
    }

    private void updateCameraVectors() {
        float cosPitch = MathUtils.cosDeg(pitch);
        camera.position.set(playerPosition);
        camera.direction.set(
                MathUtils.sinDeg(yaw) * cosPitch,
                MathUtils.sinDeg(pitch),
                MathUtils.cosDeg(yaw) * cosPitch).nor();
        camera.up.set(Vector3.Y);
        camera.update();
    }

    private void updateCoins(float delta) {
        for (Coin coin : coins) {
            if (coin.collected) {
                continue;
            }
            coin.rotation += 90f * delta;
            if (coin.rotation > 360f) {
                coin.rotation -= 360f;
            }
            coin.instance.transform.idt();
            coin.instance.transform.translate(coin.position);
            coin.instance.transform.rotate(Vector3.Y, coin.rotation);
            if (playerPosition.dst2(coin.position) < 1.6f) {
                coin.collected = true;
                collectedCoins++;
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        if (camera != null) {
            camera.viewportWidth = width;
            camera.viewportHeight = height;
            camera.update();
        }
        if (hudStage != null) {
            hudStage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void dispose() {
        disposeSafely(modelBatch);
        disposeSafely(groundModel);
        disposeSafely(treeModel);
        disposeSafely(coinModel);
        disposeSafely(hudStage);
        disposeSafely(hudFont);
        disposeSafely(touchpadBackgroundTexture);
        disposeSafely(touchpadKnobTexture);
    }

    private void disposeSafely(Disposable disposable) {
        if (disposable != null) {
            disposable.dispose();
        }
    }

    private class LookInputProcessor extends InputAdapter {

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            float screenWidth = Gdx.graphics.getWidth();
            float screenHeight = Gdx.graphics.getHeight();
            boolean isMovementArea = screenX < screenWidth * 0.45f && screenY > screenHeight * 0.55f;
            if (isMovementArea) {
                return false;
            }
            lookPointer = pointer;
            lastLookPoint.set(screenX, screenY);
            return true;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            if (pointer != lookPointer) {
                return false;
            }
            float deltaX = screenX - lastLookPoint.x;
            float deltaY = screenY - lastLookPoint.y;
            yaw -= deltaX * LOOK_SENSITIVITY;
            pitch -= deltaY * LOOK_SENSITIVITY;
            pitch = MathUtils.clamp(pitch, -60f, 45f);
            lastLookPoint.set(screenX, screenY);
            return true;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (pointer == lookPointer) {
                lookPointer = -1;
            }
            return false;
        }
    }

    private static class Coin {
        final ModelInstance instance;
        final Vector3 position;
        boolean collected;
        float rotation;

        Coin(ModelInstance instance, Vector3 position) {
            this.instance = instance;
            this.position = position;
        }
    }

    private static class QualityLevelProfile {
        final float worldRadius;
        final float lightIntensity;
        final float speedMultiplier;

        QualityLevelProfile(QualityLevel level) {
            this.worldRadius = level.getWorldRadius();
            this.lightIntensity = level.getLightIntensity() / 10f;
            this.speedMultiplier = MathUtils.lerp(0.85f, 1.15f, level.ordinal() / 2f);
        }
    }
}
