package com.songyu.commondelivery

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ImagePickerAdapter(var context: Context , var list : ArrayList<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    var checkBox = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view = View.inflate(context, R.layout.image_picker_item , null)
        var viewHolder = ImagePickerViewHolder(view,this)
        return viewHolder
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var picture = list.get(position)
        Glide.with(context).load(picture).into((holder as ImagePickerViewHolder).image)

        if(checkBox==position){
//            (holder as ImagePickerViewHolder).check.setImageResource(R.drawable.image_check_image)
            holder.after_check.visibility = View.VISIBLE
        }else{
//            (holder as ImagePickerViewHolder).check.setImageResource(R.drawable.image_not_check_image)
            holder.after_check.visibility = View.INVISIBLE
        }

    }
    fun resetting(position: Int){
        notifyDataSetChanged()
    }
    class ImagePickerViewHolder(view : View,var adapter: ImagePickerAdapter) : RecyclerView.ViewHolder(view){
        var image : ImageView = view.findViewById(R.id.image_picker_item)
        var check : ImageView = view.findViewById(R.id.image_check_box_button)
        var after_check : ImageView = view.findViewById(R.id.image_check_box_button_check)
        var layout : RelativeLayout = view.findViewById(R.id.image_picker_layout)

        init{
            layout.setOnClickListener(ButtonClick())
//            check.setOnClickListener(ButtonClick())
        }
        inner class ButtonClick : View.OnClickListener{
            override fun onClick(v: View?) {
                when(v?.id){
                    R.id.image_picker_layout ->{
                        if(adapter.checkBox==-1){
                            adapter.checkBox = adapterPosition
                            adapter.resetting(adapterPosition)
                        }else{
                            var temp = adapter.checkBox
                            adapter.checkBox = adapterPosition
                            adapter.resetting(adapterPosition)
                        }
                    }
                }
            }
        }



    }
}