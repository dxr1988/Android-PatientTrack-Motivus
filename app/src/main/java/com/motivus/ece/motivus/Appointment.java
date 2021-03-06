package com.motivus.ece.motivus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

class AppointmentStatistic{
    public float rate;
    public int accomplishedAppointment;
    public int totalAppointment;
    public int currentScore;
    public int totalScore;
}
/**
 * Created by dongx on 2015-02-18.
 */
public class Appointment implements Comparable<Appointment> {
    public int id;
    public String title;
    public String detail;
    public String date;
    public String time;
    public double latitude;
    public double longitude;
    public int done = 0;
    public int score = 0;
    public byte[] pic;
    public int category = 0;

    public Appointment() {

    }

    public Appointment(int id, String title, String detail, String date, String time, Double latitude, Double longitude, int done, byte[] pic, int category) {
        super();
        this.id = id;
        this.title = title;
        this.detail = detail;
        this.date = date;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.done = done;
        this.pic = pic;
        this.category = category;
    }

    public Calendar getDateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String appointmentDate = "" + date + ' ' + time;

        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(formatter.parse(appointmentDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }
    @Override
    public int compareTo(Appointment appointment) {
        return appointment.getDateTime().compareTo(getDateTime());
    }
}
