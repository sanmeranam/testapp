package com.cloud4form.app;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.content.Context;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.content.res.Resources.Theme;

import android.widget.TextView;

import com.cloud4form.app.db.FormData;
import com.cloud4form.app.db.FormMeta;
import com.cloud4form.app.db.IEntity;
import com.cloud4form.app.other.GenericAsyncTaskGet;
import com.cloud4form.app.pages.FormViewFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class FormListView extends AppCompatActivity {

    private String[] dropMenu=new String[]{"Sent","Inbox"};
    private int startIndex=0;
    private static FormMeta formMeta;
    private static AppController appController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_list_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        startIndex=getIntent().getIntExtra("page",0);
        formMeta=(FormMeta)getIntent().getSerializableExtra(IEntity.ARG_DATA);

        // Setup spinner
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(new MyAdapter(toolbar.getContext(),dropMenu));

        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // When the given dropdown item is selected, show its contents in the
                // container view.
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(dropMenu[position]))
                        .commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinner.setSelection(startIndex);
        appController=AppController.getInstance(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_form_list_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private static class MyAdapter extends ArrayAdapter<String> implements ThemedSpinnerAdapter {
        private final ThemedSpinnerAdapter.Helper mDropDownHelper;

        public MyAdapter(Context context, String[] objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
            mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                // Inflate the drop down using the helper's LayoutInflater
                LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            } else {
                view = convertView;
            }

            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(getItem(position));

            return view;
        }

        @Override
        public Theme getDropDownViewTheme() {
            return mDropDownHelper.getDropDownViewTheme();
        }

        @Override
        public void setDropDownViewTheme(Theme theme) {
            mDropDownHelper.setDropDownViewTheme(theme);
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        AppController controller;
        private CardViewAdapter cardViewAdapter;
        private RecyclerView.LayoutManager mLayoutManager;
        private ArrayList<FormData> formList=new ArrayList<>();


        public PlaceholderFragment() {
            controller = AppController.getInstance(getActivity());
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(String sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putString(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        private String currentAction = "";


        @Override
        public void onStart() {
            super.onStart();

        }

        private void getDataFromFile() {
            ArrayList<FormData> list=this.controller.Filo.readArray(FormData.class);
            formList.clear();

            for(FormData fd:list){
                if(fd.getMetaId().equals(formMeta.getServerId()))
                formList.add(fd);
            }

            this.cardViewAdapter.notifyDataSetChanged();
        }

        private void onResponseInboxData(JSONArray data){
            try{
                formList.clear();
                for(int i=0;i<data.length();i++){
                    FormData fNew=new FormData(data.getJSONObject(i));
                    formList.add(fNew);
                }
                this.cardViewAdapter.notifyDataSetChanged();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        private ArrayList<FormData> getIncomingData() {
            new GenericAsyncTaskGet(appController.generateURL("api_url", "inbox_form")+"?user_id="+AppController.CURRENT_USER.getServerId()+"&meta_id="+formMeta.getServerId()

                    , new GenericAsyncTaskGet.IAsyncCallback() {
                @Override
                public void onResult(JSONObject result) throws Exception {
                    if(result!=null && result.has("success") && result.getInt("success")==1){
                        JSONArray data=result.getJSONArray("data");
                        onResponseInboxData(data);
                    }
                }
            }).execute();


            ArrayList<FormData> list=new ArrayList<>();
            return list;
        }




        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View mRootView = inflater.inflate(R.layout.fragment_form_list_view, container, false);

            currentAction = getArguments().getString(ARG_SECTION_NUMBER);
            this.cardViewAdapter=new CardViewAdapter(getActivity(),formList);

            if (currentAction.equalsIgnoreCase("Sent")) {
                getDataFromFile();
            } else if (currentAction.equalsIgnoreCase("Inbox")) {
                getIncomingData();
            }

            try {
                RecyclerView mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.formDataList);
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
            private List<FormData> list;

            CardViewAdapter(Context context,List<FormData> list){
                this.context=context;
                this.list=list;
            }

            @Override
            public CardViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_form_list, parent, false);
                return new CardViewAdapter.MyViewHolder(itemView);
            }

            @Override
            public void onBindViewHolder(CardViewAdapter.MyViewHolder holder, int position) {
                FormData entity=list.get(position);

                holder.title.setText(entity.getFormName());
                holder.version.setText("v"+entity.getVersion());

                holder.date.setText(new SimpleDateFormat("dd-MM-yyyy hh:mm").format(entity.getCreateDate()));
                try {
                    holder.stage.setText("STAGE:   "+(entity.getStageHistory().length()+1));
                }catch (Exception ex){}


                holder.field1.setText("");
                holder.field2.setText("");
                holder.field3.setText("");
                try{
                    ArrayList<FormData.FieldEntity> list=new ArrayList<>(entity.getFields().values());
                    ArrayList<FormData.FieldEntity> listF=new ArrayList<>();
                    for(int i=0;i<list.size();i++){
                        if(list.get(i).type.toLowerCase().contains("input")){
                            listF.add(list.get(i));
                        }
                    }
                    if(listF.size()>0){
                        holder.field1.setText(listF.get(0).value);
                    }
                    if(listF.size()>1){
                        holder.field2.setText(listF.get(1).value);
                    }
                    if(listF.size()>2){
                        holder.field3.setText(listF.get(2).value);
                    }
                }catch (Exception ex){

                }

                switch (entity.SentStatus){
                    case ERROR:
                        holder.icon.setImageResource(R.drawable.ic_form_error);
                        break;
                    case NEW:
                        holder.icon.setImageResource(R.drawable.ic_form_new);
                        break;
                    case SENDING:
                        holder.icon.setImageResource(R.drawable.ic_form_uploading);
                        break;
                    case SENT:
                        holder.icon.setImageResource(R.drawable.ic_form_done);
                        break;
                    default:
                        holder.icon.setVisibility(View.INVISIBLE);
                }
                if (currentAction.equalsIgnoreCase("Inbox")) {
                    holder.openButton.setTag(entity);
                    holder.openButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FormData entity=(FormData)v.getTag();
                            Intent intent=new Intent(getActivity(),EditFormActivity.class);
                            intent.putExtra(IEntity.ARG_DATA,entity);
                            getActivity().startActivity(intent);
                        }
                    });
                }else{
                    holder.openButton.setVisibility(View.INVISIBLE);
                }


            }

            @Override
            public int getItemCount() {
                return list.size();
            }

            public class MyViewHolder extends RecyclerView.ViewHolder {

                public TextView title;
                public TextView version;
                public TextView stage;
                public TextView date;
                public TextView field1;
                public TextView field2;
                public TextView field3;
                public ImageView icon;
                public Button openButton;
                public MyViewHolder(View view) {
                    super(view);
                    title = (TextView) view.findViewById(R.id.txtFormName);
                    version = (TextView) view.findViewById(R.id.txtVersion);
                    date = (TextView) view.findViewById(R.id.txtFormDate);
                    stage = (TextView) view.findViewById(R.id.textView9);
                    field1 = (TextView) view.findViewById(R.id.field1);
                    field2 = (TextView) view.findViewById(R.id.field2);
                    field3 = (TextView) view.findViewById(R.id.field3);
                    icon=(ImageView)view.findViewById(R.id.imageStatus);
                    openButton=(Button)view.findViewById(R.id.buttonOpen);
                }
            }
        }
    }




}
