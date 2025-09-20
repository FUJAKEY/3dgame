package com.example.forestcollect.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.example.forestcollect.game.ForestCollectGame;

public class MainMenuScreen implements Screen {

    private final ForestCollectGame game;
    private Stage stage;

    public MainMenuScreen(ForestCollectGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport(), game.getBatch());
        Gdx.input.setInputProcessor(stage);

        Table root = new Table();
        root.setFillParent(true);
        root.defaults().pad(30f);

        Label title = new Label("Forest Collect", game.getUiSkin());
        title.setFontScale(1.8f);
        title.setColor(new Color(0.95f, 0.98f, 1f, 1f));

        TextButton playButton = new TextButton("Играть", game.getUiSkin());
        TextButton settingsButton = new TextButton("Настройки", game.getUiSkin());

        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.startGame();
            }
        });

        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.openSettings();
            }
        });

        Table panel = new Table(game.getUiSkin());
        panel.setBackground(game.getUiSkin().getDrawable("panel"));
        panel.defaults().pad(40f);
        panel.add(title).padBottom(60f).row();
        panel.add(playButton).width(520f).height(160f).row();
        panel.add(settingsButton).width(520f).height(160f);

        root.add(panel).expand().center();

        stage.addActor(root);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.36f, 0.65f, 0.98f, 1f);
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
