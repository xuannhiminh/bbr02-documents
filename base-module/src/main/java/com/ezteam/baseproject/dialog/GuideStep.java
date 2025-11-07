package com.ezteam.baseproject.dialog;

import android.view.View;
import java.util.List;

public class GuideStep {
    public View targetView;
    public List<String> titleLines;
    public float arrowOffsetY;
    public int step;

    public GuideStep(View targetView, List<String> titleLines) {
        this(targetView, titleLines, 0f, 0);
    }

    public GuideStep(View targetView, List<String> titleLines, float arrowOffsetY, int step) {
        this.targetView = targetView;
        this.titleLines = titleLines;
        this.arrowOffsetY = arrowOffsetY;
        this.step = step;
    }
}
