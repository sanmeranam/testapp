package com.cloud4form.app.pages;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cloud4form.app.R;
import com.cloud4form.app.Util;
import com.cloud4form.app.gms.MyGcmListenerService;
import com.cloud4form.app.other.ChatItemEntity;
import com.cloud4form.app.other.FormMetaEntity;
import com.cloud4form.app.other.GenericAsyncTask;
import com.cloud4form.app.other.RoundedImageView;
import com.cloud4form.app.other.UserProfileEntity;
import com.itextpdf.text.pdf.PRIndirectReference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class ChatProfileFragment extends Fragment {

    private Util util;
    private ProgressBar progressBar;
    private ListView listView;
    private FloatingActionButton reloadButton;
    private ArrayList<UserProfileEntity> userList = new ArrayList<>();
    private CardViewAdapter cardViewAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public ChatProfileFragment() {
        // Required empty public constructor
    }


    public static ChatProfileFragment newInstance() {
        ChatProfileFragment fragment = new ChatProfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        util=Util.getInstance(getActivity());

        cardViewAdapter=new CardViewAdapter(getActivity(),userList);
    }


    private void refreshUser(){
        this.progressBar.setVisibility(View.VISIBLE);
        new GenericAsyncTask(util.generateURL("api_url", "getallusers"), new GenericAsyncTask.IAsyncCallback() {
            @Override
            public void onResult(JSONObject result) throws Exception {
                if(result.has("success") && result.getInt("success")==1){
                    progressBar.setVisibility(View.GONE);

                    JSONObject data=result.getJSONObject("data");

                    JSONArray users=data.getJSONArray("USERS");

                    userList.clear();

                    for(int i=0;i<users.length();i++){
                        UserProfileEntity entity=new UserProfileEntity(users.getJSONObject(i));
                        if(!Util.CURRENT_USER.id.equals(entity.id)) {
                            userList.add(entity);
                        }
                    }

                    cardViewAdapter.notifyDataSetChanged();

                }
            }
        }).execute(new JSONObject());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.fragment_chat_profile, container, false);
        progressBar=(ProgressBar) rootView.findViewById(R.id.progressBar);

        try {
            RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
            mRecyclerView.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(cardViewAdapter);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        refreshUser();
        return rootView;
    }

    private class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.MyViewHolder>{

        private Context context;
        private ArrayList<UserProfileEntity> list;

        CardViewAdapter(Context context,ArrayList<UserProfileEntity> list){
            this.context=context;
            this.list=list;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_layout, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            UserProfileEntity entity=this.list.get(position);
            holder.title.setText(entity.firstName+" "+entity.lastName);
            holder.subtitle.setText(entity.email);
            holder.count.setText(entity.inbox.size()+"");
            if(entity.profile!=null){
                holder.user.setImageBitmap(entity.profile);
            }
            holder.rootView.setTag(entity);

        }

        @Override
        public int getItemCount() {
            return this.list.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView title;
            public TextView subtitle;
            public TextView count;
            public RoundedImageView user;
            public View rootView;

            public MyViewHolder(View view) {
                super(view);
                title = (TextView) view.findViewById(R.id.textProName);
                subtitle = (TextView) view.findViewById(R.id.textProSubtext);
                count = (TextView) view.findViewById(R.id.textCountInboxWeb);
                user = (RoundedImageView) view.findViewById(R.id.imageProfile);
                rootView=view;

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UserProfileEntity entity=(UserProfileEntity)v.getTag();
                        Intent dd=new Intent(getActivity(),ChatActivity.class);
                        dd.putExtra(UserProfileEntity.ARG_DATA,entity.toString());
                        getActivity().startActivity(dd);
                    }
                });
            }
        }
    }

    private void onMessageRecive(Bundle bundle){
        String from=bundle.getString("from1");
        String message=bundle.getString("message1");

        String event=bundle.getString("event");
        String action=bundle.getString("action");
        if(event.equals("USER_EVENT") && action.equals("USER_MESSAGE")) {
            for (int i = 0; i < userList.size(); i++) {
                UserProfileEntity entity = userList.get(i);

                if (entity.id.equals(from)) {

                    entity.inbox.add(new ChatItemEntity(from, message, System.currentTimeMillis()));

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cardViewAdapter.notifyDataSetChanged();
                        }
                    });

                    break;
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        util.attachListener1(new Util.iNotify() {
            @Override
            public void onMessage(Bundle bundle) {
                Log.d("ACTIVITY",bundle.getString("from"));
                onMessageRecive(bundle);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
       util.removeListener1();
    }

}
