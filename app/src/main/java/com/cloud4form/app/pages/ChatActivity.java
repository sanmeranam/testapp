package com.cloud4form.app.pages;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.cloud4form.app.R;
import com.cloud4form.app.AppController;
import com.cloud4form.app.db.ChatItem;
import com.cloud4form.app.db.User;
import com.cloud4form.app.db.IEntity;
import com.cloud4form.app.other.GenericAsyncTask;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    static final String SINGLE_MESSAGE="SINGLE_MESSAGE";
    static final String TO="TO";
    static final String FROM="FROM";
    static final String TYPE="TYPE";
    static final String MESSAGE="MESSAGE";

    private User targetUser;
    private User selfUser;
    private GoogleCloudMessaging gcm;
    private AppController appController;
    private EditText editText;
    private ArrayList<ChatItem> chatItems=new ArrayList<>();

    private ListView listView;
    private CustomListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_chat_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        targetUser=(User) getIntent().getSerializableExtra(IEntity.ARG_DATA);
        toolbar.setTitle(targetUser.getFirstName()+" "+targetUser.getLastName());

        setSupportActionBar(toolbar);



        appController = AppController.getInstance(this);

        ImageButton sendButton=(ImageButton)findViewById(R.id.buttonSend);
        editText=(EditText)findViewById(R.id.textMessageInput);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendMessage();
                }catch (Exception ex){ex.printStackTrace();}
            }
        });

        gcm = GoogleCloudMessaging.getInstance(this);

        selfUser=new User(appController.getAsJSON(AppController.PREE_USER_PROFILE));

        loadChatFromDB();

        listView=(ListView)findViewById(R.id.listViewChatItems);
        adapter = new CustomListAdapter(this, chatItems);
        listView.setAdapter(adapter);

    }

    private void saveToDb(ArrayList<ChatItem> items){

    }

    private void loadChatFromDB(){
        ArrayList<ChatItem> masterItems= appController.Filo.readArray(ChatItem.class);
        boolean isChange=false;
        for(ChatItem c:masterItems){
            if(c.getFrom().equals(targetUser.getServerId()) || c.getTo().equals(targetUser.getServerId())){
                chatItems.add(c);
                c.setRead(true);
                isChange=true;
            }
        }
        if(isChange)
            appController.Filo.saveArray(masterItems,ChatItem.class);
    }


    private void sendMessage()throws Exception{

        String msg=editText.getText().toString();
        editText.setText("");

        ChatItem lastItem;
        if(!chatItems.isEmpty() &&(lastItem=chatItems.get(chatItems.size() - 1)).getType()== ChatItem.item_type.UP){
            lastItem.setMessage(lastItem.getMessage()+"\n"+msg);
            lastItem.setChanged(true);
        }else{
            ChatItem ci=new ChatItem(selfUser.getServerId(),msg);
            ci.setType(ChatItem.item_type.UP);
            ci.setTo(targetUser.getServerId());
            ci.setRead(true);
            chatItems.add(ci);
        }

        adapter.notifyDataSetChanged();
        scrollMyListViewToBottom();

        JSONObject toSend=new JSONObject();
        toSend.put(TO, targetUser.getServerId());
        toSend.put(FROM,selfUser.getServerId());
        toSend.put(TYPE,SINGLE_MESSAGE);
        toSend.put(MESSAGE,msg);


        new GenericAsyncTask(appController.generateURL("api_url", "onmessage"), new GenericAsyncTask.IAsyncCallback() {
            @Override
            public void onResult(JSONObject result) throws Exception {
                if(result!=null){

                }else{

                }
            }
        }).execute(toSend);
    }


    public class CustomListAdapter extends BaseAdapter{

        private Context context;
        private ArrayList<ChatItem> chats=new ArrayList<>();

        public CustomListAdapter(Context context, ArrayList<ChatItem> chats) {
            this.context = context;
            this.chats = chats;
        }

        @Override
        public int getCount() {
            return this.chats.size();
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            ChatItem ci=chats.get(position);
            return (ci.getType()== ChatItem.item_type.DOWN)?0:1;
        }

        @Override
        public Object getItem(int position) {
            return this.chats.get(position);
        }

        @Override
        public long getItemId(int position) {
            return Long.parseLong(chats.get(position).getMessageId());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ChatItem ci=chats.get(position);
            if(convertView==null){
                LayoutInflater inflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                int vType=getItemViewType(position);


                if(vType==0)
                    convertView=inflater.inflate(R.layout.item_talk_layout_from, null);
                else
                    convertView=inflater.inflate(R.layout.item_talk_layout_to, null);


            }

            TextView msg=(TextView) convertView.findViewById(R.id.textMsgText);
            TextView time=(TextView) convertView.findViewById(R.id.textMsgTime);

            msg.setText(ci.getMessage());
            time.setText(new SimpleDateFormat("hh:mm").format(ci.getCreateDate()));

            return convertView;
        }
    }

    private void onMessageRecive(Bundle bundle){
        String event=bundle.getString("event");
        String action=bundle.getString("action");

        if(event.equals("USER_EVENT") && action.equals("USER_MESSAGE")) {

            String from=bundle.getString("from1");
            String message=bundle.getString("message1");

            if(from.equals(targetUser.getServerId())){
                ChatItem chatItem=new ChatItem(from,message);
                chatItem.setType(ChatItem.item_type.DOWN);
                chatItem.setRead(true);
                chatItem.setTo(selfUser.getServerId());

                chatItems.add(chatItem);

                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        scrollMyListViewToBottom();
                    }
                });
            }
        }
    }

    private void scrollMyListViewToBottom() {
        listView.post(new Runnable() {
            @Override
            public void run() {
                listView.setSelection(adapter.getCount() - 1);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        appController.attachListener2(new AppController.iNotify() {
            @Override
            public void onMessage(Bundle bundle) {
                Log.d("ACTIVITY",bundle.getString("from"));
                onMessageRecive(bundle);
            }
        });

        scrollMyListViewToBottom();
    }

    @Override
    public void onPause() {
        super.onPause();
        appController.removeListener2();

        saveUpdatedToDb();

        saveToDb(chatItems);
    }

    private void saveUpdatedToDb(){
        ArrayList<ChatItem> masterList= appController.Filo.readArray(ChatItem.class);
        HashMap<String,ChatItem> map=new HashMap<>();

        for(ChatItem c:masterList){
            map.put(c.getMessageId(),c);
        }
        boolean ifChange=false;
        for(ChatItem c:chatItems){
            if(!map.containsKey(c.getMessageId())){
                masterList.add(c);
                ifChange=true;
            }else if(c.isChanged()){
                map.get(c.getMessageId()).setMessage(c.getMessage());
                ifChange=true;
            }
        }
        appController.Filo.saveArray(masterList,ChatItem.class);
    }
}
