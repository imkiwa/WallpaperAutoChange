package cc.kernel19.wallpaper.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * @author kiva
 */

class BitmapUtils {
    companion object {

        private fun createScaleBitmap(src: Bitmap, dstWidth: Int,
                                      dstHeight: Int): Bitmap {
            val dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false)
            if (src != dst) { // 如果没有缩放，那么不回收
                src.recycle() // 释放Bitmap的native像素数组
            }
            return dst
        }

        fun decodeSampledBitmapFromFileSystem(pathName: String, zoom: Int): Bitmap {
            val options = android.graphics.BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(pathName, options)

            val reqWidth = options.outWidth / zoom
            val reqHeight = options.outHeight / zoom

            options.inSampleSize = zoom
            options.inJustDecodeBounds = false
            val src = BitmapFactory.decodeFile(pathName, options)
            return Companion.createScaleBitmap(src, reqWidth, reqHeight)
        }
    }
}
