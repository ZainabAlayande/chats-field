package chats.cash.chats_field.utils.camera

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import chats.cash.chats_field.R

class OverlayPosition(var x: Float, var y: Float, var r: Float)

class OverlayView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint: Paint = Paint()
    private var holePaint: Paint = Paint()
    private var bitmap: Bitmap? = null
    private var layer: Canvas? = null
    private var border: Paint = Paint()

    //position of hole
    var holePosition: OverlayPosition = OverlayPosition(0.0f, 0.0f, 0.0f)
        set(value) {
            field = value
            //redraw
            this.invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (bitmap == null) {
            configureBitmap()
        }


        //draw background
//        layer?.drawRect(0.0f, 0.0f, width.toFloat(), height.toFloat(), paint)
//        //draw hole
        layer?.drawCircle((width / 2).toFloat(), (height / 4).toFloat(), 450f, border)
        layer?.drawCircle((width / 2).toFloat(), (height / 4).toFloat(), 450f, holePaint.apply { alpha = 0 })
        //draw bitmap
        canvas.drawBitmap(bitmap!!, 0.0f, 0.0f, paint);
    }

    private fun configureBitmap() {
        //create bitmap and layer
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        layer = Canvas(bitmap!!)
    }

    init {
        //configure background color
        val backgroundAlpha = 0.8
        paint.color = ColorUtils.setAlphaComponent(context?.let {
            ContextCompat.getColor(
                it,
                R.color.overlay
            )
        }!!, (255 * backgroundAlpha).toInt())

        border.color = Color.parseColor("#FFFFFF")
        border.strokeWidth = 30F
        border.style = Paint.Style.STROKE
        border.isAntiAlias = true
        border.isDither = true

        //configure hole color & mode
        holePaint.color = ContextCompat.getColor(context, android.R.color.transparent)

        holePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
}