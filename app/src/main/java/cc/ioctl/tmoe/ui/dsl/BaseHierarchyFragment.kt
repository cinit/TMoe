package cc.ioctl.tmoe.ui.dsl

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.ioctl.tmoe.base.BaseProxyFragment
import cc.ioctl.tmoe.ui.LocaleController
import cc.ioctl.tmoe.ui.Theme
import cc.ioctl.tmoe.ui.wrapper.component.ThemedRecyclerListView

abstract class BaseHierarchyFragment : BaseProxyFragment() {

    protected abstract val hierarchy: HierarchyDescription

    protected lateinit var adapter: RecyclerView.Adapter<*>
    protected lateinit var recyclerListView: ThemedRecyclerListView
    protected lateinit var typeList: Array<Class<*>>
    protected lateinit var itemList: Array<TMsgListItem>
    protected lateinit var itemTypeIds: Array<Int>
    protected lateinit var itemTypeDelegate: Array<TMsgListItem>
    protected lateinit var context: Context

    override fun onCreateView(context: Context): View {
        this.context = context
        val actionBar = actionBarWrapper
        if (actionBar != null && hierarchy.showBackButton) {
            actionBar.setTitle(LocaleController.getString(hierarchy.titleKey, hierarchy.titleResId))
            val backImgId = context.resources.getIdentifier(
                "ic_ab_back",
                "drawable", context.packageName
            )
            if (backImgId != 0) {
                actionBar.setBackButtonImage(backImgId)
                actionBar.setActionBarMenuOnItemClick(View.OnClickListener { _ -> finishFragment() })
            }
        }
        val rootView = FrameLayout(context)
        rootView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray))

        // inflate hierarchy recycler list view items, each item will have its own view holder type
        itemList = hierarchy.collectItems(context).toTypedArray()
        // group items by java class
        typeList = itemList.map { it.javaClass }.distinct().toTypedArray()
        // item id to type id mapping
        itemTypeIds = Array(itemList.size) {
            typeList.indexOf(itemList[it].javaClass)
        }
        // item type delegate is used to create view holder
        itemTypeDelegate = Array(typeList.size) {
            itemList[itemTypeIds.indexOf(it)]
        }

        // init view
        recyclerListView = ThemedRecyclerListView(context).apply {
            itemAnimator = null
            layoutAnimation = null
            layoutManager = object : LinearLayoutManager(context, VERTICAL, false) {
                override fun supportsPredictiveItemAnimations() = false
            }
            isVerticalScrollBarEnabled = false
        }
        // init adapter
        adapter = object : ThemedRecyclerListView.SelectionAdapter() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                val delegate = itemTypeDelegate[viewType]
                return delegate.createViewHolder(context, parent)
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val item = itemList[position]
                item.bindView(holder, position, context)
            }

            override fun isEnabled(holder: RecyclerView.ViewHolder) =
                itemList[holder.adapterPosition].isEnabled

            override fun getItemCount() = itemList.size

            override fun getItemViewType(position: Int) = itemTypeIds[position]
        }
        recyclerListView.adapter = adapter
        recyclerListView.setOnItemClickListener { v, position, x, y ->
            if (parentActivity == null) return@setOnItemClickListener
            val item = itemList[position]
            item.onItemClick(v, position, x, y)
        }

        rootView.addView(
            recyclerListView, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        fragmentView = rootView
        return rootView
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
        }
    }

}
