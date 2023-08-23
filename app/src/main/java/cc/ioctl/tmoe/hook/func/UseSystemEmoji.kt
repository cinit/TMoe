package cc.ioctl.tmoe.hook.func

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextPaint
import cc.ioctl.tmoe.base.annotation.FunctionHookEntry
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import com.github.kyuubiran.ezxhelper.utils.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*

@FunctionHookEntry
object UseSystemEmoji : CommonDynamicHook() {

    private val textPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    // https://github.com/DrKLO/Telegram/blob/master/TMessagesProj/src/main/java/org/telegram/messenger/Emoji.java#L292
    // https://github.com/PreviousAlone/Nnngram/blob/main/TMessagesProj/src/main/java/org/telegram/messenger/Emoji.java#L297
    override fun initOnce(): Boolean = tryOrLogFalse {

        val emoji = loadClass("org.telegram.messenger.Emoji")
        val fixEmojiM = emoji.method(
            "fixEmoji",
            String::class.java,
            isStatic = true,
            argTypes(String::class.java)
        )

        val simpleEmojiDrawable = loadClass("org.telegram.messenger.Emoji\$SimpleEmojiDrawable")
        val getDrawRectM = simpleEmojiDrawable.method("getDrawRect")
        val fullSizeF = simpleEmojiDrawable.field("fullSize")


        val drawableInfoF = simpleEmojiDrawable.field("info")
        val pageF = drawableInfoF.type.field("page")
        val emojiIndexF = drawableInfoF.type.field("emojiIndex")

        val emojiData = loadClass("org.telegram.messenger.EmojiData")
        val data1 = emojiData.field("data", isStatic = true).get(null) as Array<Array<String>>


        findMethod(simpleEmojiDrawable, false) {
            name == "draw" && parameterTypes.size == 1
        }.hookBefore {

            if (!isEnabled) return@hookBefore

            val canvas = it.args[0] as Canvas
            val emojiDrawable = it.thisObject as Drawable


            val fullSize = fullSizeF.get(it.thisObject) as Boolean


            val b: Rect
            if (fullSize) {
                b = getDrawRectM.invoke(emojiDrawable) as Rect
            } else {
                b = emojiDrawable.bounds
            }

            val textPaint = textPaint


            val info = drawableInfoF.get(emojiDrawable)
            val page = pageF.get(info) as Byte
            val emojiIndex = emojiIndexF.get(info) as Int


            val data: String =
                data1[page.toInt()][emojiIndex] //EmojiData.data[info.page][info.emojiIndex]

            val emoji1 = fixEmojiM.invoke(null, data) as String
            textPaint.textSize = b.height() * 0.8f;
            textPaint.typeface = getSystemEmojiTypeface();

            canvas.drawText(
                emoji1,
                0,
                emoji1.length,
                b.left + 0f,
                b.bottom - b.height() * 0.225f,
                textPaint
            );
            it.result = canvas;

        }
    }

    var loadSystemEmojiFailed = false
    private var systemEmojiTypeface: Typeface? = null
    private fun getSystemEmojiTypeface(): Typeface? {
        if (!loadSystemEmojiFailed && systemEmojiTypeface == null) {
            val font: File? = getSystemEmojiFontPath()
            if (font != null) {
                systemEmojiTypeface = Typeface.createFromFile(font)
            }
            if (systemEmojiTypeface == null) {
                loadSystemEmojiFailed = true
            }
        }
        return systemEmojiTypeface
    }

    private fun getSystemEmojiFontPath(): File? {
        val fileAOSP = File("/system/fonts/NotoColorEmoji.ttf")
        if (fileAOSP.exists()) {
            return fileAOSP
        }
        return null
    }

}