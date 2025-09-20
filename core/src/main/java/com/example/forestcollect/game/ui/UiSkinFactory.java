package com.example.forestcollect.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.example.forestcollect.game.assets.EmbeddedAssets;

public final class UiSkinFactory {

    private static final String FONT_STORAGE_PATH = "embedded_fonts/baloo2.ttf";
    private static final String CYRILLIC_CHARS =
            "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ" +
            "абвгдеёжзийклмнопрстуфхцчшщъыьэюя" +
            "«»№–—…" +
            "€£₽";

    private UiSkinFactory() {
    }

    public static Skin createSkin() {
        Skin skin = new Skin();

        FileHandle fontFile = ensureFontFile();
        BitmapFont titleFont;
        BitmapFont defaultFont;
        BitmapFont smallFont;
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
        try {
            titleFont = generateFont(generator, 68);
            defaultFont = generateFont(generator, 52);
            smallFont = generateFont(generator, 44);
        } finally {
            generator.dispose();
        }

        skin.add("title-font", titleFont);
        skin.add("default-font", defaultFont);
        skin.add("small-font", smallFont);

        skin.add("primary", createRounded(skin, "primary", "#5e60ce", 420, 150));
        skin.add("primary-pressed", createRounded(skin, "primary-pressed", "#4a4eb7", 420, 150));
        skin.add("panel", createRounded(skin, "panel", "#ffe066", 520, 280));
        skin.add("list-bg", createRounded(skin, "list-bg", "#ffd166", 480, 320));
        skin.add("selection", createRounded(skin, "selection", "#ff8fab", 480, 160));
        skin.add("touchpad-bg", createCircle(skin, "touchpad-bg", "#ffd166", 260));
        skin.add("touchpad-knob", createCircle(skin, "touchpad-knob", "#ff6b6b", 140));

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = defaultFont;
        labelStyle.fontColor = new Color(0.98f, 0.99f, 1f, 1f);
        skin.add("default", labelStyle);

        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = titleFont;
        titleStyle.fontColor = new Color(1f, 1f, 1f, 0.96f);
        skin.add("title", titleStyle);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = defaultFont;
        buttonStyle.up = skin.getDrawable("primary");
        buttonStyle.down = skin.getDrawable("primary-pressed");
        buttonStyle.checked = skin.getDrawable("primary-pressed");
        buttonStyle.over = skin.getDrawable("primary-pressed");
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.downFontColor = new Color(1f, 1f, 1f, 0.85f);
        skin.add("default", buttonStyle);

        Touchpad.TouchpadStyle touchpadStyle = new Touchpad.TouchpadStyle();
        touchpadStyle.background = skin.getDrawable("touchpad-bg");
        touchpadStyle.knob = skin.getDrawable("touchpad-knob");
        skin.add("default", touchpadStyle);

        Button.ButtonStyle panelStyle = new Button.ButtonStyle();
        panelStyle.up = skin.getDrawable("panel");
        skin.add("panel", panelStyle);

        SelectBox.SelectBoxStyle selectBoxStyle = new SelectBox.SelectBoxStyle();
        selectBoxStyle.font = defaultFont;
        selectBoxStyle.fontColor = Color.WHITE;
        selectBoxStyle.background = skin.getDrawable("panel");

        List.ListStyle listStyle = new List.ListStyle();
        listStyle.font = smallFont;
        listStyle.fontColorSelected = Color.WHITE;
        listStyle.fontColorUnselected = new Color(1f, 1f, 1f, 0.8f);
        listStyle.selection = skin.getDrawable("selection");
        listStyle.background = skin.getDrawable("list-bg");
        selectBoxStyle.listStyle = listStyle;

        ScrollPane.ScrollPaneStyle scrollPaneStyle = new ScrollPane.ScrollPaneStyle();
        scrollPaneStyle.background = skin.getDrawable("list-bg");
        selectBoxStyle.scrollStyle = scrollPaneStyle;
        selectBoxStyle.disabledFontColor = new Color(1f, 1f, 1f, 0.4f);
        skin.add("default", selectBoxStyle);

        return skin;
    }

    private static BitmapFont generateFont(FreeTypeFontGenerator generator, int size) {
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = size;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + CYRILLIC_CHARS;
        parameter.borderWidth = 2f;
        parameter.borderColor = new Color(0f, 0f, 0f, 0.35f);
        parameter.shadowColor = new Color(0f, 0f, 0f, 0.35f);
        parameter.shadowOffsetX = 2;
        parameter.shadowOffsetY = 2;
        BitmapFont font = generator.generateFont(parameter);
        for (TextureRegion region : font.getRegions()) {
            region.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
        }
        font.getData().markupEnabled = true;
        return font;
    }

    private static FileHandle ensureFontFile() {
        FileHandle file = Gdx.files.local(FONT_STORAGE_PATH);
        if (!file.exists()) {
            if (file.parent() != null) {
                file.parent().mkdirs();
            }
            file.writeBytes(EmbeddedAssets.getFontBytes(), false);
        }
        return file;
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
        texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
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
        texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        skin.add(name + "-texture", texture);
        return new TextureRegionDrawable(new TextureRegion(texture));
    }
}
