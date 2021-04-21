package com.abrahamcuautle.stepprogressview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;

public class StepProgressView extends View {

    private final static String TAG_LOGGER = StepProgressView.class.getSimpleName();

    private final static int NO_POSITION = -1;

    public static final long PROGRESS_ANIMATION_DURATION = 400L;

    private final Paint mStepsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint mLinesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final TextPaint mNumbersPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    private final TextPaint mTitlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    private final Paint mPrimaryProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint mPrimaryProgressTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Path mPrimaryProgressPath = new Path();

    private final Path mPrimaryProgressTextPath = new Path();

    private final ProgressAnimation mProgressAnimation = new ProgressAnimation();

    private final ProgressTextAnimation mProgressTextAnimation = new ProgressTextAnimation();

    private Drawable mTick;

    private StaticLayout[] mStaticLayoutTitles;

    private float mSpacingStepAndTitle;

    private int mStepCount;

    private float mRadiusStep;

    private Step[] mSteps;

    private int mStepPosition = NO_POSITION;

    private float mLineX;

    private float mProgressX;

    private String[] mTitles;

    private OnStepChangedListener onStepChangedListener;

    public interface OnStepChangedListener {
        void onStepChanged(StepProgressView stepProgressView, int position);
    }

    private static void log(String message) {
        Log.d(TAG_LOGGER, message);
    }

    public StepProgressView(Context context) {
        super(context);
        readAttributes(null);
    }

