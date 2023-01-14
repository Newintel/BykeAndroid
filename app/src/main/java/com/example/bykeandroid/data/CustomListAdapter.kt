package com.example.bykeandroid.data

import android.content.Context
import android.database.DataSetObserver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.TextView
import com.example.bykeandroid.R

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