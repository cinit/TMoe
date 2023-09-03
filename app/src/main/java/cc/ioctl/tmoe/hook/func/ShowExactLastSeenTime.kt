package cc.ioctl.tmoe.hook.func

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import cc.ioctl.tmoe.base.annotation.FunctionHookEntry
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import cc.ioctl.tmoe.ui.LocaleController
import cc.ioctl.tmoe.util.Initiator
import cc.ioctl.tmoe.util.hookBeforeIfEnabled
import java.util.Locale

@FunctionHookEntry
object ShowExactLastSeenTime : CommonDynamicHook() {

    private val mFormatterTime = SimpleDateFormat("HH:mm:ss", Locale.ROOT)
    private val mFormatterDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)

    override fun initOnce(): Boolean {
        // org.telegram.messenger.LocaleController#formatDateOnline
        val kLocaleController = Initiator.loadClass("org.telegram.messenger.LocaleController")
        val formatDateOnline = kLocaleController.declaredMethods.single {
            it.name == "formatDateOnline" && it.parameterTypes.isNotEmpty() && it.parameterTypes[0] == Long::class.java
        }
        hookBeforeIfEnabled(formatDateOnline) {
            val timeMillis = (it.args[0] as Long) * 1000L
            val strLastSeenFormatted = LocaleController.getString("LastSeenFormatted")
            val strYesterdayAtFormatted = LocaleController.getString("YesterdayAtFormatted")
            val strLastSeenDateFormatted = LocaleController.getString("LastSeenDateFormatted")
            val hasLocError =
                strLastSeenFormatted.contains("LC_ERR") || strYesterdayAtFormatted.contains("LC_ERR") || strLastSeenDateFormatted.contains("LC_ERR")
            it.result = when {
                hasLocError -> mFormatterDateTime.format(timeMillis)  // sigh...
                isToday(timeMillis) -> strLastSeenFormatted.format(mFormatterTime.format(timeMillis))
                isYesterday(timeMillis) -> {
                    var shouldBeShorter = false
                    if (formatDateOnline.parameterTypes.size == 2) {
                        val madeShort = it.args[1] as? BooleanArray
                        if (madeShort != null) {
                            shouldBeShorter = true
                            madeShort[0] = true
                        }
                    }
                    if (shouldBeShorter) {
                        strYesterdayAtFormatted.format(mFormatterTime.format(timeMillis))
                    } else {
                        strLastSeenFormatted.format(strYesterdayAtFormatted.format(mFormatterTime.format(timeMillis)))
                    }
                }

                else -> strLastSeenDateFormatted.format(mFormatterDateTime.format(timeMillis))
            }
        }
        return true
    }

    private fun isToday(timeMillis: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeMillis
        val year = calendar.get(Calendar.YEAR)
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        calendar.timeInMillis = System.currentTimeMillis()
        return (year == calendar.get(Calendar.YEAR)) && (dayOfYear == calendar.get(Calendar.DAY_OF_YEAR))
    }

    private fun isYesterday(timeMillis: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeMillis
        val year = calendar.get(Calendar.YEAR)
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        calendar.timeInMillis = System.currentTimeMillis()
        return (year == calendar.get(Calendar.YEAR)) && (dayOfYear == calendar.get(Calendar.DAY_OF_YEAR) - 1)
    }

}
