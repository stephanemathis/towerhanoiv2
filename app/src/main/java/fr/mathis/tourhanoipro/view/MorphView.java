package fr.mathis.tourhanoipro.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import fr.mathis.tourhanoipro.R;
import fr.mathis.tourhanoipro.core.tools.Tools;
import fr.mathis.tourhanoipro.lib.delaunaytriangulator.DelaunayTriangulator;
import fr.mathis.tourhanoipro.lib.delaunaytriangulator.NotEnoughPointsException;
import fr.mathis.tourhanoipro.lib.delaunaytriangulator.Triangle2D;
import fr.mathis.tourhanoipro.lib.delaunaytriangulator.Vector2D;

public class MorphView extends View {

    boolean mOrientationVertical;
    float mSpeed;
    boolean mShowPoint;
    float mRandomSpeed;
    int mPointCount;

    int _viewHeight;
    int _viewWidth;
    Path mTrianglePath = new Path();
    Paint mPointPaint;
    int mPointSpace;
    float mMinGradiantSize;

    TimerIntegration mTimer;
    Vector<MorphPoint> mPoints;
    List<Triangle2D> mTriangles;
    Random mRandom;
    Map<Double, Paint> mColorMap;
    int mThemeColor;
    ArrayList<Double> mToKeepKeys;
    Bitmap mGradientBitmap;


    public MorphView(Context context) {
        super(context);

        init(context, null, 0);
    }

