package com.example.projemanag.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.databinding.ItemMemberBinding
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants

open class MemberListItemsAdapter(private val context: Context,
                                  private var list: ArrayList<User>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding:ItemMemberBinding =
            ItemMemberBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {

            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.mipmap.ic_user_place_holder)
                .into(holder.binding.ivMemberImage)

            holder.binding.tvMemberName.text = model.name
            holder.binding.tvMemberEmail.text = model.email

            if (model.selected){
                holder.binding.ivSelectedMember.visibility = View.VISIBLE
            }else{
                holder.binding.ivSelectedMember.visibility = View.GONE
            }
            holder.itemView.setOnClickListener {
                if (onClickListener != null){
                    if (model.selected){
                        onClickListener!!.onClick(position,model,Constants.UN_SELECT)
                    }else{
                        onClickListener!!.onClick(position,model,Constants.SELECT)
                    }
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    private class MyViewHolder(val binding: ItemMemberBinding) : RecyclerView.ViewHolder(binding.root)

    interface OnClickListener{
        fun onClick(position: Int, user: User, action: String)
    }
}