package com.team_pyl.gyrodash;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;

import static com.team_pyl.gyrodash.MainActivity.TAGGYRO;

/**
 * Created by sang on 2017-11-29.
 */

public class CreateJsonFile {
    // 핸드폰에 external storage가 유효한지 확인하는 코드 (T/F)
    public boolean isExternalStorageWritable() {

        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {

            return true;
        }
        return false;

    }
    // 핸드폰이 exteranl storage의 데이터를 읽고 쓸 수 있는지 확인하는 코드 (T/F)
    public boolean isExternalStorageReadWrite() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public String getFileName() {
        Calendar calendar = Calendar.getInstance();

        return Long.toString(calendar.getTimeInMillis());
    }


    public void writeFile(String fileName, String strData) {
        try {
            Log.w(TAGGYRO, "fileName: "+fileName);
            File sdCard = Environment.getExternalStorageDirectory(); //get sdcard
            File file = new File(sdCard, fileName+".json"); //file open

            FileOutputStream fos = new FileOutputStream(file); //file outputstream
            fos.write(strData.getBytes()); //write on fileoutputstream
            fos.close(); //close


        } catch (FileNotFoundException e) {
            Log.w(TAGGYRO, "File not found exception");
            e.printStackTrace();
        } catch (IOException e) {
            Log.w(TAGGYRO, "IO Exception");
            e.printStackTrace();
        }
    }

    public void read(String fileName)
    {

        try {
            File sdCard = Environment.getExternalStorageDirectory(); //get sdcard
            File file = new File(sdCard, fileName); //file open
//            1511896959704.txt
//            1511886866616.json
            FileInputStream fis = new FileInputStream(file); //fileinputstream initialize
            DataInputStream in = new DataInputStream(fis); //datainputstream initialize
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(in)); ///BufferedReader initialize
            String strLine; //string for read
            while ((strLine = br.readLine()) != null) {
                Log.w(TAGGYRO, "read:: "+strLine);
            }

            br.close(); //close file

        } catch(Exception e) { //error
            Log.i(TAGGYRO, "Read Exception");
        }
    }

}
