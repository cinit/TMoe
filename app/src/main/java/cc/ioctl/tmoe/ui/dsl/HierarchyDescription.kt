package cc.ioctl.tmoe.ui.dsl

import android.content.Context
import android.view.View
import cc.ioctl.tmoe.hook.base.DynamicHook

open class HierarchyDescription(
    val titleKey: String,
    val titleResId: Int,
    val showBackButton: Boolean = true,
    private val initializer: (HierarchyDescription.() -> Unit)? = null
) {
    private val dslItems = ArrayList<DslTMsgListItemInflatable>()
    private lateinit var listItems: ArrayList<TMsgListItem>

    open fun category(
        titleKey: String,
        titleResId: Int,
        initializer: (Category.() -> Unit)? = null
    ): Category = Category(titleKey, titleResId, initializer).also { dslItems.add(it) }

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

    open fun collectItems(context: Context): List<TMsgListItem> {
        if (!::listItems.isInitialized) {
            initializer?.invoke(this)
            listItems = ArrayList()
            dslItems.forEach {
                listItems.addAll(it.inflateTMsgListItems(context))
            }
        }
        return listItems.toMutableList()
    }
}
