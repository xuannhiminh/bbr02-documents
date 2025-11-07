package com.ezteam.baseproject.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.widget.TextView;

import com.ezteam.baseproject.R;

import java.util.List;

public class GuideEditDialog extends Dialog {
    private int currentStepIndex = 0;
    private final List<GuideStep> steps;
    private final View rootView;
    private final SpotlightOverlayView spotlightOverlay;
    private final ImageView arrowImage;
    private final LinearLayout contentBox;
    private final Button btnGotIt;
    private final Context context;
    private OnStepChangedListener onStepChangedListener;
    private final int borderColor;

    public GuideEditDialog(Context context, List<GuideStep> steps, int borderColor) {
        super(context);
        this.context = context;
        this.steps = steps;
        this.borderColor = borderColor;

        LayoutInflater inflater = LayoutInflater.from(context);
        rootView = inflater.inflate(R.layout.guide_edit_spotlight, null);
        setContentView(rootView);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            if (insetsController != null) {
                insetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                insetsController.hide(WindowInsetsCompat.Type.systemBars());
            }
        }

        spotlightOverlay = (SpotlightOverlayView) rootView.findViewById(R.id.spotlight_overlay);
        contentBox = rootView.findViewById(R.id.content_box);
        btnGotIt = rootView.findViewById(R.id.btn_got_it);
        btnGotIt.getBackground().setTint(borderColor);
        arrowImage = rootView.findViewById(R.id.arrow_image);
       // arrowImage.setColorFilter(borderColor);

        showStep(currentStepIndex);

        btnGotIt.setOnClickListener(v -> {
            currentStepIndex++;
           // spotlightOverlay.stopBlinking();
            if (currentStepIndex < steps.size()) {
                showStep(currentStepIndex);
            } else {
                dismiss();
            }
        });
    }

    private void showStep(int index) {
        if (index < 0 || index >= steps.size()) return;

        GuideStep step = steps.get(index);

        if (onStepChangedListener != null) {
            onStepChangedListener.onStepChanged(index);
        }
        try {
            step.targetView.post(() -> {
                int[] loc = new int[2];
                step.targetView.getLocationOnScreen(loc);
                int targetX = loc[0];
                int targetY = loc[1];
                int targetWidth = step.targetView.getWidth();
                int targetHeight = step.targetView.getHeight();
                spotlightOverlay.setHoleAroundView(step.targetView, 16);
//                spotlightOverlay.startBlinkingBorder(borderColor,1000L);

                int[] rootLoc = new int[2];
                rootView.getLocationOnScreen(rootLoc);

                int arrowWidth = arrowImage.getWidth();
                int arrowHeight = arrowImage.getHeight();

                int arrowX = targetX - rootLoc[0] + targetWidth / 2 - arrowWidth / 2;
                int arrowY = targetY - rootLoc[1] - arrowHeight - 16 + (int) step.arrowOffsetY;

                arrowX = Math.max(0, Math.min(arrowX, rootView.getWidth() - arrowWidth));
                arrowY = Math.max(0, Math.min(arrowY, rootView.getHeight() - arrowHeight));

                arrowImage.setX(arrowX);
                arrowImage.setY(arrowY);

                updateArrowRotation(step.step);
                arrowImage.setVisibility(View.VISIBLE);

                int childCount = contentBox.getChildCount();
                if (childCount > 1) {
                    contentBox.removeViews(0, childCount - 1);
                }

                for (String text : step.titleLines) {
                    TextView tv = new TextView(context);
                    tv.setText(text);
                    tv.setTextColor(ContextCompat.getColor(context, R.color.text1));
                    tv.setTextSize(14f);
                    tv.setPadding(0, 4, 0, 0);
                    tv.setSingleLine(false);
                    tv.setMaxLines(Integer.MAX_VALUE);
                    tv.setEllipsize(null);
                    tv.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    ));
                    contentBox.addView(tv, contentBox.getChildCount() - 1);
                }
            });
        } catch (Exception e){
            Log.e("GuideEditDialog", "error " + e);
        }

    }

    private void updateArrowRotation(int index) {
        switch (index) {
            case 0:
                arrowImage.setRotation(180);
                break;
            case 1:
                arrowImage.setRotation(90);
                break;
            case 2:
                arrowImage.setRotation(270);
                break;
            default:
                arrowImage.setRotation(0);
        }
    }

    public interface OnStepChangedListener {
        void onStepChanged(int stepIndex);
    }

    public void setOnStepChangedListener(OnStepChangedListener listener) {
        this.onStepChangedListener = listener;
    }

    public void setOnDismissListener(OnDismissListener listener) {
        super.setOnDismissListener(dialog -> {
            btnGotIt.getBackground().setTint(context.getResources().getColor(R.color.primaryColor));
            if (listener != null) listener.onDismiss(dialog);
        });
    }
}
