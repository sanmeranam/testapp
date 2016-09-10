package com.cloud4form.app.pages;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cloud4form.app.R;
import com.cloud4form.app.AppController;
import com.cloud4form.app.db.User;


public class AccountViewFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private User profileEntity;

    public AccountViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AccountViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountViewFragment newInstance(String param1, String param2) {
        AccountViewFragment fragment = new AccountViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        if(mParam1.equals("SELF") && AppController.CURRENT_USER!=null){
            profileEntity= AppController.CURRENT_USER;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View RootView=inflater.inflate(R.layout.fragment_account_view, container, false);
        if(profileEntity!=null){
            ((TextView)RootView.findViewById(R.id.textViewName)).setText(profileEntity.getFirstName()+" "+profileEntity.getLastName());
            ((TextView)RootView.findViewById(R.id.textViewEmail)).setText(profileEntity.getEmail());
            ((TextView)RootView.findViewById(R.id.textViewGroup)).setText("misc");
            ((TextView)RootView.findViewById(R.id.textViewPhone)).setText("000");
            if(profileEntity.getImage()!=null){
//                ((RoundedImageView)RootView.findViewById(R.id.NavviewImage)).setImageBitmap(profileEntity.profile);
            }
        }
        return RootView;
    }




}
