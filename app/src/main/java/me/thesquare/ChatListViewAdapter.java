package me.thesquare;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by dkroeske on 8/29/15.
 */
public class ChatListViewAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater mInflator;
    ArrayList mPersonArrayList;

    public ChatListViewAdapter(Context context, LayoutInflater layoutInflater, ArrayList<ChatItem> personArrayList)
    {
        mContext = context;
        mInflator = layoutInflater;
        mPersonArrayList = personArrayList;
    }

    @Override
    public int getCount() {
        int size = mPersonArrayList.size();
        Log.i("getCount()","=" + size);
        return size;
    }

    @Override
    public Object getItem(int position) {
        Log.i("getItem()","");
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

        viewHolder.name.setText(person.chatname);
        viewHolder.text.setText(person.chattext);

        return convertView;
    }


    private static class ViewHolder {
        public TextView text;
        public TextView name;

    }
}