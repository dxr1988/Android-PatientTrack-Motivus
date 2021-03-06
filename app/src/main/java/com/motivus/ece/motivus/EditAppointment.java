package com.motivus.ece.motivus;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;


public class EditAppointment extends Activity {

    private EditText titleAppointment;
    private EditText detailAppointment;
    private final int GPSActivityIndex = 0;
    private double latitude;
    private double longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_appoinment);


    titleAppointment = (EditText) findViewById(R.id.editText_title);
    titleAppointment.setText(Database.APPOINTMENT_COLUMN_NAME_TITLE, EditText.BufferType.EDITABLE);
    detailAppointment = (EditText) findViewById(R.id.editText_detail);

    //Add map button
    Button mapLocation = (Button) findViewById(R.id.button_location);
    mapLocation.setOnClickListener(
            new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent map = new Intent(v.getContext(), GoogleMaps.class);
            startActivityForResult(map, GPSActivityIndex);
        }
    }
    );

    //Add time button
    final EditText dateAppointment = (EditText) findViewById(R.id.editText_date);
    final EditText timeAppointment = (EditText) findViewById(R.id.editText_time);
    //Add map button
    Button timePicker = (Button) findViewById(R.id.button_timePicker);
    timePicker.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            //Pick time within the day
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(EditAppointment.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                    String hours = (selectedHour < 10 ) ? "0" + selectedHour : "" + selectedHour;
                    String mins = (selectedMinute < 10) ? "0" + selectedMinute : "" + selectedMinute;
                    timeAppointment.setText(hours + ":" + mins);
                }
            }, hour, minute, true);//Yes 24 hour time
            mTimePicker.show();

            //Pick the date, month, year
            DatePickerDialog mDatePicker;
            mDatePicker = new DatePickerDialog(EditAppointment.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker timePicker, int year, int monthOfYear, int dayOfMonth) {
                    monthOfYear = monthOfYear + 1;
                    String months = (monthOfYear < 10 ) ? "0" + monthOfYear : "" + monthOfYear;
                    String days = (dayOfMonth < 10) ? "0" + dayOfMonth : "" + dayOfMonth;
                    dateAppointment.setText(year + "-" + months + "-" + days);
                }
            }, year, month, day);//Yes 24 hour time
            mDatePicker.show();
        }
    });

    //Add new appointment button
    Button submitAppointment = (Button) findViewById(R.id.button_submit);
    submitAppointment.setOnClickListener(
            new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Appointment appointment = new Appointment();
            appointment.title = titleAppointment.getText().toString();
            appointment.detail = detailAppointment.getText().toString();
            appointment.date = dateAppointment.getText().toString();
            appointment.time = timeAppointment.getText().toString();
            appointment.latitude = latitude;
            appointment.longitude = longitude;
            Database.getInstance(getApplication()).updateAppointment(appointment);

            Intent data = new Intent();
            if (getParent() == null) {
                setResult(Activity.RESULT_OK, data);
            } else {
                getParent().setResult(Activity.RESULT_OK, data);
            }
            finish();
        }
    }
    );
}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_appointment, menu);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (GPSActivityIndex) : {
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    latitude = bundle.getDouble("latitude");
                    longitude = bundle.getDouble("longitude");
                    //Update your TextView.
                    EditText location = (EditText) findViewById(R.id.editText_location);
                    location.setText("" + latitude + "," + longitude);
                }
                break;
            }
        }
    }
}
