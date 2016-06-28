package tt.bmapsign.util;

import android.os.Environment;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Created by guanyf on 2016/6/15.
 */
public class Util {
    /**
     * 获取内置SD卡路径
     * @return
     */
    public static String getInnerSDCardPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    public static String join(String[] lines, String sep) {
        StringBuilder sb = new StringBuilder();
        for(String line: lines) {
            sb.append(line);
            sb.append(sep);
        }
        return sb.toString();
    }

    /**
     * 获取外置SD卡路径
     * @return  应该就一条记录或空
     */
    public static List<String> getExtSDCardPath()
    {
        List<String> lResult = new ArrayList<String>();
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("extSdCard"))
                {
                    String [] arr = line.split(" ");
                    String path = arr[1];
                    File file = new File(path);
                    if (file.isDirectory())
                    {
                        lResult.add(path);
                    }
                }
            }
            isr.close();
        } catch (Exception e) {
        }
        return lResult;
    }

    public static String showip() {
        StringBuffer sb = new StringBuffer();
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("netcfg");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(" UP "))
                {
                    sb.append(line).append('\n');
                }
            }
            isr.close();
        } catch (Exception e) {
        }
        return sb.toString();
    }

    public static List<Loc> getLocList() {
        ArrayList<Loc> r = new ArrayList<>();
        String fpath = getInnerSDCardPath() + "/loc.list";
        File f = new File(fpath);
        if(!f.exists()) {
            r.add(Loc.L1);
            r.add(Loc.L2);
            saveLocList(r);
            return r;
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = null; //scanner.useDelimiter(" |,|\\n|\\r");
            while((line = br.readLine()) != null){
                String[] fs = line.split(" |,|\\t");
                if(fs.length != 3)
                    continue;
                r.add(new Loc(fs[0], fs[1], fs[2]));
            }
            br.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }catch(NoSuchElementException ex){
            ex.printStackTrace();
        }

        return r;
    }

    public static void saveLocList( List<Loc> loc) {
        String fpath = getInnerSDCardPath() + "/loc.list";
        File f = new File(fpath);
            try {
                FileWriter fw = new FileWriter(f);
                for(Loc l: loc){
                    fw.write(l.toString());
                }
                fw.close();
            }catch(IOException ex){
                ex.printStackTrace();
            }

        }
    }
