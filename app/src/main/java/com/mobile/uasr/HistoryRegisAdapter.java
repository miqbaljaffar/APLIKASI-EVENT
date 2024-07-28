package com.mobile.uasr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class HistoryRegisAdapter extends BaseAdapter {
    private List<EventEntry> eventList;
    private LayoutInflater inflater;

    public HistoryRegisAdapter(Context context, List<EventEntry> eventList) {
        this.eventList = eventList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return eventList.size();
    }

    @Override
    public Object getItem(int position) {
        return eventList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_event, parent, false);
            holder = new ViewHolder();
            holder.qrCodeImageView = convertView.findViewById(R.id.qrCodeImageView);
            holder.eventTitle = convertView.findViewById(R.id.eventTitle);
            holder.registrationDateTime = convertView.findViewById(R.id.registrationDateTime);
            holder.usernameTextView = convertView.findViewById(R.id.usernameTextView); // Add this line
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        EventEntry event = (EventEntry) getItem(position);
        holder.eventTitle.setText(event.getTitle());
        holder.registrationDateTime.setText(event.getCurrentDateTime());
        holder.usernameTextView.setText(event.getUsername()); // Set the username

        // Rest of the code for setting QR code image
        String qrCodeImage = event.getQrCodeImage();
        if (qrCodeImage != null && !qrCodeImage.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(qrCodeImage, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.qrCodeImageView.setImageBitmap(decodedByte);
            } catch (IllegalArgumentException e) {
                Log.e("QRDecode", "Failed to decode QR code image: " + e.getMessage(), e);
                holder.qrCodeImageView.setImageBitmap(null);
            }
        } else {
            Log.d("QRData", "No QR code image data available");
            holder.qrCodeImageView.setImageBitmap(null);
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView qrCodeImageView;
        TextView eventTitle;
        TextView registrationDateTime;
        TextView usernameTextView; // Add this field
    }

}
