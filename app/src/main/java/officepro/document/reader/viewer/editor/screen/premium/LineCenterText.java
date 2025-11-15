package office.pdf.document.reader.viewer.editor.screen.premium;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class LineCenterText extends AppCompatTextView {
    private Rect mRect;
    private Paint mPaint;

    // we need this constructor for LayoutInflater
    public LineCenterText(Context context, AttributeSet attrs) {
        super(context, attrs);

        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStrokeWidth(3f);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(getCurrentTextColor());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int count = getLineCount();
        Rect r = mRect;
        Paint paint = mPaint;

        for (int i = 0; i < count; i++) {
            int baseline = getLineBounds(i, r);

            canvas.drawLine(r.left, r.bottom / 2, r.right, r.bottom / 2, paint);
        }

        super.onDraw(canvas);
    }
} 