package fr.mathis.tourhanoipro.view.game;

import android.annotation.SuppressLint;
import android.content.Context;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import java.util.ArrayList;

import fr.mathis.tourhanoipro.view.game.listener.HelpListener;
import fr.mathis.tourhanoipro.view.game.listener.QuickTouchListener;
import fr.mathis.tourhanoipro.view.game.listener.TurnListener;
import fr.mathis.tourhanoipro.view.game.model.ClassCircle;
import fr.mathis.tourhanoipro.view.game.model.ClassField;
import fr.mathis.tourhanoipro.view.game.model.ClassTower;
import fr.mathis.tourhanoipro.view.game.model.QuickTouch;
import fr.mathis.tourhanoipro.R;
import fr.mathis.tourhanoipro.core.tools.Tools;

public class GameView extends View {

    public static int MODE_GOAL = 0;
    public static int MODE_MULTIPLE = 1;
    public static int MODE_SIZE = 2;
    public static int MODE_COLOR_PICKER = 3;

    private static final float TOUCH_TOLERANCE = 4;

    // state
    ClassField _currentGameField;
    boolean _isBuildingQuickZone;
    boolean _currentTouchIsInquickTouchZone;
    int _currentGameDiskNumber;
    int _currentGameMovesCount;
    int _currentGameRequiredMinCount;
    long _currentGameSessionDuration;
    long _currentGameSavedDurationlastGame = -1;
    Point[] _startAndEndTouchPoint;
    boolean _shouldDrawHelpLine;
    boolean _isQtEditMode;
    int _currentGameMode;
    boolean _isTouchDisabled;
    int _backgroundColor;
    boolean _isMovingQuickTouch;

    // draw variables
    Bitmap _moveBitmap;
    Bitmap _qtBitmap;
    int _viewHeight;
    int _viewWidth;
    Rect _reusableRect;
    RectF _reusableRectF;
    Paint _elementsPaint;
    Paint _fingerLinePaint;
    Paint _bitmapPaint;
    Path _fingerLinePath;
    int helpLineColor;
    Path _diskRoundPath;
    float[] _diskCorners;

    // touch variables
    float latestTouchPositionX;
    float latestTouchPositionY;
    Point _qtStartEdgeBuilding = null;
    Point _qtEndEdgeBuilding = null;
    private static int TOUCH_MODE_SWIPE = 0;
    private static int TOUCH_MODE_TAP = 1;
    int touchMode = TOUCH_MODE_SWIPE;

    // listeners
    TurnListener _turnListener;
    HelpListener _helpListener;
    QuickTouchListener _quickTouchListener;

