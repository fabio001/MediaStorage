package com.aybu.mediastorage;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    TextToSpeech tt;
    private static final String FILE_NAME = "myfile.txt";

    MediaPlayer mp = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String s = readContentOfFile();
        TextView txt = findViewById(R.id.txt_file);
        txt.setText(s);



        //check external storage
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            Toast.makeText(this, "External is available", Toast.LENGTH_SHORT).show();
        }


        tt = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tt.setLanguage(Locale.ENGLISH);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(getApplicationContext(), "This language is not supported", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        speak("Init is OK.");
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Initialization failed", Toast.LENGTH_SHORT).show();
                }




            }
        });
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        }

    void speak(String s){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bundle bundle = new Bundle();
            bundle.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC);
            tt.speak(s, TextToSpeech.QUEUE_ADD, bundle, null);
        } else {
            //old api
            HashMap<String, String> param = new HashMap<>();
            param.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_MUSIC));
            tt.speak(s, TextToSpeech.QUEUE_ADD, param);
        }
    }

    public void Oku(View view) {
        speak("Yeah. Done. Accomplished!");
        //String a = readFile();
        //speak(a);
        readInternet();
    }

    private void ToastMessage(String s){
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    private String readFile(){
        Scanner scn =  new Scanner(getResources().openRawResource(R.raw.oku));
        String all = "";
        while(scn.hasNext())
            all += scn.nextLine();

        scn.close();
        return all;
    }
    String text = "";
    private void readInternet(){
    text = "";
        Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL u = new URL("http://fabioviper.eu5.org/ANDROID/files/out.txt");
                        Scanner scanner = new Scanner(u.openStream());

                        while(scanner.hasNext())
                            text+=scanner.nextLine();
                       runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               TextView t = findViewById(R.id.txt_file_web);
                               t.setText(text);
                               speak(text);
                           }
                       });
                        //read
                        //speak(text);
                        Log.d("AAA", text);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        Log.d("AAA", e.getMessage());
                    }catch (IOException e) {
                        Log.d("AAA", e.getLocalizedMessage());
                    }

                }
            });
        t.start();
    }

    public void RadioButton(View view) {
        int id  =view.getId();
        if(mp == null){
            mp = MediaPlayer.create(this, R.raw.button1);
        }

        switch (id){
            case R.id.btn1:
                if(mp.isPlaying())
                    mp.stop();
                mp = MediaPlayer.create(this, R.raw.button1);
                mp.start();
                break;
            case R.id.btn2:
                mp = MediaPlayer.create(this, R.raw.button2);
                mp.start();
                break;
            case R.id.btn3:
                if(mp.isPlaying())
                    mp.stop();
                break;
                default:

        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mp!=null && mp.isPlaying())
            mp.stop();
    }

    private String readContentOfFile(){
        Scanner scanner = new Scanner(
              getResources().openRawResource(R.raw.oku)
        );

        String txt = "";
        while(scanner.hasNext())
            txt += scanner.nextLine();

        scanner.close();
        return txt;
    }

    private void writeToFile(String s ){
        try {
            PrintStream p = new PrintStream(
                    openFileOutput(FILE_NAME, MODE_PRIVATE));
            p.println(s);
            p.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private String readPrivateContent(){
        try {
            Scanner scanner = new Scanner(openFileInput(FILE_NAME));
            StringBuilder sb = new StringBuilder();
            while(scanner.hasNext())
                sb.append(scanner.nextLine());

            scanner.close();
            return sb.toString();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void btnInternetRead(View view) {
        readInternet();

    }
    private static final int REQ_CODE = 35352;
    public void takePhotoCamera(View view) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ_CODE && RESULT_OK == resultCode){
            if(data !=null){
                Bitmap bmp = (Bitmap) data.getExtras().get("data");
                ImageView img = findViewById(R.id.img_camera);
                img.setImageBitmap(bmp);
            }

        }else if (requestCode == REQ_CODE && RESULT_CANCELED == resultCode){
            ToastMessage("User canceled camera shot!");
        }
    }
}