    public MorphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs, 0);
    }

    public MorphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr);
    }

    public MorphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true);
        mThemeColor = typedValue.data;

        Drawable drawable = null;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MorphView, defStyleAttr, 0);
        try {
            if (a != null) {
                mOrientationVertical = a.getBoolean(R.styleable.MorphView_verticalOrientation, true);
                mSpeed = a.getDimension(R.styleable.MorphView_speed, convertDpToPixel(24));
                mRandomSpeed = a.getDimension(R.styleable.MorphView_speed, convertDpToPixel(24));
                mPointCount = a.getInt(R.styleable.MorphView_pointCount, 20);
                mShowPoint = a.getBoolean(R.styleable.MorphView_showPoint, true);
                drawable = a.getDrawable(R.styleable.MorphView_drawable);
            }
        } finally {
            if (a != null)
                a.recycle();
        }

        mPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointPaint.setStyle(Paint.Style.FILL);
        mPointPaint.setAntiAlias(true);
        mPointPaint.setColor(Color.BLACK);

        mTrianglePath = new Path();
        mTrianglePath.setFillType(Path.FillType.EVEN_ODD);

        mPoints = new Vector<>();
        mToKeepKeys = new ArrayList<>();
        mTriangles = new ArrayList<>();
        mRandom = new Random();
        mPointSpace = convertDpToPixel(96);
        mMinGradiantSize = convertDpToPixel(24);

        mTimer = new TimerIntegration();
        mColorMap = new HashMap<>();

        mlogIndex = mRandom.nextInt(100);

        if (drawable == null) {
            theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimaryDark, typedValue, true);
            int dark = mThemeColor = typedValue.data;

            theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true);
            int medium = mThemeColor = typedValue.data;

            theme.resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true);
            int light = mThemeColor = typedValue.data;

            drawable = new GradientDrawable(
                    mOrientationVertical ? GradientDrawable.Orientation.TOP_BOTTOM : GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[]{dark, medium, light});
        }

        mGradientBitmap = this.createGradiantBitmap(drawable);
    }

    private Bitmap createGradiantBitmap(Drawable drawable) {


        Bitmap mutableBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mutableBitmap);
        drawable.setBounds(0, 0, 100, 100);
        drawable.draw(canvas);

        return mutableBitmap;
    }

    int mlogIndex = -1;

    private void initData(boolean fromResize) {

        if (_viewWidth == 0 || _viewHeight == 0)
            return;

        int generatedPoint = 0;

        if (fromResize && mPoints.size() > 0) {

            for (int i = 0; i < mPoints.size(); i++) {
                MorphPoint point = mPoints.get(i);

                if (point.dx != 0 || point.dy != 0) {
                    if (point.x > _viewWidth)
                        point.x = _viewWidth;
                    if (point.y > _viewHeight)
                        point.y = _viewHeight;
                    generatedPoint++;
                } else {
                    mPoints.removeElementAt(i);
                    i--;
                }
            }
        } else {

            mPoints.clear();
            mColorMap.clear();

            for (int i = 0; i < mPointCount; i++) {
                mPoints.add(new MorphPoint(
                        mRandom.nextFloat() * _viewWidth,
                        mRandom.nextFloat() * _viewHeight,
                        mRandom.nextFloat() * 10 + 7,
                        mRandom.nextInt(360),
                        mSpeed + mRandom.nextFloat() * mRandomSpeed,
                        generatedPoint)
                );

                generatedPoint++;
            }
        }

        for (int x = -mPointSpace; x < _viewWidth + mPointSpace; x = x + mPointSpace) {
            mPoints.add(new MorphPoint(x, -mPointSpace, 40, 0, 0, generatedPoint));
            generatedPoint++;
            mPoints.add(new MorphPoint(x, _viewHeight + mPointSpace, 40, 0, 0, generatedPoint));
            generatedPoint++;
        }

        for (int y = -mPointSpace; y < _viewHeight + mPointSpace; y = y + mPointSpace) {
            mPoints.add(new MorphPoint(-mPointSpace, y, 40, 0, 0, generatedPoint));
            generatedPoint++;
            mPoints.add(new MorphPoint(_viewWidth + mPointSpace, y, 40, 0, 0, generatedPoint));
            generatedPoint++;
        }
    }

    private void update() {
        float deltaTime = mTimer.getDeltaTime();

        for (MorphPoint point : mPoints) {

            if (point.x + point.dx * deltaTime > _viewWidth || point.x + point.dx * deltaTime < 0) {
                point.dx = -point.dx;
            }

            if (point.y + point.dy * deltaTime > _viewHeight || point.y + point.dy * deltaTime < 0) {
                point.dy = -point.dy;
            }

            point.x = point.x + point.dx * deltaTime;
            point.y = point.y + point.dy * deltaTime;

            if (point.dx > 0 && point.dy > 0 && (point.x > _viewWidth || point.x < 0 || point.y > _viewHeight || point.y < 0)) {
                point.x = mRandom.nextInt(_viewWidth);
                point.y = mRandom.nextInt(_viewHeight);
            }
        }

        DelaunayTriangulator delaunayTriangulator = new DelaunayTriangulator((Vector<Vector2D>) (Vector<?>) mPoints);
        try {
            delaunayTriangulator.triangulate();
            mTriangles = delaunayTriangulator.getTriangles();
        } catch (NotEnoughPointsException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas != null) {

            int height = getHeight();
            int width = getWidth();

            if (height != _viewHeight || width != _viewWidth) {
                _viewHeight = getHeight();
                _viewWidth = getWidth();
                initData(true);
            }

            if (mPoints.size() == 0)
                initData(false);

            update();

            mToKeepKeys.clear();

            for (Triangle2D triangle : mTriangles) {
                mTrianglePath.reset();
                mTrianglePath.moveTo((float) triangle.a.x, (float) triangle.a.y);
                mTrianglePath.lineTo((float) triangle.b.x, (float) triangle.b.y);
                mTrianglePath.lineTo((float) triangle.c.x, (float) triangle.c.y);

                double colorKey = ((MorphPoint) triangle.a).flag + ((MorphPoint) triangle.b).flag + ((MorphPoint) triangle.c).flag;
                if (!mColorMap.containsKey(colorKey)) {

                    Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
                    p.setStyle(Paint.Style.FILL);
                    p.setAntiAlias(false);

                    /*double modifierColor;
                    float shaderXTop, shaderXBottom, shaderYTop, shaderYBottom;
                    if (mOrientationVertical) {
                        double avgY = (triangle.a.y + triangle.b.y + triangle.c.y) / 3d;
                        modifierColor = avgY / (double) _viewHeight;
                        shaderXBottom = 0;
                        shaderXTop = 0;
                        shaderYTop = (float) Math.min(Math.min(triangle.a.y, triangle.b.y), triangle.c.y);
                        shaderYBottom = (float) Math.max(Math.max(triangle.a.y, triangle.b.y), triangle.c.y);

                        float diff = Math.abs(shaderYBottom - shaderYTop);
                        if (diff < mMinGradiantSize) {
                            shaderYBottom += (mMinGradiantSize - diff) / 2;
                            shaderYTop -= (mMinGradiantSize - diff) / 2;
                        }
                    } else {
                        double avgX = (triangle.a.x + triangle.b.x + triangle.c.x) / 3d;
                        modifierColor = avgX / (double) _viewWidth;
                        shaderYBottom = 0;
                        shaderYTop = 0;
                        shaderXTop = (float) Math.min(Math.min(triangle.a.x, triangle.b.x), triangle.c.x);
                        shaderXBottom = (float) Math.max(Math.max(triangle.a.x, triangle.b.x), triangle.c.x);

                        float diff = Math.abs(shaderXBottom - shaderXTop);
                        if (diff < mMinGradiantSize) {
                            shaderXBottom += (mMinGradiantSize - diff) / 2;
                            shaderXTop -= (mMinGradiantSize - diff) / 2;
                        }
                    }
                    float colorModifier = (float) (0.6f + (modifierColor) * 0.6);
                    p.setColor(darkenColor(mThemeColor, colorModifier));
                    p.setShader(new LinearGradient(shaderXTop, shaderYTop, shaderXBottom, shaderYBottom, darkenColor(mThemeColor, colorModifier + 0.07f * colorModifier), darkenColor(mThemeColor, colorModifier - 0.07f * colorModifier), Shader.TileMode.CLAMP));
                    */

                    double avgY = (triangle.a.y + triangle.b.y + triangle.c.y) / 3d;
                    double avgX = (triangle.a.x + triangle.b.x + triangle.c.x) / 3d;
                    int indexX = (int) (avgX / (float) _viewWidth * 100);
                    int indexY = (int) (avgY / (float) _viewHeight * 100);

                    if (indexX < 0) indexX = 0;
                    if (indexY < 0) indexY = 0;
                    if (indexX >= 100) indexX = 99;
                    if (indexY >= 100) indexY = 99;

                    int color = mGradientBitmap.getPixel(indexX, indexY);

                    p.setColor(color);
                    mColorMap.put(colorKey, p);
                }
                canvas.drawPath(mTrianglePath, mColorMap.get(colorKey));
                mToKeepKeys.add(colorKey);
            }
            if (this.mShowPoint) {
                for (MorphPoint point : mPoints) {
                    canvas.drawCircle((float) point.x, (float) point.y, point.radius, mPointPaint);
                }
            }
            mColorMap.keySet().retainAll(mToKeepKeys);

            if (mPoints.size() > 0 && mSpeed > 0 && mRandomSpeed > 0)
                invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int desiredWidth = Tools.convertDpToPixel(100 * 3);
        int desiredHeight = Tools.convertDpToPixel(200 * 3);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }

    public void start() {
        mPoints.clear();
        invalidate();
    }

    class MorphPoint extends Vector2D {

        public float dx;
        public float dy;
        public float radius;
        public float speed;
        public double flag;

        public MorphPoint(float _x, float _y, float _radius, int _direction, float _dpPerSecond, int _generatedIndex) {
            super(_x, _y);
            this.radius = _radius;
            double angle = Math.toRadians(_direction);

            this.dx = (float) Math.cos(angle) * _dpPerSecond;
            this.dy = (float) Math.sin(angle) * _dpPerSecond;

            this.speed = _dpPerSecond;

            this.flag = Math.pow(2, _generatedIndex);
        }
    }

    class TimerIntegration {
        private long previousTime = -1L;

        public void reset() {
            previousTime = -1L;
        }

        public float getDeltaTime() {
            if (previousTime == -1L)
                previousTime = System.nanoTime();

            long currentTime = System.nanoTime();
            long dt = (currentTime - previousTime) / 1000000;
            previousTime = currentTime;
            return (float) dt / 1000;
        }

        public long getTotalTimeRunning(long startTime) {
            long currentTime = System.currentTimeMillis();
            return (currentTime - startTime);
        }

    }

    public static int convertDpToPixel(float dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int convertPixelToDp(float px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    @ColorInt
    int darkenColor(@ColorInt int color, float ratio) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= ratio;
        return Color.HSVToColor(hsv);
    }
}
