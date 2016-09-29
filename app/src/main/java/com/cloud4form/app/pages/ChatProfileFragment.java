package com.cloud4form.app.pages;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cloud4form.app.R;
import com.cloud4form.app.AppController;
import com.cloud4form.app.db.User;
import com.cloud4form.app.db.IEntity;
import com.cloud4form.app.other.GenericAsyncTask;
import com.cloud4form.app.other.RoundedImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class ChatProfileFragment extends Fragment {

    private AppController appController;
    private ProgressBar progressBar;
    private FloatingActionButton reloadButton;
    private ArrayList<User> userList;
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

        appController = AppController.getInstance(getActivity());

        userList=loadUsersFromDB();
        cardViewAdapter=new CardViewAdapter(getActivity(),userList);



        IntentFilter mStatusIntentFilter = new IntentFilter("BROADCAST_ACTION_MSG");
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(new BroadcastReceiver() {
                                                                              @Override
                                                                              public void onReceive(Context context, Intent intent) {
                                                                                  onMessageRecive(intent.getBundleExtra("data"));
                                                                              }
                                                                          },
                mStatusIntentFilter);
    }

    private ArrayList<User> loadUsersFromDB(){
        return appController.Filo.readArray(User.class);
    }

    private void saveToDb(){
        appController.Filo.saveArray(userList,User.class);
    }

    private void loadUsersFromServer(){
        this.progressBar.setVisibility(View.VISIBLE);
        new GenericAsyncTask(appController.generateURL("api_url", "getallusers"), new GenericAsyncTask.IAsyncCallback() {
            @Override
            public void onResult(JSONObject result) throws Exception {
                progressBar.setVisibility(View.GONE);

                if(result!=null && result.has("success") && result.getInt("success")==1){

                    JSONObject data=result.getJSONObject("data");

                    JSONArray users=data.getJSONArray("USERS");

                    userList.clear();

                    for(int i=0;i<users.length();i++){
                        User entity=new User(users.getJSONObject(i));
                        if(!AppController.CURRENT_USER.equals(entity)) {
                            userList.add(entity);
                        }
                    }

                    cardViewAdapter.notifyDataSetChanged();
                    saveToDb();

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

        loadUsersFromServer();
        return rootView;
    }

    private class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.MyViewHolder>{

        private Context context;
        private ArrayList<User> list;

        CardViewAdapter(Context context,ArrayList<User> list){
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
            User entity=this.list.get(position);
            holder.title.setText(entity.getFirstName()+" "+entity.getLastName());
            holder.subtitle.setText(entity.getEmail());

//            int len=entity.getUnreadCount();
//
//            if(len==0){
//                holder.count.setVisibility(View.GONE);
//            }else{
//                holder.count.setVisibility(View.VISIBLE);
//                holder.count.setText(len+"");
//            }
//
//            holder.count.setText(len==0?"":len+"");

            if(entity.getImage()!=null){
//                holder.user.setImageBitmap(entity.profile);
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
                        User entity=(User)v.getTag();
                        Intent dd=new Intent(getActivity(),ChatActivity.class);
                        dd.putExtra(IEntity.ARG_DATA,entity);
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
                User entity = userList.get(i);

                if (entity.getServerId().equals(from)) {

//                    entity.getInbox().add(new ChatItem(from,message));

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
    public void onPause() {
        super.onPause();
        AppController.isAppRunning=false;
    }

    @Override
    public void onResume() {
        super.onResume();
        AppController.isAppRunning=true;
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//
//        appController.attachListener1(new AppController.iNotify() {
//            @Override
//            public void onMessage(Bundle bundle) {
//                Log.d("ACTIVITY",bundle.getString("from"));
//                onMessageRecive(bundle);
//            }
//        });
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//       appController.removeListener1();
//    }

}