    public StepProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        readAttributes(attrs);
    }

    public StepProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        readAttributes(attrs);
    }

    private void readAttributes(AttributeSet attrs) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.StepProgressView);

        mRadiusStep = ta.getDimension(R.styleable.StepProgressView_spv_radius,
                getContext().getResources().getDimension(R.dimen.spv_default_radius));

        int primaryColor = ta.getColor(R.styleable.StepProgressView_spv_primary_progress_color,
                ContextCompat.getColor(getContext(), R.color.design_default_color_primary));

        int secondaryColor = ta.getColor(R.styleable.StepProgressView_spv_secondary_progress_color,
                Color.LTGRAY);

        int tickTintColor = ta.getColor(R.styleable.StepProgressView_spv_tick_color, Color.WHITE);

        int numberTextColor = ta.getColor(R.styleable.StepProgressView_spv_number_text_color, Color.DKGRAY);
        int numberTextSize = ta.getDimensionPixelOffset(R.styleable.StepProgressView_spv_number_text_size,
                getContext().getResources().getDimensionPixelOffset(R.dimen.spv_default_number_txt_size));
        int numberTextFontFamily = ta.getResourceId(R.styleable.StepProgressView_spv_number_font_family, R.font.inter_bold);

        int titleTextColor = ta.getColor(R.styleable.StepProgressView_spv_title_text_color, Color.DKGRAY);
        int titleTextSize = ta.getDimensionPixelOffset(R.styleable.StepProgressView_spv_title_text_size,
                getContext().getResources().getDimensionPixelOffset(R.dimen.spv_default_number_txt_size));
        int titleFontFamily = ta.getResourceId(R.styleable.StepProgressView_spv_title_font_family, R.font.inter_bold);

        mSpacingStepAndTitle = ta.getDimension(R.styleable.StepProgressView_spv_spacing_step_and_title,
                getContext().getResources().getDimension(R.dimen.spv_default_spacing_step_and_text));

        int arrayTitleResId = ta.getResourceId(R.styleable.StepProgressView_spv_titles, 0);
        if (arrayTitleResId != 0) {
            mTitles = getContext().getResources().getStringArray(arrayTitleResId);
            if (mTitles.length <= 1){
                throw new IllegalStateException("Number of steps must be al lest 2");
            }
            mStepCount = mTitles.length;
        }

        ta.recycle();

        mPrimaryProgressPaint.setColor(primaryColor);
        mPrimaryProgressPaint.setStyle(Paint.Style.STROKE);
        mPrimaryProgressPaint.setStrokeWidth(mRadiusStep * 2);
        mPrimaryProgressPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        mPrimaryProgressTextPaint.setColor(Color.BLACK);
        mPrimaryProgressTextPaint.setStyle(Paint.Style.STROKE);
        mPrimaryProgressTextPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        mStepsPaint.setColor(secondaryColor);

        int lineWidth = getContext().getResources().getDimensionPixelOffset(R.dimen.spv_default_line_width);
        mLinesPaint.setColor(secondaryColor);
        mLinesPaint.setStyle(Paint.Style.STROKE);
        mLinesPaint.setStrokeWidth(lineWidth);

        Typeface numberFont = ResourcesCompat.getFont(getContext(), numberTextFontFamily);
        mNumbersPaint.setColor(numberTextColor);
        mNumbersPaint.setTextSize(numberTextSize);
        mNumbersPaint.setTypeface(numberFont);
        mNumbersPaint.setStyle(Paint.Style.FILL);
        mNumbersPaint.setTextAlign(Paint.Align.CENTER);

        Typeface titleFont = ResourcesCompat.getFont(getContext(), titleFontFamily);
        mTitlePaint.setColor(titleTextColor);
        mTitlePaint.setTextSize(titleTextSize);
        mTitlePaint.setTypeface(titleFont);
        mTitlePaint.setStyle(Paint.Style.FILL);
        mTitlePaint.setTextAlign(Paint.Align.CENTER);

        mTick = ContextCompat.getDrawable(getContext(), R.drawable.ic_tick_2);
        mTick.setTint(tickTintColor);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int height = 0;
        switch (heightMode) {
            case MeasureSpec.EXACTLY:
            case MeasureSpec.UNSPECIFIED:
                height = heightSize;
                break;
            case MeasureSpec.AT_MOST:
                height = getPreferredTopPadding() + getPreferredBottomPadding()
                        + computeOptimalTitlesHeight(widthSize) + (int) (mRadiusStep * 2) + (int) mSpacingStepAndTitle;
                break;
        }

        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, heightMode));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initStepPoints();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawStepProgress(canvas);
        drawPrimaryProgress(canvas);
        drawNumbers(canvas);
        drawTicks(canvas);
        drawTitles(canvas);
        drawProgressTitle(canvas);
    }

    private int computeOptimalTitlesHeight(int width) {
        if (mTitles == null){
            return getPreferredBottomPadding();
        }

        mStaticLayoutTitles = new StaticLayout[mStepCount];

        int neededWidth = (width - getPreferredLeftPadding() - getPreferredRightPadding()) / (mStepCount);
        neededWidth -= (int) DxPxUtils.dpToPx(getContext(),5); //Space between each title

        int maxHeightTitle = 0;
        for(int i = 0; i < mStepCount; i++) {
            StaticLayout sl = StaticLayoutCompat.obtain(
                    mTitles[i], mTitlePaint, Layout.Alignment.ALIGN_NORMAL,
                    neededWidth, 1f, 0f, false);
            maxHeightTitle = Math.max(maxHeightTitle, sl.getHeight());
            mStaticLayoutTitles[i] = sl;
        }

        mPrimaryProgressTextPaint.setStrokeWidth(maxHeightTitle);
        
        return maxHeightTitle;
    }

    private int getPreferredLeftPadding() {
        int minPadding = (int) DxPxUtils.dpToPx(getContext(),10f);
        return getPaddingLeft() == 0
                ? minPadding
                : getPaddingLeft() + minPadding;
    }

    private int getPreferredRightPadding() {
        int minPadding = (int) DxPxUtils.dpToPx(getContext(),10f);
        return getPaddingRight() == 0
                ? minPadding
                : getPaddingRight() + minPadding;
    }

    private int getPreferredTopPadding() {
        return getPaddingTop() == 0
                ? (int) DxPxUtils.dpToPx(getContext(),10f)
                : getPaddingTop();
    }

    private int getPreferredBottomPadding() {
        return getPaddingBottom() == 0 ?
                (int) DxPxUtils.dpToPx(getContext(),10f)
                : getPaddingBottom();
    }

    private void initStepPoints() {

        //This padding works for both right and left sides because every title has the same width
        int paddingTitle = 0;
        if (mStaticLayoutTitles != null){
            StaticLayout sl = mStaticLayoutTitles[0];
            paddingTitle = sl.getWidth() / 4;
        }

        int leftPadding = getPreferredLeftPadding() + paddingTitle;
        int rightPadding = getPreferredRightPadding() + paddingTitle;
        int neededWidth = getWidth() - (int) (mRadiusStep * 2) - leftPadding - rightPadding;
        int distanceBetweenSteps = mStepCount <= 1 ? neededWidth : neededWidth / (mStepCount - 1);
        int initialCx = leftPadding + (int) mRadiusStep;

        mSteps = new Step[mStepCount];
        for (int step = 0; step < mStepCount; step++) {
            Step sp = new Step();
            sp.setCx(initialCx + (step * distanceBetweenSteps));
            sp.setCy(getPreferredTopPadding() + mRadiusStep);
            mSteps[step] = sp;
        }

        if (mStepCount > 0){
            if (mStepPosition != NO_POSITION) {
                Step sp = mSteps[mStepPosition];
                checkAndUncheckSteps(mStepPosition);
                mLineX = sp.getCx() + mRadiusStep;
                StaticLayout staticLayout = mStaticLayoutTitles[mStepPosition];
                if (staticLayout != null) {
                    mProgressX = sp.getCx() + (int) (staticLayout.getWidth() / 2);
                }
            } else {
                Step sp = mSteps[0];
                mLineX = sp.getCx() - mRadiusStep;
                StaticLayout staticLayout = mStaticLayoutTitles[0];
                if (staticLayout != null) {
                    mProgressX = sp.getCx() - (int) (staticLayout.getWidth() / 2);
                }
            }
        }

    }

    private void drawStepProgress(Canvas canvas) {
        for (int step = 0;  step < mStepCount; step++) {
            Step sp = mSteps[step];
            canvas.drawCircle(sp.getCx(), sp.getCy(), mRadiusStep, mStepsPaint);
        }
        if (mStepCount > 1) {
            Step firstSp = mSteps[0];
            Step lastSp = mSteps[mStepCount - 1];
            mPrimaryProgressPath.reset();
            mPrimaryProgressPath.moveTo(firstSp.getCx(), firstSp.getCy());
            mPrimaryProgressPath.lineTo(lastSp.getCx(), lastSp.getCy());
            canvas.drawPath(mPrimaryProgressPath, mLinesPaint);
        }
    }

    private void drawNumbers(Canvas canvas) {
        for (int step = 0; step < mStepCount; step++) {
            Step sp = mSteps[step];

            if (sp.isChecked()) {
                continue;
            }

            Paint.FontMetrics fontMetrics = mNumbersPaint.getFontMetrics();
            float top = fontMetrics.top;
            float bottom = fontMetrics.bottom;
            int baseLineY = (int) (sp.getCy() - top / 2 - bottom / 2);

            canvas.drawText(String.valueOf(step + 1), sp.getCx(), baseLineY, mNumbersPaint);
        }
    }

    private void drawTicks(Canvas canvas) {
        if (mStepPosition == NO_POSITION) {
            return;
        }
        int margin = (int) DxPxUtils.dpToPx(getContext(), 8);
        for (int step = 0; step <= mStepPosition; step++){
            Step sp = mSteps[step];
            mTick.setBounds(
                    (int) (sp.getCx() - mRadiusStep + margin),
                    (int) (sp.getCy() - mRadiusStep + margin),
                    (int) (sp.getCx() + mRadiusStep - margin),
                    (int) (sp.getCy() + mRadiusStep - margin)
            );
            mTick.draw(canvas);
        }
    }

    private void drawPrimaryProgress(Canvas canvas) {
        if (mStepCount > 1) {
            Step firstSp = mSteps[0];
            mPrimaryProgressPath.reset();
            mPrimaryProgressPath.moveTo(firstSp.getCx() - mRadiusStep, firstSp.getCy());
            mPrimaryProgressPath.lineTo(mLineX , firstSp.getCy());
            canvas.drawPath(mPrimaryProgressPath, mPrimaryProgressPaint);
        }
    }

    private void drawTitles(Canvas canvas) {
        if (mStaticLayoutTitles == null){
            return;
        }

        for(int i = 0; i < mStepCount; i++) {
            StaticLayout sl = mStaticLayoutTitles[i];
            Step step = mSteps[i];
            canvas.save();
            canvas.translate(step.getCx(), step.getCy() + mRadiusStep + mSpacingStepAndTitle);
            sl.draw(canvas);
            canvas.restore();
        }

    }

    private void drawProgressTitle(Canvas canvas) {
        if (mStaticLayoutTitles == null){
            return;
        }

        if (mStepCount > 1) {
            Step firstSp = mSteps[0];
            StaticLayout firstSl = mStaticLayoutTitles[0];
            float x = firstSp.getCx() - (float) (firstSl.getWidth() / 2);
            float y = firstSp.getCy() + mRadiusStep + mSpacingStepAndTitle + (float) (firstSl.getHeight() / 2);
            mPrimaryProgressTextPath.reset();
            mPrimaryProgressTextPath.moveTo(x, y);
            mPrimaryProgressTextPath.lineTo(mProgressX, y);
            canvas.drawPath(mPrimaryProgressTextPath, mPrimaryProgressTextPaint);
        }

    }

    public void setPrimaryProgressColor(int color) {
        mPrimaryProgressPaint.setColor(color);
        invalidate();
    }

    public void setSecondaryProgressColor(int color) {
        mStepsPaint.setColor(color);
        mLinesPaint.setColor(color);
        invalidate();
    }

    public void setTickTintColor(int color) {
        mTick.setTint(color);
        invalidate();
    }

    public void setNumberTextColor(int color) {
        mNumbersPaint.setColor(color);
        invalidate();
    }

    public void setNumberTypeface(Typeface typeface) {
        mNumbersPaint.setTypeface(typeface);
        requestLayout();
    }

    public void setNumberTextSize(int textSize) {
        mNumbersPaint.setTextSize(textSize);
        requestLayout();
    }

    public void setTitlesTextColor(int color) {
        mTitlePaint.setColor(color);
        invalidate();
    }

    public void setTitlesTypeface(Typeface typeface) {
        mTitlePaint.setTypeface(typeface);
        requestLayout();
    }

    public void setTitlesTextSize(int textSize) {
        mTitlePaint.setTextSize(textSize);
        requestLayout();
    }

    public void setSpacingStepAndTitle(int spacing) {
        mSpacingStepAndTitle = spacing;
        requestLayout();
    }

    public void setRadius(int radius) {
        mRadiusStep = radius;
        mPrimaryProgressPaint.setStrokeWidth(radius * 2);
        requestLayout();
    }


    public void setStepPosition(int position) {
        if (position < 0 || position >= mStepCount){
            return;
        }

        if (mStepPosition == position) {
            return;
        }

        checkAndUncheckSteps(position);

        if (ViewCompat.isLaidOut(this) && !isLayoutRequested()) {
            mProgressAnimation.cancel();
            mProgressTextAnimation.cancel();
            mProgressAnimation.start(position);
            mProgressTextAnimation.start(position);
        }

        mStepPosition = position;

    }

    public void clear() {
        uncheckSteps();
        if (ViewCompat.isLaidOut(this) && !isLayoutRequested()) {
            mProgressAnimation.clear();
            mProgressTextAnimation.clear();
        }
        mStepPosition = NO_POSITION;
    }

    public void setOnStepChangedListener(OnStepChangedListener listener) {
        this.onStepChangedListener = listener;
    }

    private void checkAndUncheckSteps(int position) {
        if (mSteps == null){
            return;
        }

        for (int step = 0; step <= position; step++) {
            mSteps[step].setChecked(true);
        }
        for (int step = position + 1; step < mStepCount; step ++) {
            mSteps[step].setChecked(false);
        }
    }

    private void uncheckSteps() {
        if (mSteps == null){
            return;
        }

        for (int step = 0; step < mStepCount; step++) {
            mSteps[step].setChecked(false);
        }
    }

    private class ProgressAnimation {

        private ValueAnimator animator;

        public void start(int position) {
            if (position > mStepPosition) {
                forward(position);
            } else {
                backward(position);
            }
        }

        public void cancel() {
            if (animator != null && animator.isRunning()) {
                animator.cancel();
            }
        }

        private void forward(int position) {
            if (mSteps == null) {
                return;
            }
            Step nextSp = mSteps[position];
            int delta = computeDelta(position);

            animator = ValueAnimator.ofFloat(mLineX, nextSp.getCx() + mRadiusStep);
            animator.setDuration(PROGRESS_ANIMATION_DURATION + (delta * 50));
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                mLineX = (float) animation.getAnimatedValue();
                invalidate();
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (onStepChangedListener != null){
                        onStepChangedListener.onStepChanged(StepProgressView.this, position);
                    }
                }
            });
            animator.start();

        }

        private void backward(int position) {
            if (mSteps == null) {
                return;
            }
            Step nextSp = mSteps[position];
            int delta = computeDelta(position);

            animator = ValueAnimator.ofFloat(mLineX, nextSp.getCx() + mRadiusStep);
            animator.setDuration(PROGRESS_ANIMATION_DURATION + (delta * 50));
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                mLineX = (float) animation.getAnimatedValue();
                invalidate();
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (onStepChangedListener != null){
                        onStepChangedListener.onStepChanged(StepProgressView.this, position);
                    }
                }
            });
            animator.start();
        }

        private void clear(){
            if (mStepCount <= 0 || mSteps == null) {
                return;
            }

            if (mStepPosition == NO_POSITION) {
                return;
            }

            Step nextSp = mSteps[0];
            int delta = computeDelta(0);

            animator = ValueAnimator.ofFloat(mLineX, nextSp.getCx() - mRadiusStep);
            animator.setDuration(PROGRESS_ANIMATION_DURATION + (delta * 50));
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                mLineX = (float) animation.getAnimatedValue();
                invalidate();
            });
            animator.start();
        }

        private int computeDelta(int position) {
            int stepPosition = mStepPosition == NO_POSITION ? 0 :  mStepPosition;
            return Math.abs(stepPosition -  position);
        }

    }

    private class ProgressTextAnimation {

        private ValueAnimator animator;

        public void start(int position) {
            if (position > mStepPosition) {
                forward(position);
            } else {
                backward(position);
            }
        }

        public void cancel() {
            if (animator != null && animator.isRunning()) {
                animator.cancel();
            }
        }

        private void forward(int position) {
            if (mSteps == null || mStaticLayoutTitles == null) {
                return;
            }
            Step nextSp = mSteps[position];
            StaticLayout sl = mStaticLayoutTitles[position];
            int delta = computeDelta(position);

            animator = ValueAnimator.ofFloat(mProgressX, nextSp.getCx() + (float) (sl.getWidth() / 2));
            animator.setDuration(PROGRESS_ANIMATION_DURATION + (delta * 50));
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                mProgressX = (float) animation.getAnimatedValue();
                invalidate();
            });
            animator.start();

        }

        private void backward(int position) {
            if (mSteps == null || mStaticLayoutTitles == null) {
                return;
            }
            Step nextSp = mSteps[position];
            StaticLayout sl = mStaticLayoutTitles[position];
            int delta = computeDelta(position);

            animator = ValueAnimator.ofFloat(mProgressX, nextSp.getCx() + (float) (sl.getWidth() / 2));
            animator.setDuration(PROGRESS_ANIMATION_DURATION + (delta * 50));
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                mProgressX = (float) animation.getAnimatedValue();
                invalidate();
            });
            animator.start();
        }

        private void clear(){
            if (mStepCount <= 0 || mSteps == null || mStaticLayoutTitles == null) {
                return;
            }

            if (mStepPosition == NO_POSITION) {
                return;
            }

            Step nextSp = mSteps[0];
            StaticLayout sl = mStaticLayoutTitles[0];
            int delta = computeDelta(0);

            animator = ValueAnimator.ofFloat(mProgressX, nextSp.getCx() - (float) (sl.getWidth() / 2));
            animator.setDuration(PROGRESS_ANIMATION_DURATION + (delta * 50));
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                mProgressX = (float) animation.getAnimatedValue();
                invalidate();
            });
            animator.start();
        }

        private int computeDelta(int position) {
            int stepPosition = mStepPosition == NO_POSITION ? 0 :  mStepPosition;
            return Math.abs(stepPosition -  position);
        }

    }

}
