package me.thesquare.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import me.thesquare.R;
import me.thesquare.models.ChatItem;

public class ChatListViewAdapter extends BaseAdapter {
    private static final String TAG = "ChatListAdapter";
    private Context mContext;
    private LayoutInflater mInflator;
    private ArrayList mPersonArrayList;

    public ChatListViewAdapter(Context context, LayoutInflater layoutInflater, ArrayList<ChatItem> personArrayList)
    {
        mContext = context;
        mInflator = layoutInflater;
        mPersonArrayList = personArrayList;
    }

    @Override
    public int getCount() {
        int size = mPersonArrayList.size();
        Log.d(TAG, "getCount()" + "=" + size);
        return size;
    }

    @Override
    public Object getItem(int position) {
        Log.d(TAG,"getItem()");
        return mPersonArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if(convertView == null) {

            convertView = mInflator.inflate(R.layout.chat_listviewitem, null);
            viewHolder = new ViewHolder();
            viewHolder.text = (TextView) convertView.findViewById(R.id.txtText);
            viewHolder.name = (TextView) convertView.findViewById(R.id.txtName);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ChatItem person = (ChatItem) mPersonArrayList.get(position);

        viewHolder.name.setText(person.getChatname());
        viewHolder.text.setText(person.getChattext());

        return convertView;
    }


    private static class ViewHolder {
        public TextView text;
        public TextView name;

    }
}