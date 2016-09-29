package com.cloud4form.app.pages;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cloud4form.app.FormDetailsActivity;
import com.cloud4form.app.FormListView;
import com.cloud4form.app.NewFormActivity;
import com.cloud4form.app.R;
import com.cloud4form.app.AppController;

import com.cloud4form.app.db.FormMeta;
import com.cloud4form.app.db.IEntity;
import com.cloud4form.app.other.GenericAsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FormViewFragment extends Fragment {

    private ArrayList<FormMeta> formList;
    private AppController appController;
    private View mRootView;
    private CardViewAdapter cardViewAdapter;
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

        appController = AppController.getInstance(getActivity());
        formList=loadFormMetaFromDM();
        cardViewAdapter=new CardViewAdapter(getActivity(),formList);



        if(appController.getPref(AppController.PREE_APP_WORK_MODE).equals(appController.getPref(AppController.PREE_APP_WORK_MODE_ONLINE))){

        }
    }


    private ArrayList<FormMeta> loadFormMetaFromDM(){
        return appController.Filo.readArray(FormMeta.class);
    }

    private void loadFormMetaFromServer(){
        try {
            JSONObject dataToSend = new JSONObject();
            dataToSend.put("ENTRY_ID", appController.getPref(AppController.PREE_SYNC_TOKEN));

            new GenericAsyncTask(appController.generateURL("api_url", "from_path"), new GenericAsyncTask.IAsyncCallback() {
                @Override
                public void onResult(JSONObject result) throws Exception {
                    if(result!=null && result.has("success") && result.getInt("success")==1){
                        JSONArray data=result.getJSONArray("data");

                        HashMap<String,FormMeta> map=new HashMap<String, FormMeta>();
                        //Creating map of exist forms
                        for(FormMeta f:formList){
                            map.put(f.getServerId(),f);
                        }

                        boolean hasUpdate=false;
                        formList.clear();
                        //Comparing with old list
                        for(int i=0;i<data.length();i++){

                            FormMeta fNew=new FormMeta(data.getJSONObject(i));
                            formList.add(fNew);
                            //If already exist check version and update
//                            if(map.containsKey(fNew.getServerId())){
//                                FormMeta fOld=map.get(fNew.getServerId());
//
//                                if(fOld.getVersion()<fNew.getVersion()){
//                                    map.remove(fNew.getServerId());
//                                    map.put(fNew.getServerId(),fNew);
//                                    hasUpdate=true;
//                                }
//                            }else{//If not exist, just add it
//                                map.put(fNew.getServerId(),fNew);
                                hasUpdate=true;
//                            }
                        }

//                        ArrayList<FormMeta> m=new ArrayList<FormMeta>(map.values());
//                        formList.clear();
//                        for(FormMeta mm:m){
//                            formList.add(mm);
//                        }

                        if(FormViewFragment.this.cardViewAdapter!=null)
                            FormViewFragment.this.cardViewAdapter.notifyDataSetChanged();

                        if(hasUpdate){
                            InsertOrUpdate();
                        }
                    }
                }
            }).execute(dataToSend);

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void InsertOrUpdate(){
        appController.Filo.saveArray(formList,FormMeta.class);
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

            loadFormMetaFromServer();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return mRootView;
    }


    private class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.MyViewHolder>{

        private Context context;
        private List<FormMeta> list;

        CardViewAdapter(Context context,List<FormMeta> list){
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
            FormMeta entity=list.get(position);
            holder.title.setText(entity.getName());
            holder.version.setText("v"+entity.getVersion());
            holder.count.setText("0");

            holder.btnDetails.setTag(entity);
            holder.btnCreate.setTag(entity);

            holder.btnCreate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent newForm=new Intent(getActivity(), NewFormActivity.class);

                    newForm.putExtra(IEntity.ARG_DATA,(FormMeta)v.getTag());
                    newForm.putExtra(IEntity.ARG_MODE,IEntity.ARG_MODE_NEW);
                    getActivity().startActivity(newForm);
                }
            });
            holder.btnDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent newForm=new Intent(getActivity(), FormListView.class);
                    newForm.putExtra(IEntity.ARG_DATA,(FormMeta)v.getTag());
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
