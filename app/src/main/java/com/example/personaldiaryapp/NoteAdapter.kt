package com.example.personaldiaryapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteAdapter: RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    private var ntList: ArrayList<NoteModel> = ArrayList()
    private var onClickItem:((NoteModel) -> Unit)? = null
    private var onClickDeleteItem: ((NoteModel) -> Unit)? = null

    fun addItems(items:ArrayList<NoteModel>) {
        this.ntList = items
        notifyDataSetChanged()
    }

    fun setOnClickItem(callback: (NoteModel)->Unit){
        this.onClickItem = callback
    }

    fun setOnClickDeleteItem(callback: (NoteModel) -> Unit){
        this.onClickDeleteItem = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = NoteViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_items_nt, parent, false)
    )

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val nt = ntList[position]
        holder.bindView(nt)
        holder.itemView.setOnClickListener { onClickItem?.invoke(nt) }
        holder.btnDelete.setOnClickListener { onClickDeleteItem?.invoke(nt)}
    }

    override fun getItemCount(): Int {
        return ntList.size
    }

    class NoteViewHolder(var view: View): RecyclerView.ViewHolder(view){
        private var date = view.findViewById<TextView>(R.id.tvdate)
        var btnDelete = view.findViewById<Button>(R.id.btnDelete)


        fun bindView(nt:NoteModel){
            date.text = nt.date
        }
    }
}