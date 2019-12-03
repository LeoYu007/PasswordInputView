package com.matthew.passwordinput.lib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.os.Build;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.appcompat.widget.AppCompatEditText;

/**
 * 密码、验证码输入框
 *
 * @author matheew
 * @date 2019/6/24
 */
public class PasswordInputView extends AppCompatEditText {
    private static final String TAG = "PasswordInputView";

    private Paint paint;
    private int maxLength;
    private int borderColor;
    private int pwdColor;
    private int inputBorderColor;

    private int radius;
    private int spacing;
    private int pwdStyle;
    private int borderStyle;
    private String asterisk;

    private Path path;
    private RectF rectF;
    private Xfermode xfermode;

    private float strokeWidth;
    private float boxWidth;
    private int textLength = 0;

    private float[] linesArray = new float[12];
    private float[] radiusArray = new float[8];
    private Paint.FontMetrics metrics;

    private InputListener inputListener;

    /**
     * 边框风格
     */
    @IntDef({BorderStyle.BOX, BorderStyle.LINE})
    public @interface BorderStyle {
        int BOX = 0;// 盒子
        int LINE = 1;// 下边线
    }

    /**
     * 密码风格
     */
    @IntDef({PwdStyle.CIRCLE, PwdStyle.ASTERISK, PwdStyle.PLAINTEXT})
    public @interface PwdStyle {
        int CIRCLE = 0;// 圆点
        int ASTERISK = 1;// 星号
        int PLAINTEXT = 2;// 明文
    }

    public PasswordInputView(Context context) {
        this(context, null);
    }

