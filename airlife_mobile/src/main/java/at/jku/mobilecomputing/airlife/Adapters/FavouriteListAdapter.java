package at.jku.mobilecomputing.airlife.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import at.jku.mobilecomputing.airlife.R;


public class FavouriteListAdapter extends RecyclerView.Adapter<FavouriteListAdapter.ViewHolder> {


    private List<FavouriteListObject> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    Context ctx;


    // data is passed into the constructor
    public FavouriteListAdapter(Context context, List<FavouriteListObject> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        ctx=context;
    }


    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_item_favourite, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textview_Sno.setText(String.valueOf(mData.get(position).getSno()));
        holder.textview_LocationName.setText(mData.get(position).getLocation());
        holder.textview_LocationInfo.setText(mData.get(position).getLocationInfo());
        holder.textview_QualityValue.setText(mData.get(position).getQualityValue());

        int scale=mData.get(position).getQualityScale();
        if(scale==1)
        {
            holder.imageview_QualityScale.setBackground(ctx.getResources().getDrawable(R.drawable.ic_smile_good));
        }else if(scale==2)
        {
            holder.imageview_QualityScale.setBackground(ctx.getResources().getDrawable(R.drawable.ic_smile_sensitive_unhealthy));
        }else if(scale==3)
        {
            holder.imageview_QualityScale.setBackground(ctx.getResources().getDrawable(R.drawable.ic_smile_unhealthy));
        }else
        {
            holder.imageview_QualityScale.setBackground(ctx.getResources().getDrawable(R.drawable.ic_smile_good));
        }

    }

    private int calculateDistance() {
        return 0;
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textview_Sno;
        TextView textview_LocationName;
        TextView textview_LocationInfo;
        TextView textview_QualityValue;
        ImageView imageview_QualityScale;
        ImageView imageview_delete;

        ViewHolder(View itemView) {
            super(itemView);
            textview_Sno = itemView.findViewById(R.id.txtvw_sno);
            textview_LocationName = itemView.findViewById(R.id.txtvw_location);
            textview_LocationInfo = itemView.findViewById(R.id.txtvw_locationinfo);
            textview_QualityValue = itemView.findViewById(R.id.txtairquality);
            imageview_QualityScale = itemView.findViewById(R.id.imgvw_airqualityscale);
            imageview_delete = itemView.findViewById(R.id.imgvw_delete);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    FavouriteListObject getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
