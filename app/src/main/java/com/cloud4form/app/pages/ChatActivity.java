package com.cloud4form.app.pages;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.cloud4form.app.R;
import com.cloud4form.app.Util;
import com.cloud4form.app.other.GenericAsyncTask;
import com.cloud4form.app.other.UserProfileEntity;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONObject;

public class ChatActivity extends AppCompatActivity {

    static final String SINGLE_MESSAGE="SINGLE_MESSAGE";
    static final String TO="TO";
    static final String FROM="FROM";
    static final String TOKEN="TOKEN";
    static final String TYPE="TYPE";
    static final String MESSAGE="MESSAGE";

    private UserProfileEntity targetUser;
    private UserProfileEntity selfUser;
    private GoogleCloudMessaging gcm;
    private Util util;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_chat_view);

        String userData=getIntent().getStringExtra(UserProfileEntity.ARG_DATA);
        targetUser =UserProfileEntity.parse(userData);

        setTitle(targetUser.firstName);

        util=Util.getInstance(this);

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

        selfUser=new UserProfileEntity(util.getAsJSON(Util.PREE_USER_PROFILE));
        if(selfUser.chat_token.trim().length()==0){
            selfUser.chat_token=util.getPref(Util.PREE_GCM_TOKEN);
        }

    }

    private void sendMessage()throws Exception{

        String msg=editText.getText().toString();
        editText.setText("");


        JSONObject toSend=new JSONObject();
        toSend.put(TO, targetUser.id);
        toSend.put(FROM,selfUser.id);
        toSend.put(TYPE,SINGLE_MESSAGE);
        toSend.put(MESSAGE,msg);


        new GenericAsyncTask(util.generateURL("api_url", "onmessage"), new GenericAsyncTask.IAsyncCallback() {
            @Override
            public void onResult(JSONObject result) throws Exception {
                if(result!=null){

                }else{

                }
            }
        }).execute(toSend);
    }
}
