package com.cloud4form.app.pages;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cloud4form.app.FormDetailsActivity;
import com.cloud4form.app.NewFormActivity;
import com.cloud4form.app.R;
import com.cloud4form.app.Util;
import com.cloud4form.app.other.FormMetaEntity;
import com.cloud4form.app.other.GenericAsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class FormViewFragment extends Fragment {

    private ArrayList<FormMetaEntity> formList=new ArrayList<>();
    private Util util;
    private View mRootView;
    private CardViewAdapter cardViewAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public FormViewFragment() {
        // Required empty public constructor
    }


    public static FormViewFragment newInstance() {
        FormViewFragment fragment = new FormViewFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        util=Util.getInstance(getActivity());

        JSONArray jForms=util.getAsJSONArray(Util.PREE_APP_FORMS);
        try {


            for(int i=0;i<jForms.length();i++){
                formList.add(new FormMetaEntity(jForms.getJSONObject(i)));
            }

            cardViewAdapter=new CardViewAdapter(getActivity(),formList);



        }catch (Exception ex){ex.printStackTrace();}
    }

    @Override
    public void onStart() {
        super.onStart();

        if(util.getPref(Util.PREE_APP_WORK_MODE).equals(util.getPref(Util.PREE_APP_WORK_MODE_OFFLINE))){
            return;
        }

        updateRemoteList();
    }

    private void updateRemoteList(){
        try {
            JSONObject dataToSend = new JSONObject();
            dataToSend.put("ENTRY_ID", util.getPref(Util.PREE_SYNC_TOKEN));

            new GenericAsyncTask(util.generateURL("api_url", "from_path"), new GenericAsyncTask.IAsyncCallback() {
                @Override
                public void onResult(JSONObject result) throws Exception {
                    if(result.has("success") && result.getInt("success")==1){
                        JSONArray data=result.getJSONArray("data");
                        util.setPref(Util.PREE_APP_FORMS,data.toString());

                        formList.clear();
                        for(int i=0;i<data.length();i++){
                            formList.add(new FormMetaEntity(data.getJSONObject(i)));
                        }
                        FormViewFragment.this.cardViewAdapter.notifyDataSetChanged();
                    }
                }
            }).execute(dataToSend);

        }catch (Exception ex){

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        mRootView=inflater.inflate(R.layout.fragment_form_view, container, false);
        try {
            RecyclerView mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
            mRecyclerView.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(cardViewAdapter);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return mRootView;
    }


    private class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.MyViewHolder>{

        private Context context;
        private ArrayList<FormMetaEntity> list;

        CardViewAdapter(Context context,ArrayList<FormMetaEntity> list){
            this.context=context;
            this.list=list;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_form_card, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            FormMetaEntity entity=list.get(position);
            holder.title.setText(entity.formName);
            holder.version.setText("v"+entity.version);
            holder.count.setText("0");

            holder.btnDetails.setTag(entity);
            holder.btnCreate.setTag(entity);

            holder.btnCreate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent newForm=new Intent(getActivity(), NewFormActivity.class);
                    newForm.putExtra(FormMetaEntity.ARG_DATA,((FormMetaEntity)v.getTag()).toString());
                    newForm.putExtra(FormMetaEntity.ARG_MODE,FormMetaEntity.ARG_MODE_NEW);
                    getActivity().startActivity(newForm);
                }
            });
            holder.btnDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent newForm=new Intent(getActivity(), FormDetailsActivity.class);
                    newForm.putExtra(FormMetaEntity.ARG_DATA,((FormMetaEntity)v.getTag()).toString());
                    getActivity().startActivity(newForm);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView title;
            public TextView version;
            public TextView count;
            public Button btnCreate;
            public Button btnDetails;

            public MyViewHolder(View view) {
                super(view);
                title = (TextView) view.findViewById(R.id.textFormName);
                version = (TextView) view.findViewById(R.id.textFormVersion);
                count = (TextView) view.findViewById(R.id.textFormCount);
                btnCreate = (Button) view.findViewById(R.id.buttonCreate);
                btnDetails = (Button) view.findViewById(R.id.buttonDetails);
            }
        }
    }
}