    public GameView(Context context) {
        super(context);

        init(context);
    }

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public GameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    public GameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(context);
    }

    public static String getNewSaveData(Context context, int diskCount) {
        GameView gv = new GameView(context);
        gv.createNewGame(diskCount);

        return gv.saveGameAsString();
    }

    private void init(Context context) {
        setFocusable(true);

        _currentGameSessionDuration = 0;
        _currentGameMode = -1;
        _isTouchDisabled = false;
        _shouldDrawHelpLine = false;
        _isBuildingQuickZone = false;
        _currentTouchIsInquickTouchZone = false;
        _isMovingQuickTouch = false;
        _isQtEditMode = false;
        _reusableRect = new Rect(0, 0, 0, 0);
        _reusableRectF = new RectF(0, 0, 0, 0);

        _backgroundColor = Color.WHITE;

        _elementsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _elementsPaint.setStyle(Paint.Style.FILL);
        _elementsPaint.setStrokeWidth(Tools.convertDpToPixel(1.0f));
        _elementsPaint.setDither(true);
        _elementsPaint.setStrokeJoin(Paint.Join.ROUND);
        _elementsPaint.setStrokeCap(Paint.Cap.ROUND);
        _elementsPaint.setAntiAlias(true);
        _elementsPaint.setTextSize(Tools.convertDpToPixel(32));

        _bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _bitmapPaint.setDither(true);

        _fingerLinePaint = new Paint();
        _fingerLinePaint.setAntiAlias(true);
        _fingerLinePaint.setDither(true);
        _fingerLinePaint.setStyle(Paint.Style.STROKE);
        _fingerLinePaint.setStrokeJoin(Paint.Join.ROUND);
        _fingerLinePaint.setStrokeCap(Paint.Cap.ROUND);
        _fingerLinePaint.setStrokeWidth(Tools.convertDpToPixel(2));

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true);
        helpLineColor = typedValue.data;

        diskColors = new int[]{
                Color.parseColor("#33B5E5"),
                Color.parseColor("#99CC00"),
                Color.parseColor("#FF4444"),
                Color.parseColor("#FFBB33"),
                Color.parseColor("#AA66CC"),
        };

        _diskCorners = new float[]{
                Tools.convertDpToPixel(2), Tools.convertDpToPixel(2),        // Top left radius in px
                Tools.convertDpToPixel(2), Tools.convertDpToPixel(2),        // Top right radius in px
                Tools.convertDpToPixel(2), Tools.convertDpToPixel(2),          // Bottom right radius in px
                Tools.convertDpToPixel(2), Tools.convertDpToPixel(2)           // Bottom left radius in px
        };

        _diskRoundPath = new Path();

        createNewGame(5);
    }

    public void createNewGame(int size) {
        _currentGameSessionDuration = 0;
        _currentGameSavedDurationlastGame = -1;
        lastMovedEndTower = -1;
        lastMovedStartTower = -1;

        QuickTouch q = null;

        if (_currentGameField != null && _currentGameField.getQtCopy() != null)
            q = _currentGameField.getQtCopy();

        _currentGameField = new ClassField();
        _currentGameField.setQt(q);
        _currentGameField.setId(0);

        ArrayList<ClassTower> towers = new ArrayList<ClassTower>();
        for (int i = 0; i < 3; i++) {
            ClassTower tower = new ClassTower();
            tower.setId(i);
            tower.setCircles(new ArrayList<ClassCircle>());
            towers.add(tower);
        }

        _currentGameDiskNumber = size;
        _currentGameMovesCount = 0;
        _currentGameRequiredMinCount = (int) (Math.pow(2, _currentGameDiskNumber) - 1);
        if (_turnListener != null) {
            _turnListener.turnPlayed(0, _currentGameRequiredMinCount);
        }
        for (int i = _currentGameDiskNumber; i > 0; i--) {
            towers.get(0).getCircles().add(new ClassCircle(i, diskColors[i % diskColors.length]));
        }

        _currentGameField.setTowers(towers);

        this.invalidate();
    }

    public void cleanTouch() {
        _startAndEndTouchPoint = new Point[2];
        _fingerLinePath = new Path();
        _currentTouchIsInquickTouchZone = false;
        this.invalidate();
    }

    public void setColorPalette(int[] _colors) {
        diskColors = _colors;
    }

    int[] diskColors;
    boolean showNumberedDisks;

    public void launchGame(String value) {

        String[] values = value.split(";");

        String v1 = values[0];
        String[] towers = v1.split(":");
        QuickTouch q = null;
        if (_currentGameField != null && _currentGameField.getQtCopy() != null)
            q = _currentGameField.getQtCopy();
        _currentGameField = new ClassField();
        _currentGameField.setQt(q);
        _currentGameField.setTowers(new ArrayList<ClassTower>());
        int i = 0;

        for (String t : towers) {
            if (i != 0) {
                ClassTower tower = new ClassTower();
                tower.setCircles(new ArrayList<ClassCircle>());

                _currentGameField.getTowers().add(tower);

                if (t != null && t.length() != 0 && t.compareTo("n") != 0) {
                    String[] circles = t.split(",");
                    for (String c : circles) {
                        if (i != 0) {
                            if (c != null && c.length() != 0 && c.compareTo("n") != 0) {
                                ClassCircle circle = new ClassCircle(Integer.parseInt(c), diskColors[Integer.parseInt(c) % diskColors.length]);
                                tower.getCircles().add(circle);
                            }
                        }
                    }
                }
            }
            i++;
        }

        _currentGameDiskNumber = Integer.parseInt(values[1]);
        _currentGameMovesCount = Integer.parseInt(values[2]);
        _currentGameRequiredMinCount = Integer.parseInt(values[3]);
        if (values.length > 4)
            _currentGameSessionDuration = Long.parseLong(values[4]);
        else
            _currentGameSessionDuration = 0;
        _currentGameSavedDurationlastGame = -1;

        if (_turnListener != null) {
            _turnListener.turnPlayed(_currentGameMovesCount, _currentGameRequiredMinCount);
        }

        this.invalidate();
    }

    public String saveGameAsString() {
        String res = "";

        String valuesString = "";

        for (ClassTower ct : _currentGameField.getTowers()) {
            valuesString += ":";
            for (ClassCircle cc : ct.getCircles()) {
                valuesString += "," + cc.getId();
            }
            if (ct.getCircles().size() == 0)
                valuesString += "n";
        }

        valuesString += ";" + _currentGameDiskNumber;
        valuesString += ";" + _currentGameMovesCount;
        valuesString += ";" + _currentGameRequiredMinCount;

        valuesString += ";" + (_currentGameSessionDuration + (_currentGameSavedDurationlastGame > 0 ? System.currentTimeMillis() - _currentGameSavedDurationlastGame : 0));

        res = valuesString;

        return res;
    }

    public void setTouchMode(String mode) {
        if (mode.compareTo("swipe") == 0)
            touchMode = TOUCH_MODE_SWIPE;
        else
            touchMode = TOUCH_MODE_TAP;
    }

    int tapModeFrom = -1;
    int tapModeTo = -1;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (_isTouchDisabled)
            return false;
        else {
            if (_isBuildingQuickZone) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    _qtStartEdgeBuilding = new Point((int) event.getX(), (int) event.getY());
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    _qtEndEdgeBuilding = new Point((int) event.getX(), (int) event.getY());
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (_qtStartEdgeBuilding != null && _qtEndEdgeBuilding != null) {
                        _isBuildingQuickZone = false;

                        int x, y, w, h;
                        x = _qtStartEdgeBuilding.x < _qtEndEdgeBuilding.x ? _qtStartEdgeBuilding.x : _qtEndEdgeBuilding.x;
                        y = _qtStartEdgeBuilding.y < _qtEndEdgeBuilding.y ? _qtStartEdgeBuilding.y : _qtEndEdgeBuilding.y;

                        w = Math.abs(_qtStartEdgeBuilding.x - _qtEndEdgeBuilding.x);
                        h = Math.abs(_qtStartEdgeBuilding.y - _qtEndEdgeBuilding.y);

                        QuickTouch qt = new QuickTouch();
                        if (_shouldDrawHelpLine) {
                            qt.setWidth(Tools.convertDpToPixel(160.0f));
                            qt.setHeight(Tools.convertDpToPixel(80.0f));
                            qt.setTop(_viewHeight / 2 - Tools.convertDpToPixel(40.0f));
                            qt.setLeft(_viewWidth - Tools.convertDpToPixel(168.0f));
                        } else {
                            qt.setWidth(w);
                            qt.setHeight(h);
                            qt.setTop(y);
                            qt.setLeft(x);
                        }

                        _currentGameField.setQt(qt);
                        _qtEndEdgeBuilding = null;
                        _qtStartEdgeBuilding = null;
                        _shouldDrawHelpLine = false;
                        if (_helpListener != null)
                            _helpListener.stepPassed(0);
                        if (_quickTouchListener != null)
                            _quickTouchListener.quickTouchConstructed(qt);
                    } else {
                        _currentGameField.setQt(null);
                        _qtEndEdgeBuilding = null;
                        _qtStartEdgeBuilding = null;
                        _isBuildingQuickZone = false;
                        _shouldDrawHelpLine = false;
                    }
                }
                this.invalidate();
            } /*
             * else if (_isQtEditMode) { if (event.getAction() == MotionEvent.ACTION_DOWN) { latestTouchPositionX = event.getX(); latestTouchPositionY = event.getY(); } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
             *
             * QuickTouch movedQt = _currentGameField.getQtCopy();
             *
             * int newLeft = (int) (movedQt.getLeft() + (event.getX() - latestTouchPositionX)); if (newLeft < 0) newLeft = 0;
             *
             * if (newLeft + movedQt.getWidth() > _viewWidth) newLeft = _viewWidth - movedQt.getWidth();
             *
             * int newTop = (int) (movedQt.getTop() + (event.getY() - latestTouchPositionY)); if (newTop < 0) newTop = 0;
             *
             * if (newTop + movedQt.getHeight() > _viewHeight) newTop = _viewHeight - movedQt.getHeight();
             *
             * movedQt.setLeft(newLeft); movedQt.setTop(newTop);
             *
             * setQt(movedQt);
             *
             * latestTouchPositionX = event.getX(); latestTouchPositionY = event.getY(); } else if (event.getAction() == MotionEvent.ACTION_UP) { _isQtEditMode = false; _bitmapPaint = null; } this.invalidate(); }
             */ else {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    if (_isQtEditMode) {
                        if (_currentGameField.getQtCopy() != null) {
                            int t, w, h, l;
                            Point p = new Point((int) event.getX(), (int) event.getY());
                            t = _currentGameField.getQtCopy().getTop();
                            w = _currentGameField.getQtCopy().getWidth();
                            h = _currentGameField.getQtCopy().getHeight();
                            l = _currentGameField.getQtCopy().getLeft();

                            if (p.x < l + w && p.x > l && p.y > t && p.y < t + h) {

                                _isMovingQuickTouch = true;
                                latestTouchPositionX = event.getX();
                                latestTouchPositionY = event.getY();
                                invalidate();
                                return true;
                            }
                        }
                    }

                    _startAndEndTouchPoint = new Point[2];
                    _fingerLinePath = new Path();
                    _startAndEndTouchPoint[0] = new Point((int) event.getX(), (int) event.getY());
                    _fingerLinePath.moveTo(event.getX(), event.getY());
                    latestTouchPositionX = event.getX();
                    latestTouchPositionY = event.getY();
                    if (_currentGameField.getQtCopy() != null) {
                        int t, w, h, l;
                        Point p = new Point((int) event.getX(), (int) event.getY());
                        t = _currentGameField.getQtCopy().getTop();
                        w = _currentGameField.getQtCopy().getWidth();
                        h = _currentGameField.getQtCopy().getHeight();
                        l = _currentGameField.getQtCopy().getLeft();

                        if (p.x < l + w && p.x > l && p.y > t && p.y < t + h) {

                            _currentTouchIsInquickTouchZone = true;

                            p.x = p.x - l;

                            if (p.x < w / 3)
                                _startAndEndTouchPoint[0] = new Point(_viewWidth / 6, _viewHeight * 2 / 5);
                            else if (p.x < w * 2 / 3)
                                _startAndEndTouchPoint[0] = new Point(_viewWidth / 2, _viewHeight * 2 / 5);
                            else
                                _startAndEndTouchPoint[0] = new Point(_viewWidth / 2 + _viewWidth / 6 + _viewWidth / 6, _viewHeight * 2 / 5);
                        }
                    }

                    if (touchMode == TOUCH_MODE_TAP) {
                        if (tapModeFrom == -1) {
                            if (_startAndEndTouchPoint[0].x < _viewWidth / 3) {
                                tapModeFrom = 0;
                                _startAndEndTouchPoint[0] = new Point(_viewWidth / 6, _viewHeight * 2 / 5);
                            } else if (_startAndEndTouchPoint[0].x < _viewWidth * 2 / 3) {
                                tapModeFrom = 1;
                                _startAndEndTouchPoint[0] = new Point(_viewWidth / 2, _viewHeight * 2 / 5);
                            } else {
                                tapModeFrom = 2;
                                _startAndEndTouchPoint[0] = new Point(_viewWidth / 2 + _viewWidth / 6 + _viewWidth / 6, _viewHeight * 2 / 5);
                            }
                        } else {
                            if (_startAndEndTouchPoint[0].x < _viewWidth / 3)
                                tapModeTo = 0;
                            else if (_startAndEndTouchPoint[0].x < _viewWidth * 2 / 3)
                                tapModeTo = 1;
                            else
                                tapModeTo = 2;
                        }

                        if (tapModeFrom != -1) {
                            if (_currentGameField.getTowers().get(tapModeFrom).getCircles().size() == 0)
                                tapModeFrom = -1;
                        }

                        if (tapModeFrom != -1 && tapModeTo != -1) {
                            moveOneCircle(tapModeFrom, tapModeTo);
                            tapModeFrom = -1;
                            tapModeTo = -1;
                            _startAndEndTouchPoint = new Point[2];
                        }
                    }


                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    Point p = new Point((int) event.getX(), (int) event.getY());

                    if (_isMovingQuickTouch) {
                        QuickTouch movedQt = _currentGameField.getQtCopy();

                        int newLeft = (int) (movedQt.getLeft() + (event.getX() - latestTouchPositionX));
                        if (newLeft < 0)
                            newLeft = 0;

                        if (newLeft + movedQt.getWidth() > _viewWidth)
                            newLeft = _viewWidth - movedQt.getWidth();

                        int newTop = (int) (movedQt.getTop() + (event.getY() - latestTouchPositionY));
                        if (newTop < 0)
                            newTop = 0;

                        if (newTop + movedQt.getHeight() > _viewHeight)
                            newTop = _viewHeight - movedQt.getHeight();

                        movedQt.setLeft(newLeft);
                        movedQt.setTop(newTop);

                        setQt(movedQt);

                        latestTouchPositionX = event.getX();
                        latestTouchPositionY = event.getY();
                    } else if (_currentTouchIsInquickTouchZone) {
                        if (touchMode == TOUCH_MODE_SWIPE) {
                            int t, w, h, l;
                            t = _currentGameField.getQtCopy().getTop();
                            w = _currentGameField.getQtCopy().getWidth();
                            h = _currentGameField.getQtCopy().getHeight();
                            l = _currentGameField.getQtCopy().getLeft();

                            if (p.x < l)
                                p.x = l;
                            if (p.x > l + w)
                                p.x = l + w;
                            if (p.y < t)
                                p.y = t;
                            if (p.y > t + h)
                                p.y = t + h;

                            p.x = p.x - l;

                            if (p.x < w / 3)
                                _startAndEndTouchPoint[1] = new Point(_viewWidth / 6, _viewHeight * 2 / 5);
                            else if (p.x < w * 2 / 3)
                                _startAndEndTouchPoint[1] = new Point(_viewWidth / 2, _viewHeight * 2 / 5);
                            else
                                _startAndEndTouchPoint[1] = new Point(_viewWidth / 2 + _viewWidth / 6 + _viewWidth / 6, _viewHeight * 2 / 5);
                        }
                    } else {
                        if (touchMode == TOUCH_MODE_SWIPE) {
                            _startAndEndTouchPoint[1] = new Point((int) event.getX(), (int) event.getY());
                            int historySize = event.getHistorySize();
                            for (int i = 0; i < historySize; i++) {
                                float historicalX = event.getHistoricalX(i);
                                float historicalY = event.getHistoricalY(i);

                                float dx = Math.abs(event.getHistoricalX(i) - latestTouchPositionX);
                                float dy = Math.abs(event.getHistoricalY(i) - latestTouchPositionY);

                                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                                    _fingerLinePath.quadTo(latestTouchPositionX, latestTouchPositionY, (historicalX + latestTouchPositionX) / 2, (historicalY + latestTouchPositionY) / 2);
                                    latestTouchPositionX = historicalX;
                                    latestTouchPositionY = historicalY;
                                }

                                _fingerLinePath.quadTo(latestTouchPositionX, latestTouchPositionY, historicalX, historicalY);
                                latestTouchPositionX = historicalX;
                                latestTouchPositionY = historicalY;
                            }
                            _fingerLinePath.quadTo(latestTouchPositionX, latestTouchPositionY, event.getX(), event.getY());
                            latestTouchPositionX = event.getX();
                            latestTouchPositionY = event.getY();
                        }
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {

                    if (_isMovingQuickTouch) {
                        _isQtEditMode = false;
                        _isMovingQuickTouch = false;
                        _bitmapPaint = null;
                        _quickTouchListener.quickTouchUpdated(this.getQt());
                    } else {
                        if (_currentTouchIsInquickTouchZone)
                            if (_helpListener != null)
                                _helpListener.stepPassed(1);

                        if (touchMode == TOUCH_MODE_SWIPE) {
                            moveOneCircle(_startAndEndTouchPoint);
                            _startAndEndTouchPoint = new Point[2];
                            _currentTouchIsInquickTouchZone = false;
                            _fingerLinePath = new Path();
                        }
                    }
                }

                this.invalidate();
            }

            return true;
        }
    }

    private void moveOneCircle(Point[] line) {
        if (line[0] != null && line[1] != null) {
            int startX = line[0].x;
            int endX = line[1].x;
            int startTower = 0;
            int endTower = 0;

            if (startX <= _viewWidth * 1 / 3)
                startTower = 0;
            else if (startX < _viewWidth * 2 / 3)
                startTower = 1;
            else
                startTower = 2;

            if (endX <= _viewWidth * 1 / 3)
                endTower = 0;
            else if (endX < _viewWidth * 2 / 3)
                endTower = 1;
            else
                endTower = 2;

            moveOneCircle(startTower, endTower);
        }
    }

    int lastMovedStartTower = -1;
    int lastMovedEndTower = -1;

    private void moveOneCircle(int startTower, int endTower) {

        if (startTower != endTower) {

            if (_currentGameSavedDurationlastGame == -1)
                _currentGameSavedDurationlastGame = System.currentTimeMillis();

            int nbCirclesStartTower = _currentGameField.getTowers().get(startTower).getCircles().size();
            ClassCircle toMove = null;
            ClassCircle toBeSecond = null;
            if (nbCirclesStartTower > 0) {
                boolean isAllowed = true;
                toMove = _currentGameField.getTowers().get(startTower).getCircles().get(nbCirclesStartTower - 1);
                if (_currentGameField.getTowers().get(endTower).getCircles().size() > 0) {
                    toBeSecond = _currentGameField.getTowers().get(endTower).getCircles().get(_currentGameField.getTowers().get(endTower).getCircles().size() - 1);
                }
                if (toBeSecond != null && toMove.getId() > toBeSecond.getId()) {
                    isAllowed = false;
                }
                if (isAllowed) {
                    lastMovedStartTower = startTower;
                    lastMovedEndTower = endTower;
                    _currentGameField.getTowers().get(startTower).getCircles().remove(nbCirclesStartTower - 1);
                    _currentGameField.getTowers().get(endTower).getCircles().add(toMove);
                    _currentGameMovesCount++;

                    if (_turnListener != null) {
                        _turnListener.turnPlayed(_currentGameMovesCount, _currentGameRequiredMinCount);
                    }
                    CheckGameWin();

                }
            }
        }
    }

    private void CheckGameWin() {
        ArrayList<ClassCircle> circles = _currentGameField.getTowers().get(2).getCircles();
        boolean win = true;
        int value = _currentGameDiskNumber;
        if (circles.size() == _currentGameDiskNumber) {
            for (int i = 0; i < circles.size(); i++) {
                if (circles.get(i).getId() != value) {
                    win = false;
                    break;
                }
                value--;
            }
        }

        if (value != 0)
            win = false;

        if (!win) {
            win = true;
            circles = _currentGameField.getTowers().get(1).getCircles();
            value = _currentGameDiskNumber;
            if (circles.size() == _currentGameDiskNumber) {
                for (int i = 0; i < circles.size(); i++) {
                    if (circles.get(i).getId() != value) {
                        win = false;
                        break;
                    }
                    value--;
                }
            }

            if (value != 0)
                win = false;
        }

        if (win) {
            lastMovedEndTower = -1;
            lastMovedStartTower = -1;
            if (_turnListener != null) {
                _turnListener.gameFinished(_currentGameMovesCount, _currentGameRequiredMinCount, (_currentGameSessionDuration + (System.currentTimeMillis() - _currentGameSavedDurationlastGame)));
            }
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        if (canvas != null) {

            //canvas.drawColor(_backgroundColor);

            ClassCircle selectedCircle = null;
            ClassCircle belowCircle = null;
            // get circle index
            if (_startAndEndTouchPoint != null && _startAndEndTouchPoint[0] != null) {
                int startX = _startAndEndTouchPoint[0].x;
                int endX = _startAndEndTouchPoint[1] == null ? _startAndEndTouchPoint[0].x : _startAndEndTouchPoint[1].x;
                if (startX <= _viewWidth * 1 / 3) {
                    selectedCircle = _currentGameField.getTowers().get(0).getCircles().size() > 0 ? _currentGameField.getTowers().get(0).getCircles().get(_currentGameField.getTowers().get(0).getCircles().size() - 1) : null;
                } else if (startX < _viewWidth * 2 / 3) {
                    selectedCircle = _currentGameField.getTowers().get(1).getCircles().size() > 0 ? _currentGameField.getTowers().get(1).getCircles().get(_currentGameField.getTowers().get(1).getCircles().size() - 1) : null;
                } else {
                    selectedCircle = _currentGameField.getTowers().get(2).getCircles().size() > 0 ? _currentGameField.getTowers().get(2).getCircles().get(_currentGameField.getTowers().get(2).getCircles().size() - 1) : null;
                }

                if (endX <= _viewWidth * 1 / 3) {
                    belowCircle = _currentGameField.getTowers().get(0).getCircles().size() > 0 ? _currentGameField.getTowers().get(0).getCircles().get(_currentGameField.getTowers().get(0).getCircles().size() - 1) : null;
                } else if (endX < _viewWidth * 2 / 3) {
                    belowCircle = _currentGameField.getTowers().get(1).getCircles().size() > 0 ? _currentGameField.getTowers().get(1).getCircles().get(_currentGameField.getTowers().get(1).getCircles().size() - 1) : null;
                } else {
                    belowCircle = _currentGameField.getTowers().get(2).getCircles().size() > 0 ? _currentGameField.getTowers().get(2).getCircles().get(_currentGameField.getTowers().get(2).getCircles().size() - 1) : null;
                }

                if (selectedCircle != null) {
                    if (belowCircle == null || selectedCircle.getId() <= belowCircle.getId())
                        _elementsPaint.setColor(selectedCircle.getColor());
                    else
                        _elementsPaint.setColor(Color.DKGRAY);
                    _elementsPaint.setAlpha(40);
                    int currentX = endX;
                    if (currentX <= _viewWidth * 1 / 3)
                        canvas.drawRect(0, 0, _viewWidth * 1 / 3, _viewHeight, _elementsPaint);
                    else if (currentX < _viewWidth * 2 / 3)
                        canvas.drawRect(_viewWidth * 1 / 3, 0, _viewWidth * 2 / 3, _viewHeight, _elementsPaint);
                    else
                        canvas.drawRect(_viewWidth * 2 / 3, 0, _viewWidth, _viewHeight, _elementsPaint);
                }

                _elementsPaint.setColor(Color.rgb(0, 0, 0));
                _elementsPaint.setAlpha(255);
            }

            if (_currentGameMode == MODE_MULTIPLE) {
                _elementsPaint.setColor(Color.DKGRAY);
                _elementsPaint.setAlpha(40);
                canvas.drawRect(_viewWidth * 2 / 3, 0, _viewWidth, _viewHeight, _elementsPaint);
            }

            if (selectedCircle == null)
                selectedCircle = new ClassCircle(-1, -1);

            int x = 0;
            int y = _viewHeight;
            int i = 1;
            int circleHeight = (int)(_viewWidth / 20);

            if (circleHeight * _currentGameDiskNumber > _viewHeight) {
                circleHeight = (int) (_viewHeight * 0.85f) / _currentGameDiskNumber;
            }

            if (_currentGameField != null) {
                if (_currentGameMode == MODE_COLOR_PICKER) {
                    x = _viewWidth / 2;
                    y = _viewHeight;
                    ClassTower tower = _currentGameField.getTowers().get(0);
                    for (ClassCircle cercle : tower.getCircles()) {

                        int circleWidth = (_viewWidth - 10) * ((cercle.getId()) * 100 / _currentGameDiskNumber) / 100;

                        circleHeight = _viewWidth / 5;

                        if (circleHeight * _currentGameDiskNumber > _viewHeight) {
                            circleHeight = (int) (_viewHeight * 0.95f) / _currentGameDiskNumber;
                        }

                        _elementsPaint.setColor(cercle.getColor());
                        _reusableRect.set(x - (circleWidth / 2), y - (circleHeight), x + (circleWidth / 2), y);
                        _elementsPaint.setAlpha(255);
                        canvas.drawRect(_reusableRect, _elementsPaint);
                        y -= circleHeight;
                    }
                } else {
                    for (ClassTower tower : _currentGameField.getTowers()) {
                        x = (_viewWidth * i / 3) - ((_viewWidth * 1 / 3) / 2);
                        y = _viewHeight;

                        int barWidth = Tools.convertDpToPixel(8.0f);

                        _elementsPaint.setStrokeWidth(barWidth);
                        _elementsPaint.setColor(Color.parseColor("#AAAAAA"));
                        canvas.drawLine((_viewWidth * i / 3) - (_viewWidth * 1/6), _viewHeight - Math.min(circleHeight * (_currentGameDiskNumber + 1), 0.90f * _viewHeight), (_viewWidth * i / 3)- (_viewWidth * 1/6), _viewHeight , _elementsPaint);
                        _elementsPaint.setStrokeWidth(Tools.convertDpToPixel(1.0f));

                        for (ClassCircle cercle : tower.getCircles()) {

                            int circleWidth = this.getCircleWidth(cercle, _viewWidth, _currentGameDiskNumber);
                            _elementsPaint.setColor(cercle.getColor());
                            _reusableRectF.set(x - (circleWidth / 2), y - (circleHeight), x + (circleWidth / 2), y);

                            if (selectedCircle.getId() == cercle.getId() || (_currentGameMode == MODE_GOAL && i == 1)) {
                                _elementsPaint.setAlpha(selectedCircle.getId() == cercle.getId() ? 40 : 150);
                            }

                            _diskRoundPath.reset();
                            _diskRoundPath.addRoundRect(_reusableRectF, _diskCorners, Path.Direction.CW);
                            canvas.drawPath(_diskRoundPath, _elementsPaint);

                            y -= circleHeight;

                            _elementsPaint.setColor(Color.WHITE);
                            if (selectedCircle.getId() == cercle.getId() || (_currentGameMode == MODE_GOAL && i == 1)) {
                                _elementsPaint.setAlpha(selectedCircle.getId() == cercle.getId() ? 40 : 150);
                            }

                            if(this.showNumberedDisks) {
                                _elementsPaint.setTextSize(circleHeight * 0.4f);
                                _elementsPaint.getTextBounds(cercle.getId() + "", 0, (cercle.getId() + "").length(), _reusableRect);
                                canvas.drawText(cercle.getId() + "", x - _reusableRect.exactCenterX(), y + circleHeight / 2f - _reusableRect.exactCenterY(), _elementsPaint);
                            }
                        }
                        if (i != 3) {
                            _elementsPaint.setStrokeWidth(1);
                            _elementsPaint.setColor(Color.parseColor("#AAAAAA"));
                            canvas.drawLine((_viewWidth * i / 3), 0, (_viewWidth * i / 3), _viewHeight, _elementsPaint);
                            _elementsPaint.setStrokeWidth(Tools.convertDpToPixel(1.0f));
                        }
                        i++;
                    }
                }
            }

            // draw selected circle
            if (_startAndEndTouchPoint != null && _startAndEndTouchPoint[0] != null && selectedCircle.getId() != -1) {

                // draw line
                if (!_currentTouchIsInquickTouchZone) {
                    _fingerLinePaint.setColor(selectedCircle.getColor());
                    if (_fingerLinePath != null) {
                        canvas.drawPath(_fingerLinePath, _fingerLinePaint);
                    }
                }

                int circleWidth = this.getCircleWidth(selectedCircle, _viewWidth, _currentGameDiskNumber);

                if (_startAndEndTouchPoint[1] != null) {
                    x = _startAndEndTouchPoint[1].x;
                    y = _startAndEndTouchPoint[1].y;
                } else {
                    x = _startAndEndTouchPoint[0].x;
                    y = _startAndEndTouchPoint[0].y;
                }
                _reusableRect.set(x - (circleWidth / 2), y - (circleHeight / 2), x + (circleWidth / 2), y + (circleHeight / 2));
                _elementsPaint.setColor(selectedCircle.getColor());
                canvas.drawRect(_reusableRect, _elementsPaint);
                _elementsPaint.setTextSize(Tools.convertDpToPixel(32));
                canvas.drawText(selectedCircle.getId() + "", _viewWidth - _elementsPaint.measureText(selectedCircle.getId() + "") - Tools.convertDpToPixel(16.0f), Tools.convertDpToPixel(34.0f), _elementsPaint);
            }

            if (_currentGameMode == MODE_MULTIPLE) {
                x = _viewWidth / 6 * 5;
                y = _viewHeight / 2;

                ClassCircle circle2 = new ClassCircle(1, Color.parseColor("#99CC00"));
                ClassCircle circle = new ClassCircle(2, Color.parseColor("#FF4444"));

                int circleWidth = (_viewWidth * 1 / 3 - 10) * ((circle.getId()) * 100 / _currentGameDiskNumber) / 100;
                if (circleWidth < 2)
                    circleWidth = 2;

                circleHeight = Tools.convertDpToPixel(10.0f);
                circleHeight = _viewWidth / 3 / 10;

                _reusableRect.set(x - (circleWidth / 2), y - (circleHeight / 2), x + (circleWidth / 2), y + (circleHeight / 2));
                _elementsPaint.setColor(circle.getColor());
                canvas.drawRect(_reusableRect, _elementsPaint);

                circleWidth = (_viewWidth * 1 / 3 - 10) * ((circle2.getId()) * 100 / _currentGameDiskNumber) / 100;
                if (circleWidth < 2)
                    circleWidth = 2;

                _reusableRect.set(x - (circleWidth / 2), y - (circleHeight / 2) - circleHeight, x + (circleWidth / 2), y + (circleHeight / 2) - circleHeight);
                _elementsPaint.setColor(circle2.getColor());
                canvas.drawRect(_reusableRect, _elementsPaint);
            }

            if (_isBuildingQuickZone && (_qtEndEdgeBuilding == null && _qtStartEdgeBuilding == null)) {
                _elementsPaint.setColor(Color.BLACK);
                _elementsPaint.setAlpha(180);
                _reusableRect.set(0, 0, _viewWidth, _viewHeight);
                canvas.drawRect(_reusableRect, _elementsPaint);
            }

            if (_isBuildingQuickZone && _qtEndEdgeBuilding != null && _qtStartEdgeBuilding != null) {
                int xx, yy, w, h;
                xx = _qtStartEdgeBuilding.x < _qtEndEdgeBuilding.x ? _qtStartEdgeBuilding.x : _qtEndEdgeBuilding.x;
                yy = _qtStartEdgeBuilding.y < _qtEndEdgeBuilding.y ? _qtStartEdgeBuilding.y : _qtEndEdgeBuilding.y;

                w = Math.abs(_qtStartEdgeBuilding.x - _qtEndEdgeBuilding.x);
                h = Math.abs(_qtStartEdgeBuilding.y - _qtEndEdgeBuilding.y);

                _elementsPaint.setColor(Color.BLACK);
                _elementsPaint.setAlpha(180);

                canvas.drawRect(0, 0, _viewWidth, yy, _elementsPaint);
                canvas.drawRect(0, yy + h, _viewWidth, _viewHeight, _elementsPaint);
                canvas.drawRect(0, yy, xx, yy + h, _elementsPaint);
                canvas.drawRect(xx + w, yy, _viewWidth, yy + h, _elementsPaint);
            }

            if (_isBuildingQuickZone) {
                int imageSizeHalf = _qtBitmap.getWidth() / 2;
                canvas.drawBitmap(_qtBitmap, null, new Rect(_viewWidth / 2 - imageSizeHalf, _viewHeight / 2 - imageSizeHalf, _viewWidth / 2 + imageSizeHalf, _viewHeight / 2 + imageSizeHalf), _bitmapPaint);
            }

            if (_shouldDrawHelpLine) {
                Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

                mPaint.setColor(helpLineColor);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(Tools.convertDpToPixel(2.0f));
                mPaint.setPathEffect(new DashPathEffect(new float[]{Tools.convertDpToPixel(3), Tools.convertDpToPixel(1.4f)}, 0));
                mPaint.setAlpha(150);
                canvas.drawLine(_viewWidth - Tools.convertDpToPixel(160.0f) + Tools.convertDpToPixel(6), _viewHeight / 2 - Tools.convertDpToPixel(40.0f) + Tools.convertDpToPixel(6), _viewWidth - Tools.convertDpToPixel(16.0f) - Tools.convertDpToPixel(6), _viewHeight / 2 + Tools.convertDpToPixel(40.0f) - Tools.convertDpToPixel(6), mPaint);
                mPaint.setAlpha(255);
                canvas.drawCircle(_viewWidth - Tools.convertDpToPixel(160.0f), _viewHeight / 2 - Tools.convertDpToPixel(40.0f), Tools.convertDpToPixel(8), mPaint);
                canvas.drawCircle(_viewWidth - Tools.convertDpToPixel(16.0f), _viewHeight / 2 + Tools.convertDpToPixel(40.0f), Tools.convertDpToPixel(8), mPaint);
            }

            if (_currentGameField != null && _currentGameField.getQtCopy() != null) {
                int t, w, h, l;

                t = _currentGameField.getQtCopy().getTop();
                w = _currentGameField.getQtCopy().getWidth();
                h = _currentGameField.getQtCopy().getHeight();
                l = _currentGameField.getQtCopy().getLeft();

                _elementsPaint.setColor(Color.WHITE);
                _elementsPaint.setAlpha(240);

                canvas.drawRect(l, t, l + w, t + h, _elementsPaint);

                _elementsPaint.setAlpha(255);

                if (_isQtEditMode) {
                    int color = helpLineColor;
                    _elementsPaint.setColor(color);
                    _elementsPaint.setAlpha(80);
                    canvas.drawRect(l, t, l + w, t + h, _elementsPaint);
                    _elementsPaint.setAlpha(255);

                    int imageSize = Tools.convertDpToPixel(24);
                    int imageSizeHalf = imageSize / 2;
                    canvas.drawBitmap(_moveBitmap, null, new Rect(l + w / 2 - imageSizeHalf, t + h / 2 - imageSizeHalf, l + w / 2 + imageSizeHalf, t + h / 2 + imageSizeHalf), _bitmapPaint);
                } else {
                    if (_currentTouchIsInquickTouchZone && _startAndEndTouchPoint != null && _startAndEndTouchPoint[0] != null) {
                        if (_startAndEndTouchPoint[1] != null) {
                            x = _startAndEndTouchPoint[1].x;
                            y = _startAndEndTouchPoint[1].y;
                        } else {
                            x = _startAndEndTouchPoint[0].x;
                            y = _startAndEndTouchPoint[0].y;
                        }

                        if (x < _viewWidth / 3) {
                            _reusableRect.set(l, t, l + (w / 3), t + h);
                        } else if (x < _viewWidth / 3 + _viewWidth / 3) {
                            _reusableRect.set(l + (w / 3), t, l + (w / 3) + (w / 3), t + h);
                        } else {
                            _reusableRect.set(l + (w / 3) + (w / 3), t, l + (w / 3) + (w / 3) + (w / 3), t + h);
                        }
                        if (_startAndEndTouchPoint != null && _startAndEndTouchPoint[0] != null && selectedCircle.getId() != -1) {
                            _elementsPaint.setColor(selectedCircle.getColor());

                            _elementsPaint.setAlpha(150);
                            canvas.drawRect(_reusableRect, _elementsPaint);
                            _elementsPaint.setAlpha(255);
                        }
                    }
                    if (_startAndEndTouchPoint != null && _startAndEndTouchPoint[0] != null && selectedCircle.getId() != -1 && _currentTouchIsInquickTouchZone) {
                        _elementsPaint.setColor(selectedCircle.getColor());
                    } else {
                        _elementsPaint.setColor(Color.parseColor("#AAAAAA"));
                    }

                    canvas.drawLine(l + w / 3, t, l + w / 3, t + h, _elementsPaint);
                    canvas.drawLine(l + w * 2 / 3, t, l + w * 2 / 3, t + h, _elementsPaint);

                    _elementsPaint.setStyle(Paint.Style.STROKE);
                    canvas.drawRect(l, t, l + w, t + h, _elementsPaint);
                    _elementsPaint.setStyle(Paint.Style.FILL);
                }

            }
        }
    }

    private int getCircleWidth(ClassCircle circle, int _viewWidth, int _currentGameDiskNumber)
    {
        int maxSize = (_viewWidth / 3 - 10);
        int minSize = Tools.convertDpToPixel(12f);
        float delta = (maxSize - minSize) / _currentGameDiskNumber;

        return (int)(minSize + circle.getId() * delta);
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
/*
        if (_currentGameField != null && _currentGameField.getQtCopy() != null) {
            QuickTouch resizedQt = _currentGameField.getQtCopy();

            float heightRatio = (float) height / (float) _viewHeight;
            float widthRatio = (float) width / (float) _viewWidth;

            if (heightRatio != 1.0f) {
                if (resizedQt.getWidth() > width) {
                    resizedQt.setWidth((int) (width * widthRatio));
                }

                if (resizedQt.getHeight() > height) {
                    resizedQt.setHeight((int) (height * heightRatio));
                }

                float percentLeft = (float) resizedQt.getLeft() / _viewWidth;
                float percentRight = (float) (_viewWidth - (resizedQt.getLeft() + resizedQt.getWidth())) / _viewWidth;

                if (percentLeft < percentRight) {
                    resizedQt.setLeft((int) (width * percentLeft));
                } else {
                    resizedQt.setLeft((int) (width - resizedQt.getWidth() - width * percentRight));
                }

                float percentTop = (float) resizedQt.getTop() / _viewHeight;
                float percentBottom = (float) (_viewHeight - (resizedQt.getTop() + resizedQt.getHeight())) / _viewHeight;

                if (percentTop < percentBottom) {
                    resizedQt.setTop((int) (height * percentTop));
                } else {
                    resizedQt.setTop((int) (height - resizedQt.getHeight() - height * percentBottom));
                }

                if (resizedQt.getLeft() + resizedQt.getWidth() > width) {
                    resizedQt.setLeft(width - resizedQt.getWidth() - Tools.convertDpToPixel(8));
                }

                if (resizedQt.getTop() + resizedQt.getHeight() > height) {
                    resizedQt.setTop(height - resizedQt.getHeight() - Tools.convertDpToPixel(8));
                }

                this.setQt(resizedQt);
            }
        }
*/
        _viewHeight = height;
        _viewWidth = width;

        setMeasuredDimension(width, height);
    }

    public void setDemoMode(int mode) {
        createNewGame(5);
        _currentGameMode = mode;

        if (mode == MODE_SIZE) {
            _currentGameField.getTowers().get(0).getCircles().remove(4);
            _currentGameField.getTowers().get(0).getCircles().remove(3);

            ClassCircle circle = new ClassCircle(1, Color.parseColor("#99CC00"));
            _currentGameField.getTowers().get(1).getCircles().add(circle);
            circle = new ClassCircle(2, Color.parseColor("#FF4444"));
            _currentGameField.getTowers().get(1).getCircles().add(circle);
        } else if (mode == MODE_GOAL) {
            for (int i = 0; i < _currentGameField.getTowers().get(0).getCircles().size(); i++) {
                _currentGameField.getTowers().get(2).getCircles().add(_currentGameField.getTowers().get(0).getCircles().get(i));
            }
        } else if (mode == MODE_MULTIPLE) {
            _currentGameField.getTowers().get(0).getCircles().remove(4);
            _currentGameField.getTowers().get(0).getCircles().remove(3);
        }

        invalidate();
    }

    @Override
    public void setBackgroundColor(int color) {
        _backgroundColor = color;
        super.setBackgroundColor(color);
    }

    public void activateQuickTouchMode() {
        if (_isBuildingQuickZone || _currentGameField.getQtCopy() != null) {
            _currentGameField.setQt(null);
            _isBuildingQuickZone = false;
        } else {
            _isBuildingQuickZone = true;
            _qtBitmap = getBitmapFromVectorDrawable(getContext(), R.drawable.ic_action_smalltouch);
        }
        this.invalidate();
    }

    public boolean getDrawHelpLine() {
        return _shouldDrawHelpLine;
    }

    public void setDrawHelpLine(boolean drawHelpLine) {
        this._shouldDrawHelpLine = drawHelpLine;
        invalidate();
    }

    public TurnListener getTurnListener() {
        return _turnListener;
    }

    public void setTurnListener(TurnListener turnListener) {
        this._turnListener = turnListener;
    }

    public void setDisabled(boolean disabled) {
        this._isTouchDisabled = disabled;
    }

    public HelpListener getHelpListener() {
        return _helpListener;
    }

    public void setHelpListener(HelpListener helpListener) {
        this._helpListener = helpListener;
    }

    public QuickTouch getQt() {
        if (_currentGameField != null)
            return _currentGameField.getQtCopy();
        else
            return null;
    }

    public void setQt(QuickTouch qt) {
        if (_currentGameField != null)
            _currentGameField.setQt(qt);
    }

    public boolean isJustStarted() {
        return _currentGameField.getTowers().get(0).getCircles().size() == _currentGameDiskNumber;
    }

    public boolean isFinished() {
        return _currentGameField.getTowers().get(1).getCircles().size() == _currentGameDiskNumber || _currentGameField.getTowers().get(2).getCircles().size() == _currentGameDiskNumber;
    }

    public QuickTouchListener getQuickTouchListener() {
        return _quickTouchListener;
    }

    public void setQuickTouchListener(QuickTouchListener _quickTouchListener) {
        this._quickTouchListener = _quickTouchListener;
    }

    public void enterEditMode() {
        _isQtEditMode = true;
        _isMovingQuickTouch = false;

        _moveBitmap = getBitmapFromVectorDrawable(getContext(), R.drawable.ic_action_drag);

        invalidate();
    }

    public void undo() {
        if (lastMovedEndTower != -1 && lastMovedStartTower != -1) {
            _currentGameMovesCount -= 2;
            moveOneCircle(lastMovedEndTower, lastMovedStartTower);

            lastMovedEndTower = -1;
            lastMovedStartTower = -1;
            invalidate();
        }
    }

    public boolean canUndo() {
        return this.lastMovedEndTower != -1 && lastMovedStartTower != -1 && _currentGameMovesCount > 0 && !isFinished();
    }

    private Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;

    }

    public void setShowNumberedDisks(boolean showNumberedDisks) {
        this.showNumberedDisks = showNumberedDisks;
    }
}
