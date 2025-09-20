package com.example.forestcollect.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public final class UiSkinFactory {

    private UiSkinFactory() {
    }

    public static Skin createSkin() {
        Skin skin = new Skin();

        BitmapFont font = new BitmapFont();
        font.getData().setScale(1.5f);
        skin.add("default-font", font);

        skin.add("primary", createRounded(skin, "primary", "#5e60ce", 420, 150));
        skin.add("primary-pressed", createRounded(skin, "primary-pressed", "#4a4eb7", 420, 150));
        skin.add("panel", createRounded(skin, "panel", "#ffe066", 460, 200));
        skin.add("touchpad-bg", createCircle(skin, "touchpad-bg", "#ffd166", 260));
        skin.add("touchpad-knob", createCircle(skin, "touchpad-knob", "#ff6b6b", 140));

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = skin.getFont("default-font");
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = skin.getFont("default-font");
        buttonStyle.up = skin.getDrawable("primary");
        buttonStyle.over = skin.getDrawable("primary-pressed");
        buttonStyle.down = skin.getDrawable("primary-pressed");
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = new Color(1f, 1f, 1f, 0.9f);
        skin.add("default", buttonStyle);

        Touchpad.TouchpadStyle touchpadStyle = new Touchpad.TouchpadStyle();
        touchpadStyle.background = skin.getDrawable("touchpad-bg");
        touchpadStyle.knob = skin.getDrawable("touchpad-knob");
        skin.add("default", touchpadStyle);

        Button.ButtonStyle panelStyle = new Button.ButtonStyle();
        panelStyle.up = skin.getDrawable("panel");
        skin.add("panel", panelStyle);

        return skin;
    }

    private static Drawable createRounded(Skin skin, String name, String hexColor, int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        Color color = Color.valueOf(hexColor);
        pixmap.setColor(color);
        pixmap.fillRectangle(30, 0, width - 60, height);
        pixmap.fillRectangle(0, 30, width, height - 60);
        pixmap.fillCircle(30, 30, 30);
        pixmap.fillCircle(width - 31, 30, 30);
        pixmap.fillCircle(30, height - 31, 30);
        pixmap.fillCircle(width - 31, height - 31, 30);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        skin.add(name + "-texture", texture);
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    private static Drawable createCircle(Skin skin, String name, String hexColor, int diameter) {
        Pixmap pixmap = new Pixmap(diameter, diameter, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.CLEAR);
        pixmap.fill();
        pixmap.setColor(Color.valueOf(hexColor));
        pixmap.fillCircle(diameter / 2, diameter / 2, diameter / 2);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        skin.add(name + "-texture", texture);
        return new TextureRegionDrawable(new TextureRegion(texture));
    }
}
