package com.michael.numbersensegame;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class CustomAdapter<T> extends ArrayAdapter<T> {

    T[] objects;
    int screenPixelWidth;
    float factor;

    public CustomAdapter(Context context, int textViewResourceId, T[] objects, int screenPixelWidth, float factor) {
        super(context, textViewResourceId, objects);
        this.objects = objects;
        this.screenPixelWidth = screenPixelWidth;
        this.factor = factor;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        super.getDropDownView(position, convertView, parent);

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(
                    R.layout.spinner_text_view_game_level, parent, false);
        }

        View view = super.getView(position, convertView, parent);
        TextView tv = (TextView) view.findViewById(R.id.gameLevelTextViewSpinner);
        tv.setText((CharSequence) objects[position]);
        tv.setTextSize(0, (screenPixelWidth * factor));
        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = super.getView(position, convertView, parent);
        TextView text = (TextView) view.findViewById(R.id.gameLevelTextViewSpinner);
        text.setTextSize(0, (screenPixelWidth * factor));

        return view;
    }

}
