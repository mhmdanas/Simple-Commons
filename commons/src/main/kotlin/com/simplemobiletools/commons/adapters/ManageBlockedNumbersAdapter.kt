package com.simplemobiletools.commons.adapters

import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.copyToClipboard
import com.simplemobiletools.commons.extensions.deleteBlockedNumber
import com.simplemobiletools.commons.interfaces.RefreshRecyclerViewListener
import com.simplemobiletools.commons.models.BlockedNumber
import com.simplemobiletools.commons.views.MyRecyclerView
import com.simplemobiletools.commons.views.bottomactionmenu.BottomActionMenuView
import kotlinx.android.synthetic.main.item_manage_blocked_number.view.*
import java.util.*

class ManageBlockedNumbersAdapter(
    activity: BaseSimpleActivity, var blockedNumbers: ArrayList<BlockedNumber>, val listener: RefreshRecyclerViewListener?,
    recyclerView: MyRecyclerView, itemClick: (Any) -> Unit
) : MyRecyclerViewAdapter(activity, recyclerView, itemClick) {
    init {
        setupDragListener(true)
    }

    override fun getActionMenuId() = R.menu.cab_blocked_numbers

    override fun onBottomActionMenuCreated(view: BottomActionMenuView) {
        view.toggleItemVisibility(R.id.cab_copy_number, isOneItemSelected())
    }

    override fun actionItemPressed(id: Int) {
        when (id) {
            R.id.cab_copy_number -> copyNumberToClipboard()
            R.id.cab_delete -> deleteSelection()
        }
    }

    override fun getSelectableItemCount() = blockedNumbers.size

    override fun getIsItemSelectable(position: Int) = true

    override fun getItemSelectionKey(position: Int) = blockedNumbers.getOrNull(position)?.id?.toInt()

    override fun getItemKeyPosition(key: Int) = blockedNumbers.indexOfFirst { it.id.toInt() == key }

    override fun onActionModeDestroyed() {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = createViewHolder(R.layout.item_manage_blocked_number, parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val blockedNumber = blockedNumbers[position]
        holder.bindView(blockedNumber, true, true) { itemView, adapterPosition ->
            setupView(itemView, blockedNumber)
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = blockedNumbers.size

    private fun getSelectedItems() = blockedNumbers.filter { selectedKeys.contains(it.id.toInt()) } as ArrayList<BlockedNumber>

    private fun setupView(view: View, blockedNumber: BlockedNumber) {
        view.apply {
            manage_blocked_number_holder?.isSelected = selectedKeys.contains(blockedNumber.id.toInt())
            manage_blocked_number_title.apply {
                text = blockedNumber.number
                setTextColor(textColor)
            }
        }
    }

    private fun copyNumberToClipboard() {
        val selectedNumber = getSelectedItems().firstOrNull() ?: return
        activity.copyToClipboard(selectedNumber.number)
        finishActMode()
    }

    private fun deleteSelection() {
        val deleteBlockedNumbers = ArrayList<BlockedNumber>(selectedKeys.size)
        val positions = getSelectedItemPositions()

        getSelectedItems().forEach {
            deleteBlockedNumbers.add(it)
            activity.deleteBlockedNumber(it.number)
        }

        blockedNumbers.removeAll(deleteBlockedNumbers)
        removeSelectedItems(positions)
        if (blockedNumbers.isEmpty()) {
            listener?.refreshItems()
        }
    }
}
