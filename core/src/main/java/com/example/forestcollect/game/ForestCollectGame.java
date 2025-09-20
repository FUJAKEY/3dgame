package com.example.forestcollect.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.example.forestcollect.game.screens.GameplayScreen;
import com.example.forestcollect.game.screens.MainMenuScreen;
import com.example.forestcollect.game.screens.SettingsScreen;
import com.example.forestcollect.game.ui.UiSkinFactory;

public class ForestCollectGame extends Game {
    public static final String PREFS_NAME = "forest_collect_prefs";

    private SpriteBatch batch;
    private Skin uiSkin;
    private Preferences preferences;

    @Override
    public void create() {
        batch = new SpriteBatch();
        uiSkin = UiSkinFactory.createSkin();
        preferences = Gdx.app.getPreferences(PREFS_NAME);
        setScreen(new MainMenuScreen(this));
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public Skin getUiSkin() {
        return uiSkin;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public void openMainMenu() {
        setScreen(new MainMenuScreen(this));
    }

    public void openSettings() {
        setScreen(new SettingsScreen(this));
    }

    public void startGame() {
        setScreen(new GameplayScreen(this));
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }
        batch.dispose();
        uiSkin.dispose();
    }
}
