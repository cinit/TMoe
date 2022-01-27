package cc.ioctl.tmoe.ui.dsl

import android.content.Context
import android.view.View
import cc.ioctl.tmoe.hook.base.DynamicHook
import cc.ioctl.tmoe.ui.LocaleController
import cc.ioctl.tmoe.ui.dsl.item.Header
import cc.ioctl.tmoe.ui.dsl.item.ShadowSection

open class Category(
    val titleKey: String,
    val titleResId: Int,
    private val initializer: (Category.() -> Unit)? = null
) : DslTMsgListItemInflatable {

    private val dslItems = ArrayList<DslTMsgListItemInflatable>()
    private lateinit var listItems: ArrayList<TMsgListItem>

    override fun inflateTMsgListItems(context: Context): List<TMsgListItem> {
        if (!::listItems.isInitialized) {
            initializer?.invoke(this)
            listItems = ArrayList()
            listItems.add(Header(LocaleController.getString(titleKey, titleResId)))
            dslItems.forEach {
                listItems.addAll(it.inflateTMsgListItems(context))
            }
            listItems.add(ShadowSection())
        }
        return listItems.toMutableList()
    }

    open fun functionSwitch(
        hook: DynamicHook,
        titleKey: String,
        titleResId: Int,
        descKey: String? = null,
        descResId: Int? = null,
        onClick: View.OnClickListener? = null
    ): FunctionSwitch = FunctionSwitch(
        titleKey = titleKey,
        titleResId = titleResId,
        descKey = descKey,
        descResId = descResId,
        onClick = onClick,
        hook = hook
    ).also {
        dslItems.add(it)
    }
}
