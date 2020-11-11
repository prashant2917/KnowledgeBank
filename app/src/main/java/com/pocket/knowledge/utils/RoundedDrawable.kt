package com.pocket.knowledge.utils

import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.Log
import android.widget.ImageView.ScaleType
import kotlin.math.max
import kotlin.math.min

class RoundedDrawable(bitmap: Bitmap) : Drawable() {
    private val mBounds = RectF()
    private val mDrawableRect = RectF()
    private val mBitmapRect = RectF()
    private val mBitmapShader: BitmapShader
    private val mBitmapPaint: Paint
    private val mBitmapWidth: Int = bitmap.width
    private val mBitmapHeight: Int = bitmap.height
    private val mBorderRect = RectF()
    private val mBorderPaint: Paint
    private val mShaderMatrix = Matrix()
    private var cornerRadius = 0f
    private var isOval = false
    private var borderWidth = 0f
    private var borderColors = ColorStateList.valueOf(DEFAULT_BORDER_COLOR)
    var scaleType = ScaleType.FIT_CENTER
        private set

    override fun isStateful(): Boolean {
        return borderColors.isStateful
    }

    override fun onStateChange(state: IntArray): Boolean {
        val newColor = borderColors.getColorForState(state, 0)
        return if (mBorderPaint.color != newColor) {
            mBorderPaint.color = newColor
            true
        } else {
            super.onStateChange(state)
        }
    }

    private fun updateShaderMatrix() {
        val scale: Float
        var dx: Float
        var dy: Float
        when (scaleType) {
            ScaleType.CENTER -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset(borderWidth / 2, borderWidth / 2)
                mShaderMatrix.set(null)
                mShaderMatrix.setTranslate(((mBorderRect.width() - mBitmapWidth) * 0.5f + 0.5f),
                        ((mBorderRect.height() - mBitmapHeight) * 0.5f + 0.5f))
            }
            ScaleType.CENTER_CROP -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset(borderWidth / 2, borderWidth / 2)
                mShaderMatrix.set(null)
                dx = 0f
                dy = 0f
                if (mBitmapWidth * mBorderRect.height() > mBorderRect.width() * mBitmapHeight) {
                    scale = mBorderRect.height() / mBitmapHeight.toFloat()
                    dx = (mBorderRect.width() - mBitmapWidth * scale) * 0.5f
                } else {
                    scale = mBorderRect.width() / mBitmapWidth.toFloat()
                    dy = (mBorderRect.height() - mBitmapHeight * scale) * 0.5f
                }
                mShaderMatrix.setScale(scale, scale)
                mShaderMatrix.postTranslate((dx + 0.5f).toInt() + borderWidth,
                        (dy + 0.5f).toInt() + borderWidth)
            }
            ScaleType.CENTER_INSIDE -> {
                mShaderMatrix.set(null)
                scale = if (mBitmapWidth <= mBounds.width() && mBitmapHeight <= mBounds.height()) {
                    1.0f
                } else {
                    min(mBounds.width() / mBitmapWidth.toFloat(),
                            mBounds.height() / mBitmapHeight.toFloat())
                }
                dx = ((mBounds.width() - mBitmapWidth * scale) * 0.5f + 0.5f)
                dy = ((mBounds.height() - mBitmapHeight * scale) * 0.5f + 0.5f)
                mShaderMatrix.setScale(scale, scale)
                mShaderMatrix.postTranslate(dx, dy)
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(borderWidth / 2, borderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
            ScaleType.FIT_CENTER -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.CENTER)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(borderWidth / 2, borderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
            ScaleType.FIT_END -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.END)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(borderWidth / 2, borderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
            ScaleType.FIT_START -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.START)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(borderWidth / 2, borderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
            ScaleType.FIT_XY -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset(borderWidth / 2, borderWidth / 2)
                mShaderMatrix.set(null)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
            else -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.CENTER)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(borderWidth / 2, borderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
        }
        mDrawableRect.set(mBorderRect)
        mBitmapShader.setLocalMatrix(mShaderMatrix)
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        mBounds.set(bounds)
        updateShaderMatrix()
    }