    public PasswordInputView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
//        this(context, attrs, R.style.PasswordInputViewStyle);
    }

    public PasswordInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        initAttribute(context, attrs);

        textLength = getText() == null ? 0 : getText().length();

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(strokeWidth);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(getTextSize());

        metrics = paint.getFontMetrics();

        path = new Path();
        rectF = new RectF();

        xfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            this.setBackground(null);
        } else {
            this.setBackgroundDrawable(null);
        }
        this.setCursorVisible(false);
        this.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
    }

    private void initAttribute(Context context, AttributeSet attrs) {
        TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.PasswordInputView);

        maxLength = t.getInt(R.styleable.PasswordInputView_pwv_maxLength, 6);
        borderColor = t.getColor(R.styleable.PasswordInputView_pwv_borderColor, Color.GRAY);
        pwdColor = t.getColor(R.styleable.PasswordInputView_pwv_pwdColor, Color.GRAY);
        inputBorderColor = t.getColor(R.styleable.PasswordInputView_pwv_haveInputBorderColor, borderColor);
        asterisk = t.getString(R.styleable.PasswordInputView_pwv_asterisk);

        if (asterisk == null || asterisk.length() == 0) asterisk = "*";
        else if (asterisk.length() > 1) this.asterisk = asterisk.substring(0, 1);

        radius = t.getDimensionPixelSize(R.styleable.PasswordInputView_pwv_radius, 0);
        strokeWidth = t.getDimensionPixelSize(R.styleable.PasswordInputView_pwv_strokeWidth, 2);
        spacing = t.getDimensionPixelSize(R.styleable.PasswordInputView_pwv_spacing, 0);
        borderStyle = t.getInt(R.styleable.PasswordInputView_pwv_borderStyle, BorderStyle.BOX);
        pwdStyle = t.getInt(R.styleable.PasswordInputView_pwv_pwdStyle, PwdStyle.CIRCLE);

        t.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int availableWidth = w - getPaddingLeft() - getPaddingRight();
        int availableHeight = h - getPaddingTop() - getPaddingBottom();

        checkSpacing(availableWidth);
        checkRadius(availableWidth, availableHeight);
    }

    // 计算boxWidth并检查圆角大小是否过大
    private void checkRadius(int availableWidth, int availableHeight) {
        // 每个盒子的宽度 = （可用宽度 - 间隔宽度）/ 盒子个数
        boxWidth = (availableWidth - (maxLength - 1f) * spacing) / maxLength;

        float availableRadius = Math.min(availableHeight / 2f, boxWidth / 2);
        if (radius > availableRadius) {
            Log.d(TAG, "radius is too large, reset it");
            radius = (int) availableRadius;
        } else if (radius < 0) {
            radius = 0;
        }
    }

    // 检查间距是否过大
    private void checkSpacing(int availableWidth) {
        if (spacing < 0 || (maxLength - 1) * spacing >= availableWidth) {
            Log.d(TAG, "spacing is too large, reset it to zero");
            spacing = 0;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
////        super.onDraw(canvas); // 去掉EditText默认的绘制

        int top = getPaddingTop();
        int bottom = getHeight() - getPaddingBottom();
        int start = getPaddingLeft();
        float left;

        for (int i = 0; i < maxLength; i++) {
            left = start + (boxWidth + spacing) * i;
            rectF.set(left, top, left + boxWidth, bottom);
            drawBorder(canvas, i);

            if (i >= textLength) continue;

            drawPassword(canvas, i);
        }
    }

    private void drawBorder(Canvas canvas, int index) {
        paint.setColor(index < textLength ? inputBorderColor : borderColor);
        paint.setStyle(Paint.Style.STROKE);
        switch (borderStyle) {
            case BorderStyle.BOX:// 边框模式
                if (radius == 0) {
                    // 圆角为0，判断间距
                    // 间距为0时第一个绘制方框，后边的每一个只绘制上、右、下三条边，避免重复绘制一条边
                    // 如果间距不为0，直接绘制方框
                    if (spacing == 0) {
                        if (index == 0) {
                            canvas.drawRect(rectF, paint);
                        } else {
                            fillLinesArray();
                            canvas.drawLines(linesArray, paint);
                        }
                    } else {
                        canvas.drawRect(rectF, paint);
                    }
                } else {
                    // 圆角!=0
                    // 策略同上，只是增加了圆角，没有间距并且有圆角的情况只绘制第一个和最后一个圆角
                    if (spacing == 0) {
                        if (index == 0) {
                            fillRadiusArray(true);
                            path.reset();
                            path.addRoundRect(rectF, radiusArray, Path.Direction.CCW);
                            canvas.drawPath(path, paint);
                        } else if (index == maxLength - 1) {
                            // 这里绘制最后一个密码框的三条边，带圆角
                            // 先绘制一个带两个圆角的方框，然后用xfermode合成去掉左边的一条边
                            int layer = canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG);

                            fillRadiusArray(false);
                            path.reset();
                            path.addRoundRect(rectF, radiusArray, Path.Direction.CCW);
                            canvas.drawPath(path, paint);

                            paint.setXfermode(xfermode);
                            canvas.drawLine(rectF.left, rectF.top, rectF.left, rectF.bottom, paint);
                            paint.setXfermode(null);

                            canvas.restoreToCount(layer);
                        } else {
                            fillLinesArray();
                            canvas.drawLines(linesArray, paint);
                        }
                    } else {
                        path.reset();
                        path.addRoundRect(rectF, radius, radius, Path.Direction.CCW);
                        canvas.drawPath(path, paint);
                    }
                }
                break;
            case BorderStyle.LINE:// 底部边线
                canvas.drawLine(rectF.left, rectF.bottom, rectF.right, rectF.bottom, paint);
                break;
        }
    }

    private void drawPassword(Canvas canvas, int index) {
        paint.setColor(pwdColor);
        paint.setStyle(Paint.Style.FILL);
        switch (pwdStyle) {
            case PwdStyle.CIRCLE:// 绘制圆点
                canvas.drawCircle((rectF.left + rectF.right) / 2, (rectF.top + rectF.bottom) / 2,
                        8, paint);
                break;
            case PwdStyle.ASTERISK:// 绘制*号
                canvas.drawText(asterisk, (rectF.left + rectF.right) / 2,
                        (rectF.top + rectF.bottom - metrics.ascent - metrics.descent) / 2, paint);
                break;
            case PwdStyle.PLAINTEXT:// 绘制明文
                canvas.drawText(String.valueOf(getText().charAt(index)), (rectF.left + rectF.right) / 2,
                        (rectF.top + rectF.bottom - metrics.ascent - metrics.descent) / 2, paint);
                break;
        }
    }

    // 间距为0,并且有圆角时，只绘制第一个和最后一个的圆角，这里填充圆角的数组
    private void fillRadiusArray(boolean firstOrLast) {
        if (firstOrLast) {
            radiusArray[0] = radius;
            radiusArray[1] = radius;
            radiusArray[2] = 0;
            radiusArray[3] = 0;
            radiusArray[4] = 0;
            radiusArray[5] = 0;
            radiusArray[6] = radius;
            radiusArray[7] = radius;
        } else {
            radiusArray[0] = 0;
            radiusArray[1] = 0;
            radiusArray[2] = radius;
            radiusArray[3] = radius;
            radiusArray[4] = radius;
            radiusArray[5] = radius;
            radiusArray[6] = 0;
            radiusArray[7] = 0;
        }
    }

    // 间距为0时，第一个绘制方框，后边的每一个框均只绘制上右下三条边，这里是添加这三条边的数组
    private void fillLinesArray() {
        linesArray[0] = rectF.left;
        linesArray[1] = rectF.top;

        linesArray[2] = rectF.right;
        linesArray[3] = rectF.top;

        linesArray[4] = rectF.right;
        linesArray[5] = rectF.top;

        linesArray[6] = rectF.right;
        linesArray[7] = rectF.bottom;

        linesArray[8] = rectF.right;
        linesArray[9] = rectF.bottom;

        linesArray[10] = rectF.left;
        linesArray[11] = rectF.bottom;
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);

        textLength = text.toString().length();
        invalidate();

        if (textLength == maxLength && inputListener != null) {
            inputListener.onInputCompleted(text.toString());
        }
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        //保证光标始终在最后
        if (selStart == selEnd) {
            setSelection(getText() == null ? 0 : getText().length());
        }
    }

    public void setRadius(int radius) {
        int availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int availableHeight = getHeight() - getPaddingTop() - getPaddingBottom();

        this.radius = radius;

        checkRadius(availableWidth, availableHeight);

        invalidate();
    }

    public void setSpacing(int spacing) {
        int availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int availableHeight = getHeight() - getPaddingTop() - getPaddingBottom();

        this.spacing = spacing;

        checkSpacing(availableWidth);
        checkRadius(availableWidth, availableHeight);

        invalidate();
    }

    public void setMaxLength(int maxLength) {
        int availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int availableHeight = getHeight() - getPaddingTop() - getPaddingBottom();

        this.maxLength = maxLength;

        checkSpacing(availableWidth);
        checkRadius(availableWidth, availableHeight);

        invalidate();
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
        invalidate();
    }

    public void setPwdColor(int pwdColor) {
        this.pwdColor = pwdColor;
        invalidate();
    }

    /**
     * 设置星号字符
     *
     * @param asterisk
     */
    public void setAsterisk(String asterisk) {
        if (asterisk == null || asterisk.length() == 0) return;

        if (asterisk.length() > 1)
            this.asterisk = asterisk.substring(0, 1);
        else
            this.asterisk = asterisk;

        invalidate();
    }

    public void setPwdStyle(@PwdStyle int pwdStyle) {
        this.pwdStyle = pwdStyle;
        invalidate();
    }

    public void setBorderStyle(@BorderStyle int borderStyle) {
        this.borderStyle = borderStyle;
        invalidate();
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        invalidate();
    }

    public void setInputListener(InputListener inputListener) {
        this.inputListener = inputListener;
    }

    public interface InputListener {
        void onInputCompleted(String text);
    }
}
