package com.example.forestcollect.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.example.forestcollect.game.ForestCollectGame;
import com.example.forestcollect.game.world.Coin;
import com.example.forestcollect.game.world.ForestWorld;
import com.example.forestcollect.game.world.GraphicsQuality;

public class GameplayScreen implements Screen {

    private static final String PREF_QUALITY = "graphics_quality";

    private final ForestCollectGame game;

    private ForestWorld world;
    private PerspectiveCamera camera;
    private com.badlogic.gdx.graphics.g3d.ModelBatch modelBatch;
    private Stage uiStage;
    private Touchpad movementPad;
    private Label coinLabel;
    private Label qualityLabel;

    private final Vector3 playerPosition = new Vector3(0f, 0f, 0f);
    private float cameraYaw = 0f;
    private float cameraPitch = -10f;
    private final float cameraHeight = 2.4f;
    private float coinSpin;
    private GraphicsQuality quality;

    private InputMultiplexer inputMultiplexer;
    private CameraDragProcessor cameraDragProcessor;

    public GameplayScreen(ForestCollectGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        quality = loadQuality();
        world = new ForestWorld(quality);
        modelBatch = new com.badlogic.gdx.graphics.g3d.ModelBatch();

        camera = new PerspectiveCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 0.1f;
        camera.far = world.getMapRadius() * 4f;
        updateCameraTransform();

        uiStage = new Stage(new ScreenViewport(), game.getBatch());
        setupUi();

        cameraDragProcessor = new CameraDragProcessor();
        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(cameraDragProcessor);
        inputMultiplexer.addProcessor(uiStage);
        Gdx.input.setInputProcessor(inputMultiplexer);
        Gdx.input.setCatchKey(Input.Keys.BACK, true);
    }

    private void setupUi() {
        movementPad = new Touchpad(18f, game.getUiSkin());
        movementPad.setBounds(0, 0, 260f, 260f);

        coinLabel = new Label("Монет: 0", game.getUiSkin());
        coinLabel.setFontScale(1.2f);

        qualityLabel = new Label("Графика: " + quality.name(), game.getUiSkin());
        qualityLabel.setFontScale(0.9f);

        Table leftBottom = new Table();
        leftBottom.setFillParent(true);
        leftBottom.left().bottom();
        leftBottom.add(movementPad).size(320f).pad(40f);

        Table topLeft = new Table();
        topLeft.setFillParent(true);
        topLeft.left().top().pad(40f);
        topLeft.add(coinLabel).left().row();
        topLeft.add(qualityLabel).left().padTop(15f);

        uiStage.addActor(leftBottom);
        uiStage.addActor(topLeft);
    }

    private GraphicsQuality loadQuality() {
        String value = game.getPreferences().getString(PREF_QUALITY, GraphicsQuality.MEDIUM.name());
        try {
            return GraphicsQuality.valueOf(value);
        } catch (IllegalArgumentException e) {
            return GraphicsQuality.MEDIUM;
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.52f, 0.76f, 0.99f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        modelBatch.begin(camera);
        for (com.badlogic.gdx.graphics.g3d.ModelInstance instance : world.getStaticInstances()) {
            modelBatch.render(instance, world.getEnvironment());
        }
        for (Coin coin : world.getCoins()) {
            if (!coin.isCollected()) {
                coin.getModelInstance().transform.idt()
                        .translate(coin.getPosition())
                        .rotate(Vector3.Y, coinSpin);
                modelBatch.render(coin.getModelInstance(), world.getEnvironment());
            }
        }
        modelBatch.end();
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);

        uiStage.act(delta);
        uiStage.draw();
    }

    private void update(float delta) {
        float clampedDelta = MathUtils.clamp(delta, 0f, 0.1f);
        updateMovement(clampedDelta);
        updateCoinCollection();
        updateCameraTransform();
        coinSpin = (coinSpin + 90f * clampedDelta) % 360f;
    }

    private void updateMovement(float delta) {
        float knobX = movementPad.getKnobPercentX();
        float knobY = movementPad.getKnobPercentY();

        if (Math.abs(knobX) < 0.05f && Math.abs(knobY) < 0.05f) {
            return;
        }

        float strength = Math.min((float) Math.sqrt(knobX * knobX + knobY * knobY), 1f);

        Vector3 forward = new Vector3(camera.direction.x, 0f, camera.direction.z);
        if (forward.isZero(0.0001f)) {
            forward.set(0f, 0f, -1f);
        }
        forward.nor();
        Vector3 right = new Vector3(forward.z, 0f, -forward.x).nor();

        Vector3 velocity = new Vector3();
        velocity.add(right.scl(knobX));
        velocity.add(forward.scl(knobY));
        if (velocity.len2() > 0.0001f) {
            velocity.nor().scl(strength * 18f * delta);
            playerPosition.add(velocity);
            clampPlayerInsideWorld();
        }
    }

    private void clampPlayerInsideWorld() {
        float radius = world.getMapRadius() - 5f;
        if (playerPosition.x > radius) {
            playerPosition.x = radius;
        } else if (playerPosition.x < -radius) {
            playerPosition.x = -radius;
        }
        if (playerPosition.z > radius) {
            playerPosition.z = radius;
        } else if (playerPosition.z < -radius) {
            playerPosition.z = -radius;
        }
    }

    private void updateCoinCollection() {
        int collectedCount = 0;
        for (Coin coin : world.getCoins()) {
            if (!coin.isCollected()) {
                if (coin.getPosition().dst2(playerPosition) < 4f) {
                    coin.collect();
                }
            }
            if (coin.isCollected()) {
                collectedCount++;
            }
        }
        coinLabel.setText("Монет: " + collectedCount + " / " + world.getCoins().size);
    }

    private void updateCameraTransform() {
        float cosPitch = MathUtils.cosDeg(cameraPitch);
        Vector3 direction = new Vector3(
                -MathUtils.sinDeg(cameraYaw) * cosPitch,
                MathUtils.sinDeg(cameraPitch),
                -MathUtils.cosDeg(cameraYaw) * cosPitch
        );
        camera.direction.set(direction).nor();
        camera.position.set(playerPosition.x, playerPosition.y + cameraHeight, playerPosition.z);
        camera.up.set(Vector3.Y);
        camera.update();
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
        uiStage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        Gdx.input.setInputProcessor(null);
        Gdx.input.setCatchKey(Input.Keys.BACK, false);
        if (modelBatch != null) {
            modelBatch.dispose();
        }
        if (uiStage != null) {
            uiStage.dispose();
        }
        if (world != null) {
            world.dispose();
        }
    }

    private class CameraDragProcessor extends InputAdapter {
        private int pointer = -1;
        private float lastX;
        private float lastY;
        private final float sensitivity = 0.25f;

        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.BACK) {
                game.openMainMenu();
                return true;
            }
            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (screenX > Gdx.graphics.getWidth() / 2f && this.pointer == -1) {
                this.pointer = pointer;
                lastX = screenX;
                lastY = screenY;
                return true;
            }
            return false;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            if (pointer == this.pointer) {
                float deltaX = screenX - lastX;
                float deltaY = screenY - lastY;
                lastX = screenX;
                lastY = screenY;
                cameraYaw -= deltaX * sensitivity;
                cameraPitch -= deltaY * sensitivity;
                cameraPitch = MathUtils.clamp(cameraPitch, -60f, 45f);
                return true;
            }
            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (pointer == this.pointer) {
                this.pointer = -1;
                return true;
            }
            return false;
        }
    }
}
