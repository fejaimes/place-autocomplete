package com.example.placeautocomplete

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable

class PredictionAdapter(context: Context, resource: Int) :
    ArrayAdapter<Prediction>(context, resource), Filterable {

    private val data: MutableList<Prediction> = mutableListOf()

    fun setData(list: List<Prediction>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    override fun getCount() = data.size

    override fun getItem(position: Int) = data[position]

    override fun getFilter(): Filter = ListFilter()

    inner class ListFilter : Filter() {
        override fun performFiltering(p0: CharSequence?): FilterResults {
            val filterResult = FilterResults()
            filterResult.count = data.size
            filterResult.values = data
            return filterResult
        }

        override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
            notifyDataSetChanged()
        }
    }
}