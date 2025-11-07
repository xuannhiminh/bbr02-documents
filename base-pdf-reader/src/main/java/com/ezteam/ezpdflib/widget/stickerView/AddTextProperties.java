package com.ezteam.ezpdflib.widget.stickerView;

import android.graphics.Color;
import android.graphics.Shader;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class AddTextProperties {

    public static class TextShadow {

        private int radius;
        private int dx;
        private int dy;
        private int colorShadow;


        TextShadow(int radius, int dx, int dy, int colorShadow) {
            this.radius = radius;
            this.dx = dx;
            this.dy = dy;
            this.colorShadow = colorShadow;
        }

        public int getRadius() {
            return radius;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }

        public int getDx() {
            return dx;
        }

        public int getDy() {
            return dy;
        }

        public int getColorShadow() {
            return colorShadow;
        }

    }

    private TextShadow textShadow;
    private int textShadowIndex;
    private int textSize;
    private int textColor;
    private int textColorIndex;
    private int textAlpha;
    private Shader textShader;
    private int textShaderIndex;
    private String text;
    private int textAlign;
    private String fontName;
    private int fontIndex;
    private int backgroundColor;
    private int backgroundColorIndex;
    private int backgroundAlpha;
    private int backgroundBorder;
    private boolean isFullScreen;
    private boolean isShowBackground;
    private int paddingWidth;
    private int paddingHeight;
    private int textWidth;
    private int textHeight;

    static List<TextShadow> getLstTextShadow() {
        List<TextShadow> textShadows = new ArrayList<>();
        textShadows.add(new TextShadow(0, 0, 0, Color.CYAN));
        textShadows.add(new TextShadow(8, 4, 4, Color.parseColor("#FF1493")));
        textShadows.add(new TextShadow(8, 4, 4, Color.MAGENTA));
        textShadows.add(new TextShadow(8, 4, 4, Color.parseColor("#24ffff")));
        textShadows.add(new TextShadow(8, 4, 4, Color.YELLOW));
        textShadows.add(new TextShadow(8, 4, 4, Color.WHITE));
        textShadows.add(new TextShadow(8, 4, 4, Color.GRAY));


        textShadows.add(new TextShadow(8, -4, -4, Color.parseColor("#FF1493")));
        textShadows.add(new TextShadow(8, -4, -4, Color.MAGENTA));
        textShadows.add(new TextShadow(8, -4, -4, Color.parseColor("#24ffff")));
        textShadows.add(new TextShadow(8, -4, -4, Color.YELLOW));
        textShadows.add(new TextShadow(8, -4, -4, Color.WHITE));
        textShadows.add(new TextShadow(8, -4, -4, Color.parseColor("#696969")));


        textShadows.add(new TextShadow(8, -4, 4, Color.parseColor("#FF1493")));
        textShadows.add(new TextShadow(8, -4, 4, Color.MAGENTA));
        textShadows.add(new TextShadow(8, -4, 4, Color.parseColor("#24ffff")));
        textShadows.add(new TextShadow(8, -4, 4, Color.YELLOW));
        textShadows.add(new TextShadow(8, -4, 4, Color.WHITE));
        textShadows.add(new TextShadow(8, -4, 4, Color.parseColor("#696969")));

        textShadows.add(new TextShadow(8, 4, -4, Color.parseColor("#FF1493")));
        textShadows.add(new TextShadow(8, 4, -4, Color.MAGENTA));
        textShadows.add(new TextShadow(8, 4, -4, Color.parseColor("#24ffff")));
        textShadows.add(new TextShadow(8, 4, -4, Color.YELLOW));
        textShadows.add(new TextShadow(8, 4, -4, Color.WHITE));
        textShadows.add(new TextShadow(8, 4, -4, Color.parseColor("#696969")));

        return textShadows;
    }

    static AddTextProperties getDefaultProperties() {

        AddTextProperties addTextProperties = new AddTextProperties();
        addTextProperties.setTextSize(30);
        addTextProperties.setTextAlign(View.TEXT_ALIGNMENT_CENTER);
        addTextProperties.setFontName("36.ttf");
        addTextProperties.setTextColor(Color.WHITE);
        addTextProperties.setTextAlpha(255);
        addTextProperties.setBackgroundAlpha(255);
        addTextProperties.setPaddingWidth(12);
        addTextProperties.setTextShaderIndex(7);
        addTextProperties.setBackgroundColorIndex(21);
        addTextProperties.setTextColorIndex(16);
        addTextProperties.setFontIndex(0);
        addTextProperties.setShowBackground(false);
        addTextProperties.setBackgroundBorder(8);
        addTextProperties.setTextAlign(View.TEXT_ALIGNMENT_CENTER);
        return addTextProperties;
    }

    int getTextColorIndex() {
        return textColorIndex;
    }

    public void setTextColorIndex(int textColorIndex) {
        this.textColorIndex = textColorIndex;
    }

    int getTextShaderIndex() {
        return textShaderIndex;
    }

    public void setTextShaderIndex(int textShaderIndex) {
        this.textShaderIndex = textShaderIndex;
    }

    int getBackgroundColorIndex() {
        return backgroundColorIndex;
    }

    public void setBackgroundColorIndex(int backgroundColorIndex) {
        this.backgroundColorIndex = backgroundColorIndex;
    }

    int getFontIndex() {
        return fontIndex;
    }

    public void setFontIndex(int fontIndex) {
        this.fontIndex = fontIndex;
    }

    int getTextShadowIndex() {
        return textShadowIndex;
    }

    void setTextShadowIndex(int textShadowIndex) {
        this.textShadowIndex = textShadowIndex;
    }

    public TextShadow getTextShadow() {
        return textShadow;
    }

    void setTextShadow(TextShadow textShadow) {
        this.textShadow = textShadow;
    }

    public int getBackgroundBorder() {
        return backgroundBorder;
    }

    public void setBackgroundBorder(int backgroundBorder) {
        this.backgroundBorder = backgroundBorder;
    }

    public int getTextHeight() {
        return textHeight;
    }

    void setTextHeight(int textHeight) {
        this.textHeight = textHeight;
    }

    public int getTextWidth() {
        return textWidth;
    }

    void setTextWidth(int textWidth) {
        this.textWidth = textWidth;
    }

    boolean isFullScreen() {
        return isFullScreen;
    }

    void setFullScreen(boolean fullScreen) {
        isFullScreen = fullScreen;
    }

    public int getPaddingWidth() {
        return paddingWidth;
    }

    public void setPaddingWidth(int paddingWidth) {
        this.paddingWidth = paddingWidth;
    }

    int getPaddingHeight() {
        return paddingHeight;
    }

    void setPaddingHeight(int paddingHeight) {
        this.paddingHeight = paddingHeight;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getTextAlpha() {
        return textAlpha;
    }

    public void setTextAlpha(int textAlpha) {
        this.textAlpha = textAlpha;
    }

    public Shader getTextShader() {
        return textShader;
    }

    void setTextShader(Shader textShader) {
        this.textShader = textShader;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getTextAlign() {
        return textAlign;
    }

    public void setTextAlign(int textAlign) {
        this.textAlign = textAlign;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public boolean isShowBackground() {
        return isShowBackground;
    }

    public void setShowBackground(boolean showBackground) {
        isShowBackground = showBackground;
    }

    public int getBackgroundAlpha() {
        return backgroundAlpha;
    }

    public void setBackgroundAlpha(int backgroundAlpha) {
        this.backgroundAlpha = backgroundAlpha;
    }
}
