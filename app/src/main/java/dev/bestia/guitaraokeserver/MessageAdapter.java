package dev.bestia.guitaraokeserver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MessageAdapter extends ArrayAdapter<Message> {

    private static class ViewHolder {
        TextView nameDateMsg;
    }

    public MessageAdapter(ArrayList<Message> data, Context context) {
        super(context, R.layout.message, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Message msg = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        //final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.message, parent, false);
            viewHolder.nameDateMsg = (TextView) convertView.findViewById(R.id.name_date_msg);
            //result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            //result=convertView;
        }

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        String showDate = sdf.format(msg.timestamp);
        viewHolder.nameDateMsg.setText(Html.fromHtml("<b>"+msg.username+"</b> <i>" + showDate+"</i> " +msg.data));
        // Return the completed view to render on screen
        return convertView;
    }

}
