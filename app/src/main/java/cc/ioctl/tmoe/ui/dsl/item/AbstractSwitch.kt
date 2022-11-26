package cc.ioctl.tmoe.ui.dsl.item

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cc.ioctl.tmoe.ui.LocaleController
import cc.ioctl.tmoe.ui.dsl.DslTMsgListItemInflatable
import cc.ioctl.tmoe.ui.dsl.TMsgListItem
import cc.ioctl.tmoe.ui.wrapper.cell.TextCheckCell

class AbstractSwitch(
    val titleKey: String,
    val titleResId: Int,
    private val isCheckedLambda: () -> Boolean,
    private val onCheckedChanged: (Boolean) -> Unit,
    private val descKey: String? = null,
    private val descResId: Int? = null,
    private val descProvider: ((Context) -> String?)? = null,
    private val enabledLambda: (() -> Boolean)?,
    private val extraOnClickListener: View.OnClickListener? = null
) : DslTMsgListItemInflatable, TMsgListItem {

    class ViewHolder(cell: TextCheckCell) : RecyclerView.ViewHolder(cell)

    override val isEnabled = true
    override val isVoidBackground = false

    override fun createViewHolder(context: Context, parent: ViewGroup) =
        ViewHolder(TextCheckCell(context))

    override fun bindView(viewHolder: RecyclerView.ViewHolder, position: Int, context: Context) {
        val cell = viewHolder.itemView as TextCheckCell
        val titleString = LocaleController.getString(titleKey, titleResId)
        val descString: String? = descProvider?.invoke(context) ?: descKey?.let {
            LocaleController.getString(it, descResId ?: 0)
        }
        if (descString != null) {
            cell.setTextAndValueAndCheck(titleString, descString, isCheckedLambda(), true, true)
        } else {
            cell.setTextAndCheck(titleString, isCheckedLambda(), true)
        }
        cell.drawLine = extraOnClickListener != null
    }

    override fun onItemClick(v: View, position: Int, x: Float, y: Float) {
        val cell = v as TextCheckCell
        if (extraOnClickListener == null || cell.isInSwitchRange(x.toInt())) {
            if (enabledLambda?.invoke() == false) return
            val checked = cell.toggle()
            onCheckedChanged(checked)
        } else {
            extraOnClickListener.onClick(v)
        }
        // update description if dynamic
        if (descProvider != null) {
            val descString = descProvider.invoke(v.context)
            val titleString = LocaleController.getString(titleKey, titleResId)
            cell.setTextAndValueAndCheck(titleString, descString, isCheckedLambda(), true, true)
        }
    }
}
