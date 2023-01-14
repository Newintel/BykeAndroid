package com.example.bykeandroid.data

import android.database.DataSetObserver
import android.widget.ListAdapter

abstract class CustomListAdapter<T : Any>(
    private val list: List<T>,
) : ListAdapter {
    override fun registerDataSetObserver(observer: DataSetObserver?) {
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
    }

    override fun getCount(): Int = list.size

    override fun getItem(position: Int): Any = list[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = false

    override fun getItemViewType(position: Int): Int = position

    override fun getViewTypeCount(): Int = list.size

    override fun isEmpty(): Boolean = list.isEmpty()

    override fun areAllItemsEnabled(): Boolean = false

    override fun isEnabled(position: Int): Boolean = true
}