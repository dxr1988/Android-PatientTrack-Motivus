package com.motivus.ece.motivus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener, SigninDialog.SigninDialogListener {
    /**
     * The refresh distance is set to 0 meter, which means the update does not depend on distance
     */
    private PreferenceChangeListener mPreferenceListener = null;
    private SharedPreferences mSharedPreferences;
    private LocationManager mLocationManager;
    private Database mDatabase;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private boolean doctor = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HelperFunctions.showNoticeDialog(this);

        //Get setting preference
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPreferenceListener = new PreferenceChangeListener();
        mSharedPreferences.registerOnSharedPreferenceChangeListener(mPreferenceListener);

        //Check GPS on or off
        mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            HelperFunctions.alertTurnOnGPS(this);
        }

        //Constantly monitoring the GPS location
        startService(new Intent(this, GPSlocationTracingService.class));
        //Constantly monitoring the photo usage
        startService(new Intent(this, PhoneUsageTracingService.class));

        //Set up the database
        mDatabase = Database.getInstance(this);

        //Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        //Set up the ViewPager with the sections adapter.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
                //mSectionsPagerAdapter.notifyDataSetChanged();
            }
            public void onPageScrollStateChanged(int state) {
            }
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        //Demo data
        Appointment[] demoAppointments = HelperFunctions.demoAppointments();
        for(int i = 0; i < demoAppointments.length; i++) {
            mDatabase.addAppointment(demoAppointments[i]);
            mDatabase.setMaxAppointmentID(demoAppointments[i].id);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(doctor == true) {
            if (id == R.id.action_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
        }
        else
            Toast.makeText(this, "You are not a doctor. No permission ", Toast.LENGTH_SHORT).show();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        int tabPosition = tab.getPosition();
        mViewPager.setCurrentItem(tabPosition);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
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
            switch(position) {
                case 0:
                    return AppointmentFragment.newInstance(position + 1);
                case 1:
                    return ReportFragment.newInstance(position + 1);
                //case 2:
                //    return GoogleCalendarFragment.newInstance(position + 1);
                default:
                    return AppointmentFragment.newInstance(position + 1);
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public int getItemPosition(Object object) {
            if(object instanceof  AppointmentFragment ) {
                AppointmentFragment f = (AppointmentFragment) object;
                if (f != null) {
                    f.update();
                }
            }
            return super.getItemPosition(object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section3).toUpperCase(l);
                //case 2:
                //    return getString(R.string.title_section2).toUpperCase(l);
            }
            return null;
        }
    }

    public static class AppointmentFragment extends ListFragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public List<Appointment> appointmentList = new ArrayList<Appointment>();
        public AppointmentAdapter mAdapter;
        private final int NewAppointmentIndex = 0;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static AppointmentFragment newInstance(int sectionNumber) {
            AppointmentFragment fragment = new AppointmentFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public AppointmentFragment() {
        }

        public void update() {
            appointmentList = Database.getInstance(getActivity()).getAllAppointments();
            Collections.sort(appointmentList);
            mAdapter = new AppointmentAdapter(getActivity(), appointmentList);
            setListAdapter(mAdapter);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_appointment, container, false);

            //Get all the appointment and put them into
            appointmentList = Database.getInstance(getActivity()).getAllAppointments();
            Collections.sort(appointmentList);
            mAdapter = new AppointmentAdapter(getActivity(), appointmentList);
            setListAdapter(mAdapter);

            FragmentManager fm = getFragmentManager();
            fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    //Update after delete or modify any appointment; even just return by back button
                    if(getFragmentManager().getBackStackEntryCount() == 0) {
                        update();
                    }
                }
            });

            //Add new appointment button
            Button newAppointment = (Button) rootView.findViewById(R.id.button_newappointment);
            newAppointment.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent newActivity = new Intent(v.getContext(), NewAppointment.class);
                            startActivityForResult(newActivity, NewAppointmentIndex);
                        }
                    }
            );

            return rootView;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            switch(requestCode) {
                case (NewAppointmentIndex) : {
                    if (resultCode == Activity.RESULT_OK) {
                        update();
                    }
                    break;
                }
            }
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            Appointment appointment = (Appointment)l.getItemAtPosition(position);
            DetailFragment detailFragment = new DetailFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("appointmentID", appointment.id);
            detailFragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.container, detailFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    public static class ReportFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static ReportFragment newInstance(int sectionNumber) {
            ReportFragment fragment = new ReportFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public ReportFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_report, container, false);

            //Map Tracking
            Button mapTrackButton = (Button) rootView.findViewById(R.id.button_tracking_map);
            mapTrackButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent map = new Intent(v.getContext(), TrackingMap.class);
                            startActivity(map);
                        }
                    }
            );

            //Accomplishment Tracking
            Button accomplishmentTrackButton = (Button) rootView.findViewById(R.id.button_tracking_accomplishment);
            accomplishmentTrackButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent barChat = new Intent(v.getContext(), BarChartActivity.class);
                            startActivity(barChat);
                        }
                    }
            );

            //Appointment Tracking
            Button appointmentTrackButton = (Button) rootView.findViewById(R.id.button_tracking_appointment);
            appointmentTrackButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent stackedBarChart = new Intent(v.getContext(), StackedBarActivity.class);
                            startActivity(stackedBarChart);
                        }
                    }
            );

            //Score Tracking
            Button scoreTrackButton = (Button) rootView.findViewById(R.id.button_scores);
            scoreTrackButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent combinedChart = new Intent(v.getContext(), CombinedChartActivity.class);
                            startActivity(combinedChart);
                        }
                    }
            );

            //GPS raw date
            Button gpsDataButton = (Button) rootView.findViewById(R.id.button_rawdata_gps);
            gpsDataButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            storeFile();
                        }
                    }
            );

            return rootView;
        }

        public boolean storeFile()
        {
            boolean created = false;
            String fileName = "GPSRawData.txt";
            ArrayList<GPSlocation> GPSlocations = Database.getInstance(getActivity()).getAllGPSs();
            //File content
            String fileContent = "";
            for(int i = 0;i < GPSlocations.size(); i++ ){
                GPSlocation gpsLocation = GPSlocations.get(i);
                fileContent = fileContent + gpsLocation.time + "#" + gpsLocation.latitude + "#" + gpsLocation.longitude  + "\n";
            }

            //Create File
            created = HelperFunctions.writeToExternalStoragePublic(fileName, fileContent);

            //Return back to start page
            if (created){
                Toast.makeText(getActivity(), "Saved " + fileName, Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getActivity(), "Failed to save " + fileName, Toast.LENGTH_SHORT).show();
            }

            return created;
        }
    }

    public static class GoogleCalendarFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private WebView webView;
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static GoogleCalendarFragment newInstance(int sectionNumber) {
            GoogleCalendarFragment fragment = new GoogleCalendarFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);

            return fragment;
        }

        public GoogleCalendarFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            //super.onCreate(savedInstanceState);
            View rootView = inflater.inflate(R.layout.fragment_cal, container, false);
            //see map
            Button mapTrackButton = (Button) rootView.findViewById(R.id.button_calendar);
            mapTrackButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent calendar = new Intent(v.getContext(), Calendar.class);
                            startActivity(calendar);
                        }
                    }
            );
            return rootView;
        }
    }

    public static class DetailFragment extends Fragment {
        public DetailFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            Bundle args = getArguments();
            int appointmentID = args.getInt("appointmentID");
            final Appointment appointment = Database.getInstance(getActivity()).getAppointment(appointmentID);
           
            final EditText editText_title = (EditText)(rootView.findViewById(R.id.editView_title));
            editText_title.setEnabled(false);
            editText_title.setText(appointment.title, EditText.BufferType.EDITABLE);

            final EditText editText_detail = (EditText)(rootView.findViewById(R.id.editView_detail));
            editText_detail.setEnabled(false);
            editText_detail.setText(appointment.detail, EditText.BufferType.EDITABLE);

            final EditText editText_latitude = (EditText)(rootView.findViewById(R.id.editView_latitude));
            editText_latitude.setEnabled(false);
            editText_latitude.setText("" + appointment.latitude, EditText.BufferType.EDITABLE);

            final EditText editText_longitude = (EditText)(rootView.findViewById(R.id.editView_longitude));
            editText_longitude.setEnabled(false);
            editText_longitude.setText("" + appointment.longitude, EditText.BufferType.EDITABLE);

            final EditText editText_time = (EditText)(rootView.findViewById(R.id.editView_time));
            editText_time.setEnabled(false);
            editText_time.setText("" + appointment.time, EditText.BufferType.EDITABLE);

            final EditText editText_date = (EditText)(rootView.findViewById(R.id.editView_date));
            editText_date.setText("" + appointment.date, EditText.BufferType.EDITABLE);
            editText_date.setEnabled(false);

            //Category
            ArrayAdapter<String> adapterCategory = new ArrayAdapter<String>(getActivity(),
                    R.layout.spinner_item, Database.AppointmentCategory.subList(1,Database.AppointmentCategory.size()));
            adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            final Spinner spinner_category = (Spinner)(rootView.findViewById(R.id.spinner_category));
            spinner_category.setAdapter(adapterCategory);
            spinner_category.setSelection(appointment.category);
            spinner_category.setEnabled(false);

            CheckBox checkBox_done = (CheckBox)(rootView.findViewById(R.id.checkBox_done));
            if(appointment.done == 1)
                checkBox_done.setChecked(true);
            /*
            ImageView imageView_pic = (ImageView)(rootView.findViewById(R.id.imageView_pic));
            byte[] imageByteArray = appointment.pic;
            ByteArrayInputStream imageStream = new ByteArrayInputStream(imageByteArray);
            Bitmap image = BitmapFactory.decodeStream(imageStream);
            imageView_pic.setImageBitmap(image);
            Button button_back = (Button) rootView.findViewById(R.id.button_back);
            button_back.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
            */
            editText_time.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    String appointmentDate = "" + editText_date.getText().toString() + ' ' + editText_time.getText().toString();

                    Calendar calendar = Calendar.getInstance();
                    try {
                        calendar.setTime(formatter.parse(appointmentDate));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);

                    //Pick time within the day
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            String hours = (selectedHour < 10 ) ? "0" + selectedHour : "" + selectedHour;
                            String mins = (selectedMinute < 10) ? "0" + selectedMinute : "" + selectedMinute;
                            editText_time.setText(hours + ":" + mins);
                        }
                    }, hour, minute, true);//Yes 24 hour time
                    mTimePicker.show();

                }
            });
            editText_date.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    String appointmentDate = "" + editText_date.getText().toString() + ' ' + editText_time.getText().toString();

                    Calendar calendar = Calendar.getInstance();
                    try {
                        calendar.setTime(formatter.parse(appointmentDate));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);

                    //Pick the date, month, year
                    DatePickerDialog mDatePicker;
                    mDatePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker timePicker, int year, int monthOfYear, int dayOfMonth) {
                            monthOfYear = monthOfYear + 1;
                            String months = (monthOfYear < 10 ) ? "0" + monthOfYear : "" + monthOfYear;
                            String days = (dayOfMonth < 10) ? "0" + dayOfMonth : "" + dayOfMonth;
                            editText_date.setText(year + "-" + months + "-" + days);
                        }
                    }, year, month, day);//Yes 24 hour time
                    mDatePicker.show();
                }
            });
            final Button deleteAppointment = (Button) rootView.findViewById(R.id.button_delete);
            deleteAppointment.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
                            /*
                            long eventID= 8;
                            Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID);
                            int rows = getActivity().getContentResolver().delete(deleteUri, null, null);
                            Toast.makeText(getActivity(), "deleted google!", Toast.LENGTH_SHORT).show();*/
                            getActivity().getSupportFragmentManager().popBackStack();
                            Database.getInstance(getActivity()).deleteAppointment(appointment.id);
                        }
                    }
            );
            final Button editAppointment = (Button) rootView.findViewById(R.id.button_edit);
            editAppointment.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            editAppointment.setVisibility(EditText.INVISIBLE);
                            deleteAppointment.setVisibility(EditText.INVISIBLE);
                            editText_title.setEnabled(true);
                            editText_detail.setEnabled(true);
                            //editText_latitude.setEnabled(true);
                            //appointment.latitude = editText_latitude.getText().toString();
                            //editText_longitude.setEnabled(true);
                            //appointment.longitude = editText_longitude.getText().toString();
                            editText_time.setEnabled(true);
                            editText_date.setEnabled(true);
                            spinner_category.setEnabled(true);
                            //appointment.latitude = editText_latitude.getT;
                            //appointment.longitude = editText_longitude.getText();
                        }
                    }
            );
            final Button updateAppointment = (Button) rootView.findViewById(R.id.button_done);
            updateAppointment.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            appointment.title = editText_title.getText().toString();
                            appointment.detail = editText_detail.getText().toString();
                            appointment.time = editText_time.getText().toString();
                            appointment.date = editText_date.getText().toString();
                            appointment.category = Database.AppointmentCategory.indexOf(spinner_category.getSelectedItem().toString()) - 1; //substract one since the "index 0 : all" is hidden
                            Database.getInstance(getActivity()).updateAppointment(appointment);
                            Toast.makeText(getActivity(), "Saved!", Toast.LENGTH_SHORT).show();
                            getActivity().getSupportFragmentManager().popBackStack();
                            //Intent intent = new Intent(getActivity(), EditAppointment.class);
                            //startActivity(intent);
                            //getActivity().getSupportFragmentManager().popBackStack();
                            //Database.getInstance(getActivity()).updateAppointment(appointment);
                        }
                    }
            );
            return rootView;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDatabase.close();
    }

    @Override
    public void onDialogPositiveClick(SigninDialog dialog) {
        doctor = true;
    }

    @Override
    public void onDialogNegativeClick(SigninDialog dialog) {
        doctor = false;
    }

    private class PreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            ApplySettings();
        }
    }

    public void ApplySettings() {
        boolean gpsTrackingSwitch = mSharedPreferences.getBoolean("gps_tracking_switch", true);
        boolean phoneUsageTrackingSwitch = mSharedPreferences.getBoolean("phone_usage_tracking_switch", true);
        long gpsTrackingFrequency = Long.parseLong(mSharedPreferences.getString("gps_tracking_frequency", "5000"));
        float gpsTrackingDistanceInterval = Float.parseFloat(mSharedPreferences.getString("gps_tracking_distance_interval", "0"));
        long appointmentRemindTime = Long.parseLong(mSharedPreferences.getString("appointment_remind_time", "30"));
        float appointmentRange = Float.parseFloat(mSharedPreferences.getString("appointment_range", "500"));

        if(gpsTrackingSwitch) {
            startService(new Intent(this, GPSlocationTracingService.class));
        }
        else {
            stopService(new Intent(this, GPSlocationTracingService.class));
        }
        if(phoneUsageTrackingSwitch) {
            startService(new Intent(this, PhoneUsageTracingService.class));
        }
        else {
            stopService(new Intent(this, PhoneUsageTracingService.class));
        }
        if(gpsTrackingFrequency != GPSlocationTracingService.LOCATION_REFRESH_TIME){
            GPSlocationTracingService.LOCATION_REFRESH_TIME = gpsTrackingFrequency;
            if(stopService(new Intent(this, GPSlocationTracingService.class)))
                startService(new Intent(this, GPSlocationTracingService.class));
        }
        if(gpsTrackingDistanceInterval != GPSlocationTracingService.LOCATION_REFRESH_DISTANCE){
            GPSlocationTracingService.LOCATION_REFRESH_DISTANCE = gpsTrackingDistanceInterval;
            if(stopService(new Intent(this, GPSlocationTracingService.class)))
                startService(new Intent(this, GPSlocationTracingService.class));
        }
        if(appointmentRemindTime != GPSlocationTracingService.APPOINTMENT_REMIND_TIME){
            GPSlocationTracingService.APPOINTMENT_REMIND_TIME = appointmentRemindTime;
        }
        if(appointmentRange != GPSlocationTracingService.APPOINTMENT_RANGE){
            GPSlocationTracingService.APPOINTMENT_RANGE = appointmentRange;
        }
    }
}
