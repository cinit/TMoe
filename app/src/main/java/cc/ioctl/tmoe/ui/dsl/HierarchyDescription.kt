package cc.ioctl.tmoe.ui.dsl

import android.content.Context
import android.view.View
import cc.ioctl.tmoe.hook.base.DynamicHook
import cc.ioctl.tmoe.ui.LocaleController
import cc.ioctl.tmoe.ui.dsl.item.DescriptionItem
import cc.ioctl.tmoe.ui.dsl.item.FunctionSwitch
import cc.ioctl.tmoe.ui.dsl.item.TextDetailItem
import cc.ioctl.tmoe.ui.dsl.item.TextValueItem

open class HierarchyDescription(
    val titleKey: String,
    val titleResId: Int,
    val showBackButton: Boolean = true,
    private val initializer: (HierarchyDescription.() -> Unit)? = null
) {
    private val dslItems = ArrayList<DslTMsgListItemInflatable>()
    private lateinit var listItems: ArrayList<TMsgListItem>
    private var isAfterBuild: Boolean = false;

    open fun category(
        titleKey: String,
        titleResId: Int,
        initializer: (Category.() -> Unit)? = null
    ): Category = Category(titleKey, titleResId, initializer).also {
        checkState()
        dslItems.add(it)
    }

    open fun functionSwitch(
        hook: DynamicHook,
        titleKey: String,
        titleResId: Int,
        descKey: String? = null,
        descResId: Int? = null,
        descProvider: ((Context) -> String?)? = null,
        onClick: View.OnClickListener? = null
    ): FunctionSwitch = FunctionSwitch(
        titleKey = titleKey,
        titleResId = titleResId,
        descKey = descKey,
        descResId = descResId,
        descProvider = descProvider,
        onClick = onClick,
        hook = hook
    ).also {
        checkState()
        dslItems.add(it)
    }

    open fun description(
        titleKey: String,
        titleResId: Int
    ) = DescriptionItem(titleKey, titleResId).also {
        checkState()
        dslItems.add(it)
    }

    open fun textDetail(
        titleKey: String,
        titleResId: Int,
        descProvider: ((Context) -> String?)? = null,
        onClick: View.OnClickListener? = null
    ) = TextDetailItem(
        title = LocaleController.getString(titleKey, titleResId),
        description = descProvider,
        onClick = onClick,
        multiLine = true
    ).also {
        checkState()
        dslItems.add(it)
    }

    open fun textValue(
        titleKey: String,
        titleResId: Int,
        valueProvider: ((Context) -> String?)? = null,
        onClick: View.OnClickListener? = null
    ) = TextValueItem(titleKey, titleResId, valueProvider, onClick).also {
        checkState()
        dslItems.add(it)
    }

    open fun textValue(
        titleKey: String,
        titleResId: Int,
        valueConstant: String,
        onClick: View.OnClickListener? = null
    ) = TextValueItem(titleKey, titleResId, { valueConstant }, onClick).also {
        checkState()
        dslItems.add(it)
    }

    open fun textValue(
        titleKey: String,
        titleResId: Int,
        valueKey: String,
        valueResId: Int,
        onClick: View.OnClickListener? = null
    ) = TextValueItem(titleKey, titleResId, {
        LocaleController.getString(valueKey, valueResId)
    }, onClick).also {
        checkState()
        dslItems.add(it)
    }

    open fun add(item: DslTMsgListItemInflatable) {
        checkState()
        dslItems.add(item)
    }

    open fun add(items: List<TMsgListItem>) {
        checkState()
        dslItems.addAll(items)
    }

    open fun collectItems(context: Context): List<TMsgListItem> {
        if (!::listItems.isInitialized) {
            initializer?.invoke(this)
            isAfterBuild = true
            listItems = ArrayList()
            dslItems.forEach {
                listItems.addAll(it.inflateTMsgListItems(context))
            }
        }
        return listItems.toMutableList()
    }

    private fun checkState() {
        if (isAfterBuild) {
            throw IllegalStateException("you can't add item after build")
        }
    }
}