    override fun draw(canvas: Canvas) {
        if (isOval) {
            if (borderWidth > 0) {
                canvas.drawOval(mDrawableRect, mBitmapPaint)
                canvas.drawOval(mBorderRect, mBorderPaint)
            } else {
                canvas.drawOval(mDrawableRect, mBitmapPaint)
            }
        } else {
            if (borderWidth > 0) {
                canvas.drawRoundRect(mDrawableRect, max(cornerRadius, 0f),
                        max(cornerRadius, 0f), mBitmapPaint)
                canvas.drawRoundRect(mBorderRect, cornerRadius, cornerRadius, mBorderPaint)
            } else {
                canvas.drawRoundRect(mDrawableRect, cornerRadius, cornerRadius, mBitmapPaint)
            }
        }
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setAlpha(alpha: Int) {
        mBitmapPaint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mBitmapPaint.colorFilter = cf
        invalidateSelf()
    }

    override fun setDither(dither: Boolean) {
        mBitmapPaint.isDither = dither
        invalidateSelf()
    }

    override fun setFilterBitmap(filter: Boolean) {
        mBitmapPaint.isFilterBitmap = filter
        invalidateSelf()
    }

    override fun getIntrinsicWidth(): Int {
        return mBitmapWidth
    }

    override fun getIntrinsicHeight(): Int {
        return mBitmapHeight
    }

    fun setCornerRadius(radius: Float): RoundedDrawable {
        cornerRadius = radius
        return this
    }

    fun setBorderWidth(width: Int): RoundedDrawable {
        borderWidth = width.toFloat()
        mBorderPaint.strokeWidth = borderWidth
        return this
    }

    fun setBorderColors(colors: ColorStateList?): RoundedDrawable {
        borderColors = colors ?: ColorStateList.valueOf(0)
        mBorderPaint.color = borderColors.getColorForState(state, DEFAULT_BORDER_COLOR)
        return this
    }

    fun setOval(oval: Boolean): RoundedDrawable {
        isOval = oval
        return this
    }

    fun setScaleType(scaleType: ScaleType?): RoundedDrawable {
        var scaleType = scaleType
        if (scaleType == null) {
            scaleType = ScaleType.FIT_CENTER
        }
        if (this.scaleType != scaleType) {
            this.scaleType = scaleType
            updateShaderMatrix()
        }
        return this
    }

    companion object {
        private const val TAG = "RoundedDrawable"
        const val DEFAULT_BORDER_COLOR = Color.BLACK
        @JvmStatic
        fun fromBitmap(bitmap: Bitmap?): RoundedDrawable? {
            return bitmap?.let { RoundedDrawable(it) }
        }

        @JvmStatic
        fun fromDrawable(drawable: Drawable?): Drawable? {
            if (drawable != null) {
                if (drawable is RoundedDrawable) {
                    // just return if it's already a RoundedDrawable
                    return drawable
                } else if (drawable is LayerDrawable) {
                    val num = drawable.numberOfLayers

                    // loop through layers to and change to RoundedDrawables if possible
                    for (i in 0 until num) {
                        val d = drawable.getDrawable(i)
                        drawable.setDrawableByLayerId(drawable.getId(i), fromDrawable(d))
                    }
                    return drawable
                }

                // try to get a bitmap from the drawable and
                val bm = drawableToBitmap(drawable)
                if (bm != null) {
                    return RoundedDrawable(bm)
                } else {
                    Log.w(TAG, "Failed to create bitmap from drawable!")
                }
            }
            return drawable
        }

        private fun drawableToBitmap(drawable: Drawable): Bitmap? {
            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }
            var bitmap: Bitmap?
            val width = max(drawable.intrinsicWidth, 1)
            val height = max(drawable.intrinsicHeight, 1)
            try {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
            } catch (e: Exception) {
                e.printStackTrace()
                bitmap = null
            }
            return bitmap
        }
    }

    init {
        mBitmapRect[0f, 0f, mBitmapWidth.toFloat()] = mBitmapHeight.toFloat()
        mBitmapShader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        mBitmapShader.setLocalMatrix(mShaderMatrix)
        mBitmapPaint = Paint()
        mBitmapPaint.style = Paint.Style.FILL
        mBitmapPaint.isAntiAlias = true
        mBitmapPaint.shader = mBitmapShader
        mBorderPaint = Paint()
        mBorderPaint.style = Paint.Style.STROKE
        mBorderPaint.isAntiAlias = true
        mBorderPaint.color = borderColors.getColorForState(state, DEFAULT_BORDER_COLOR)
        mBorderPaint.strokeWidth = borderWidth
    }
}