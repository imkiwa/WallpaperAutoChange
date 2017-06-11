package cc.kernel19.wallpaper.utils

import java.io.FileInputStream
import java.io.InputStream

/**
 * @author kiva
 */
object FileUtils {
    fun openInput(path: String?): InputStream? {
        return FileInputStream(path)
    }
}