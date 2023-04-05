package com.monita.weatherapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.monita.weatherapp.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.SimpleTimeZone;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {
    private Context context;
    private ArrayList<WeatherItemsRVModel> weatherItemsRVModelArrayList;

    public WeatherAdapter(Context context, ArrayList<WeatherItemsRVModel> weatherItemsRVModelArrayList) {
        this.context = context;
        this.weatherItemsRVModelArrayList = weatherItemsRVModelArrayList;
    }

    @NonNull
    @Override
    public WeatherAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.weather_itemview, parent,false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull WeatherAdapter.ViewHolder holder, int position) {
      WeatherItemsRVModel model =weatherItemsRVModelArrayList.get(position);
      holder.tempTV.setText(model.getTemperature());
      holder.windTv.setText(model.getWindSpeed());
      holder.timeTv.setText(model.getTime());
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        SimpleDateFormat output = new SimpleDateFormat("hh:mm aa");
        try
        {
            Date t = input.parse(model.getTime());
            holder.timeTv.setText(output.format(t));
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return weatherItemsRVModelArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tempTV,timeTv,windTv;
        private ImageView conditionIV;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            windTv=itemView.findViewById(R.id.tv_windspeed);
            timeTv=itemView.findViewById(R.id.tv_time);
            tempTV=itemView.findViewById(R.id.tv_temp);
            conditionIV=itemView.findViewById(R.id.img_condition);
        }
    }
}
