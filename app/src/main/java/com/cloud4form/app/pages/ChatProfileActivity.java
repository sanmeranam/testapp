package com.cloud4form.app.pages;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cloud4form.app.R;
import com.cloud4form.app.AppController;
import com.cloud4form.app.db.ChatItem;
import com.cloud4form.app.db.IEntity;
import com.cloud4form.app.db.User;
import com.cloud4form.app.other.GenericAsyncTask;
import com.cloud4form.app.other.RoundedImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatProfileActivity extends AppCompatActivity {
    private AppController appController;
    private ProgressBar progressBar;
    private FloatingActionButton reloadButton;
    private ArrayList<User> userList;
    private CardViewAdapter cardViewAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private HashMap<String,Integer> mapUnread=new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_char_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               loadUsersFromServer();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar=(ProgressBar) findViewById(R.id.progressBar);

        appController = AppController.getInstance(this);

        userList=loadUsersFromDB();
        cardViewAdapter=new CardViewAdapter(this,userList);

        try {
            RecyclerView mRecyclerView = (RecyclerView)findViewById(R.id.recycler_view);
            mRecyclerView.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(cardViewAdapter);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        loadUsersFromServer();

        loadChatFromDB();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

    }

    private ArrayList<User> loadUsersFromDB(){
        return appController.Filo.readArray(User.class);
    }

    private void loadChatFromDB(){
        ArrayList<ChatItem> cList= appController.Filo.readArray(ChatItem.class);

        HashMap<String,User> uMap=new HashMap<>();
        for(User u:userList){
            uMap.put(u.getServerId(),u);
        }

        for(ChatItem c:cList){
            if(uMap.containsKey(c.getFrom()) && c.getTo().equals("SELF") && !c.isRead()){
                if(!mapUnread.containsKey(c.getFrom())){
                    mapUnread.put(c.getFrom(),0);
                }

                Integer count=mapUnread.get(c.getFrom());
                count++;
                mapUnread.put(c.getFrom(),count);
            }
        }
    }

    private void saveToDb(){
        appController.Filo.saveArray(userList,User.class);
    }

    private void loadUsersFromServer(){
//        this.progressBar.setVisibility(View.VISIBLE);
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

            if(!mapUnread.containsKey(entity.getServerId())){
                mapUnread.put(entity.getServerId(),0);
            }

            Integer iCount=mapUnread.get(entity.getServerId());

            if(iCount==0){
                holder.count.setVisibility(View.INVISIBLE);
            }else{
                holder.count.setVisibility(View.VISIBLE);
                holder.count.setText(iCount+"");
            }

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
                        mapUnread.remove(entity.getServerId());
                        Intent dd=new Intent(ChatProfileActivity.this,ChatActivity.class);
                        dd.putExtra(IEntity.ARG_DATA,entity);
                        startActivity(dd);
                        cardViewAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }

    private void onMessageRecive(Bundle bundle){
        String event=bundle.getString("event");
        String action=bundle.getString("action");

        if(event.equals("USER_EVENT") && action.equals("USER_MESSAGE")) {

            String from=bundle.getString("from1");
            String message=bundle.getString("message1");


            for (int i = 0; i < userList.size(); i++) {
                User entity = userList.get(i);

                if (entity.getServerId().equals(from)) {

                    if(!mapUnread.containsKey(entity.getServerId())){
                        mapUnread.put(entity.getServerId(),0);
                    }

                    Integer iCount=mapUnread.get(entity.getServerId());

                    iCount=iCount+1;

                    mapUnread.put(entity.getServerId(),iCount);

                    this.runOnUiThread(new Runnable() {
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
    protected void onResume() {
        super.onResume();
        loadChatFromDB();
    }

    @Override
    public void onStart() {
        super.onStart();

        appController.attachListener1(new AppController.iNotify() {
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
        appController.removeListener1();
    }

}
