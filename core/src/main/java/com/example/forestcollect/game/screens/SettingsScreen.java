package com.example.forestcollect.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.example.forestcollect.game.ForestCollectGame;
import com.example.forestcollect.game.world.GraphicsQuality;

public class SettingsScreen implements Screen {

    private static final String PREF_QUALITY = "graphics_quality";

    private final ForestCollectGame game;
    private Stage stage;
    private SelectBox<GraphicsQuality> graphicsSelector;

    public SettingsScreen(ForestCollectGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport(), game.getBatch());
        Gdx.input.setInputProcessor(stage);

        Table root = new Table();
        root.setFillParent(true);
        root.defaults().pad(24f);

        Label title = new Label("Настройки", game.getUiSkin(), "title");

        graphicsSelector = new SelectBox<>(game.getUiSkin());
        graphicsSelector.setItems(GraphicsQuality.values());
        GraphicsQuality saved = getSavedQuality();
        graphicsSelector.setSelected(saved);
        graphicsSelector.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                saveQuality(graphicsSelector.getSelected());
            }
        });

        TextButton backButton = new TextButton("Назад", game.getUiSkin());
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                saveQuality(graphicsSelector.getSelected());
                game.openMainMenu();
            }
        });

        Table panel = new Table(game.getUiSkin());
        panel.setBackground(game.getUiSkin().getDrawable("panel"));
        panel.defaults().pad(30f);
        panel.add(title).padBottom(50f).row();
        panel.add(new Label("Качество графики", game.getUiSkin())).padBottom(10f).row();
        panel.add(graphicsSelector).width(460f).padBottom(30f).row();
        panel.add(backButton).width(420f).height(140f);

        root.add(panel).expand().center();
        stage.addActor(root);
    }

    private GraphicsQuality getSavedQuality() {
        String value = game.getPreferences().getString(PREF_QUALITY, GraphicsQuality.MEDIUM.name());
        try {
            return GraphicsQuality.valueOf(value);
        } catch (IllegalArgumentException e) {
            return GraphicsQuality.MEDIUM;
        }
    }

    private void saveQuality(GraphicsQuality quality) {
        game.getPreferences().putString(PREF_QUALITY, quality.name());
        game.getPreferences().flush();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.26f, 0.54f, 0.94f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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
        if (stage != null) {
            stage.dispose();
        }
    }
}
