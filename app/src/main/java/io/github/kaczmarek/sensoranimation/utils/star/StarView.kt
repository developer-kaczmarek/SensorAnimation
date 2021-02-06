package io.github.kaczmarek.sensoranimation.utils.star

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import io.github.kaczmarek.sensoranimation.R
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

class StarView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.starViewStyle
) : View(context, attrs, defStyleAttr), TiltListener {

    private var waveGap =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, this.resources.displayMetrics)

    private var maxRadius = 0f

    private var initialRadius = 0f

    private var waveRadiusOffset = 0f
        set(value) {
            field = value
            postInvalidateOnAnimation()
        }

    private val starAccentColor = ContextCompat.getColor(context, R.color.StarViewPrimaryColor)

    private val gradientColors = intArrayOf(
            starAccentColor,
            modifyAlpha(starAccentColor, 0.50f),
            modifyAlpha(starAccentColor, 0.05f)
    )

    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    }
    private var center = PointF(0f, 0f)

    private val startPath = Path()

    private val gradientMatrix = Matrix()

    private val sensor = DeviceTiltSensor(context)

    private var starAnimator: ValueAnimator? = null

    init {
        context.withStyledAttributes(attrs, R.styleable.StarView) {
            val radius = getDimension(R.styleable.StarView_starCorners, 50f)
            with(starPaint) {
                color = starAccentColor
                strokeWidth = getDimension(R.styleable.StarView_starStrokeWidth, 1f)
                style = Paint.Style.STROKE
                setPathEffect(CornerPathEffect(radius))
            }
            waveGap = getDimension(R.styleable.StarView_starGap, 20f)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        starAnimator = ValueAnimator.ofFloat(0f, waveGap).apply {
            addUpdateListener {
                waveRadiusOffset = it.animatedValue as Float
            }
            duration = 1000L
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }

        sensor.addListener(this)
        sensor.register()
    }

    override fun onDetachedFromWindow() {
        starAnimator?.cancel()
        sensor.unregister()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var currentRadius = initialRadius + waveRadiusOffset
        while (currentRadius < maxRadius) {
            val path = createStarPath(currentRadius, startPath)
            canvas.drawPath(path, starPaint)
            currentRadius += waveGap
        }

        canvas.drawPaint(gradientPaint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        center.set(w / 2f, h / 2f)
        maxRadius = hypot(center.x.toDouble(), center.y.toDouble()).toFloat() * 1.3f
        initialRadius = w / waveGap

        gradientPaint.shader = RadialGradient(
                center.x,
                center.y,
                w / 2f,
                gradientColors,
                null,
                Shader.TileMode.CLAMP
        )
    }

    override fun onTilt(pitchRoll: Pair<Double, Double>) {
        val maxYOffset = center.y.toDouble()
        val maxXOffset = center.x.toDouble()

        val yOffset = (sin(pitchRoll.first) * maxYOffset)
        val xOffset = (sin(pitchRoll.second) * maxXOffset)

        updateGradient(xOffset.toFloat() + center.x, yOffset.toFloat() + center.y)
    }

    private fun createStarPath(
            radius: Float,
            path: Path = Path(),
            points: Int = 20
    ): Path {
        path.reset()
        val pointDelta = 0.7f
        val angleInRadians = 2.0 * Math.PI / points
        val startAngleInRadians = 0.0

        path.moveTo(
                center.x + (radius * pointDelta * cos(startAngleInRadians)).toFloat(),
                center.y + (radius * pointDelta * sin(startAngleInRadians)).toFloat()
        )

        for (i in 1 until points) {
            val hypotenuse = if (i % 2 == 0) pointDelta * radius else radius

            val nextPointX =
                    center.x + (hypotenuse * cos(startAngleInRadians - angleInRadians * i)).toFloat()
            val nextPointY =
                    center.y + (hypotenuse * sin(startAngleInRadians - angleInRadians * i)).toFloat()
            path.lineTo(nextPointX, nextPointY)
        }

        path.close()
        return path
    }

    private fun updateGradient(x: Float, y: Float) {
        gradientMatrix.setTranslate(x - center.x, y - center.y)
        gradientPaint.shader.setLocalMatrix(gradientMatrix)
        postInvalidateOnAnimation()
    }

    /**
     * Метод для изменения альфа-канала цвета. Данный метод используется при отрисовке
     * маркера пользователя при создании круга погрешности
     */
    private fun modifyAlpha(@ColorInt color: Int, factor: Float): Int {
        return Color.argb(
                (Color.alpha(color) * factor).roundToInt(),
                Color.red(color),
                Color.green(color),
                Color.blue(color)
        )
    }
}