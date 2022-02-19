package com.example.personaldiaryapp;

import android.view.LayoutInflater
import android.view.View;
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView

class CheckboxAdapter: RecyclerView.Adapter<CheckboxAdapter.CheckboxViewHolder>()  {

    private var cbList: ArrayList<CheckboxModel> = ArrayList()
    private var onClickItem:((CheckboxModel) -> Unit)? = null
    private var onClickDeleteItem: ((CheckboxModel) -> Unit)? = null

    fun addItems(items:ArrayList<CheckboxModel>) {
        this.cbList = items
        notifyDataSetChanged()
    }

    fun setOnClickItem(callback: (CheckboxModel)->Unit){
        this.onClickItem = callback
    }

    fun setOnClickDeleteItem(callback: (CheckboxModel) -> Unit){
        this.onClickDeleteItem = callback
    }

    fun getAllCheckboxes(): ArrayList<CheckboxModel>{
        return this.cbList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = CheckboxViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_items_cb, parent, false)
    )

    override fun onBindViewHolder(holder: CheckboxViewHolder, position: Int) {
        val cb = cbList[position]
        holder.bindView(cb)
        holder.itemView.setOnClickListener { onClickItem?.invoke(cb) }
        holder.btnDelete.setOnClickListener { onClickDeleteItem?.invoke(cb)}
    }

    override fun getItemCount(): Int {
        return cbList.size
    }

    class CheckboxViewHolder(var view:View): RecyclerView.ViewHolder(view){
        private var cbValue = view.findViewById<CheckBox>(R.id.cbValue)
        var edCheckbox = view.findViewById<EditText>(R.id.edCheckbox)
        var btnDelete = view.findViewById<Button>(R.id.btnDelete)


        fun bindView(cb:CheckboxModel){
            cbValue.isChecked = cb.value
            edCheckbox.setText(cb.text)
        }
    }
}
