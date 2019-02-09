package com.balakrishnan.poorna.contacts;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.net.Uri;
import android.database.Cursor;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener,View.OnClickListener{
Button b1;
Button b2;
TextView tv;
ListView lv;
TelephonyManager tm;
PhoneStateListener ls;
TextToSpeech tts;
ArrayList<Name> cname;
MyAdapter adapter;
 static   MainActivity main;
Cursor phones;
String no="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv=findViewById(R.id.textView);
        lv=findViewById(R.id.listview);
        b1=findViewById(R.id.button);
        b2=findViewById(R.id.button2);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.EXTRA_LANGUAGE);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Hello,How can I help you?");
                try{
                    startActivityForResult(intent,1);
                }catch (ActivityNotFoundException a){
                    Toast.makeText(getApplicationContext(),"Error in connection",Toast.LENGTH_LONG).show();
                }
            }
        });
        main=this;
        PhoneCallListener phoneListener = new PhoneCallListener();
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);


 }
    public static void replace(ArrayList<String> strings)
    {
        ListIterator<String> iterator = strings.listIterator();
        while (iterator.hasNext())
        {
            iterator.set(iterator.next().toLowerCase());
        }
    }
    private String containsIgnoreCase(String nmm, String soughtFor) {
        phones=getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,"upper(" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") ASC");
        while (phones.moveToNext()) {
            String disname = phones.getString(phones.getColumnIndex(nmm));
            if (disname.equalsIgnoreCase(soughtFor)) {
                return disname;
            }
        }

        return "not found";
    }
    @Override
    protected void onActivityResult(int requestCode,int resultCode,
                                    Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==1 && resultCode==RESULT_OK && data!=null){
            ArrayList<String> result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            replace(result);
            String disp=result.get(0);
            String name=disp.substring(5,result.get(0).length());
            //String name1 = name.substring(0,1).toUpperCase() + name.substring(1).toLowerCase();
            String select= ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+"=?";
            String[] t={containsIgnoreCase(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,name)};
            Toast.makeText(getApplicationContext(),t[0],Toast.LENGTH_LONG).show();
            phones=getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,select,t,null);
            if(phones.moveToNext())
                no=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            Intent in = new Intent(Intent.ACTION_CALL);
            String phnno=String.format("tel: %s",no);
            in.setData(Uri.parse(phnno));

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                Toast.makeText(getApplicationContext(), "Error in permission", Toast.LENGTH_LONG).show();
                // ActivityCompat.requestPermissions((Activity) getApplicationContext(),new String[]{Manifest.permission.CALL_PHONE},MY_PERMISSION_REQUEST_CALL_PHONE);
                return;
            }

            startActivity(in);
        }
    }
    private class PhoneCallListener extends PhoneStateListener {

        private boolean isPhoneCalling = false;

        @Override
        public void onCallStateChanged(int state, final String incomingNumber) {

            if (TelephonyManager.CALL_STATE_RINGING == state) {
                // phone ringing
                Intent in=new Intent(Intent.ACTION_MAIN);
                ComponentName cn=new ComponentName("com.balakrishnan.poorna.incomingcall","com.balakrishnan.poorna.incomingcall.MainActivity");
                in.setComponent(cn);
                startActivity(in);

            }
        }

    }



    public void onClick(View v) {

            Name nam;
            cname=new ArrayList<>();
            phones=getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,"upper(" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") ASC");
           String lastnam="";
            while (phones.moveToNext()){
               String disname=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
               String phnno=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
               nam=new Name(disname,phnno);
               cname.add(nam);
                if(disname.equals(lastnam)){
                    cname.remove(nam);
                }
                else{
                    lastnam=disname;
                }
           }

            adapter=new MyAdapter(this,cname);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Name n=   cname.get(position);
                    String nm=n.name;
                    String no=n.num;

                    Intent in = new Intent(Intent.ACTION_CALL);

                    String phnno=String.format("tel: %s",no);
                    in.setData(Uri.parse(phnno));

                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.

                        Toast.makeText(getApplicationContext(), "Error in permission", Toast.LENGTH_LONG).show();
                        // ActivityCompat.requestPermissions((Activity) getApplicationContext(),new String[]{Manifest.permission.CALL_PHONE},MY_PERMISSION_REQUEST_CALL_PHONE);
                        return;
                    }

                    startActivity(in);
                }
            });
        }

    @Override
    public void onInit(int status) {

    }

}


