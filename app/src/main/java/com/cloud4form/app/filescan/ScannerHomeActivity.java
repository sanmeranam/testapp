package com.cloud4form.app.filescan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;

import com.cloud4form.app.R;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ScannerHomeActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    public ArrayList<Bitmap> cropedList = new ArrayList<Bitmap>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        CrossAccess.homeActivity=this;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ScannerHomeActivity.this, ScannerCropActivity.class);
                startActivityForResult(intent, 10);
            }
        });

    }


    public void afterCropImage(Bitmap img){
        this.cropedList.add(img);
        this.mSectionsPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scanner_home, menu);
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
            String filePath;
            if((filePath=createPDFFile())!=null){
                Intent data=new Intent();
                data.putExtra("data",filePath);
                setResult(RESULT_OK,data);
                finish();
            }else{
                setResult(RESULT_CANCELED);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String createPDFFile() {

        if (!cropedList.isEmpty()) {
            int maxWidth = 0, maxHeight = 0;
            for (Bitmap b : cropedList) {
                maxWidth = Math.max(b.getWidth(), maxWidth);
                maxHeight = Math.max(b.getHeight(), maxHeight);
            }
            try {
                File f = createPdfFilePath();
                ByteArrayOutputStream stream;
                com.itextpdf.text.Rectangle pageSize = new com.itextpdf.text.Rectangle(maxWidth + 20, maxHeight + 20);

                Document document = new Document(pageSize, 10f, 10f, 10f, 10f);
                PdfWriter.getInstance(document, new FileOutputStream(f));
                document.open();
                for (Bitmap b : cropedList) {
                    stream = new ByteArrayOutputStream();
                    b.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    Image image = Image.getInstance(stream.toByteArray());
                    document.add(image);
                    stream.close();
                }
                document.close();
                return f.getAbsolutePath();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    private File createPdfFilePath() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "SCAN_" + timeStamp + "_";
        File storageDir = Environment.getExternalStorageDirectory();
        File filePath = new File(storageDir.getAbsolutePath(), "c4f_files");
        if (!filePath.exists()) {
            filePath.mkdir();
        }
        File file = File.createTempFile(imageFileName, ".pdf", filePath);
        return file;
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

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(Bitmap sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putParcelable(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_scanner_home, container, false);
            ImageView textView = (ImageView) rootView.findViewById(R.id.imageView);
            textView.setImageBitmap((Bitmap)getArguments().getParcelable(ARG_SECTION_NUMBER));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(cropedList.get(position));
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return cropedList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Page "+(position+1);
        }
    }
}
