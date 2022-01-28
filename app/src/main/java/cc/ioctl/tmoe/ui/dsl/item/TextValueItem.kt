package cc.ioctl.tmoe.ui.dsl.item

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cc.ioctl.tmoe.ui.LocaleController
import cc.ioctl.tmoe.ui.dsl.DslTMsgListItemInflatable
import cc.ioctl.tmoe.ui.dsl.TMsgListItem
import cc.ioctl.tmoe.ui.wrapper.cell.TextSettingsCell

class TextValueItem(
    private val titleKey: String,
    private val titleResId: Int,
    private val value: ((Context) -> String?)?,
    private val onClick: View.OnClickListener?
) : DslTMsgListItemInflatable, TMsgListItem {

    class ViewHolder(cell: TextSettingsCell) : RecyclerView.ViewHolder(cell)

    override val isEnabled = true
    override val isVoidBackground = false

    override fun createViewHolder(context: Context, parent: ViewGroup) =
        ViewHolder(TextSettingsCell(context))

    override fun bindView(viewHolder: RecyclerView.ViewHolder, position: Int, context: Context) {
        val cell = viewHolder.itemView as TextSettingsCell
        val titleStr = LocaleController.getString(titleKey, titleResId)
        val valueStr = value?.invoke(context)
        if (valueStr != null) {
            cell.setTextAndValue(titleStr, valueStr, true)
        } else {
            cell.setText(titleStr, true)
        }
    }

    override fun onItemClick(v: View, position: Int, x: Float, y: Float) {
        onClick?.onClick(v)
    }
}
