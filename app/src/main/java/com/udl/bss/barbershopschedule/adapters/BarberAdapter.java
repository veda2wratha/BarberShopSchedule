package com.udl.bss.barbershopschedule.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.udl.bss.barbershopschedule.R;
import com.udl.bss.barbershopschedule.domain.Barber;
import com.udl.bss.barbershopschedule.listeners.OnItemClickListener;
import com.udl.bss.barbershopschedule.utils.BitmapUtils;

import java.util.Iterator;
import java.util.List;


public class BarberAdapter extends RecyclerView.Adapter<BarberAdapter.ViewHolder> {

    private List<Barber> mDataset;
    private OnItemClickListener listener;
    private Context context;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView name;
        TextView description;
        ImageView image;
        TextView address;
        ViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.card_view);
            name = itemView.findViewById(R.id.name_cv);
            description = itemView.findViewById(R.id.description_cv);
            image = itemView.findViewById(R.id.image_cv);
            address = itemView.findViewById(R.id.address_cv);
        }
    }

    public BarberAdapter(List<Barber> myDataset, OnItemClickListener listener, Context context) {
        mDataset = myDataset;
        this.listener = listener;
        this.context = context;
    }

    @Override
    public BarberAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.barber_card_view, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.description.setText(mDataset.get(position).getDescription());

        Bitmap bitmap = mDataset.get(position).getImagePath() == null ?
                BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_launcher) :
                BitmapUtils.loadImageFromStorage(mDataset.get(position).getImagePath(),
                        mDataset.get(position).getName());

        holder.image.setImageBitmap(bitmap);
        holder.name.setText(mDataset.get(position).getName());
        String address = mDataset.get(position).getAddress();
        holder.address.setText(address);

        ViewCompat.setTransitionName(holder.image, String.valueOf(position)+"_image");
        ViewCompat.setTransitionName(holder.description, String.valueOf(position)+"_desc");
        ViewCompat.setTransitionName(holder.name, String.valueOf(position)+"_name");
        ViewCompat.setTransitionName(holder.address, String.valueOf(position)+"_addr");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(v, holder.getAdapterPosition());
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public Barber getItem (int position) {
        return mDataset.get(position);
    }

    public void removeAll(){
        Iterator<Barber> iter = mDataset.iterator();
        while(iter.hasNext()){
            Barber barber = iter.next();
            int position = mDataset.indexOf(barber);
            iter.remove();
            notifyItemRemoved(position);
        }
    }

    public int add(Barber barber){
        mDataset.add(barber);
        notifyItemInserted(mDataset.size()-1);
        return mDataset.size()-1;
    }
}