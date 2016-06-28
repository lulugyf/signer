package tt.bmapsign.util;

import java.io.EOFException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

/**
 * Created by guanyf on 2016/6/28.
 */
public class FileLogger {
    public static void log(String msg) {
        String fpath = getPath();
        FileWriter fw = null;
        if(msg.indexOf('\n') >= 0)
            msg = msg.replace('\n', ' ');
        if(msg.indexOf('\r') >= 0)
            msg = msg.replace('\r', ' ');
        try {
            fw = new FileWriter(fpath, true);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss ");
            fw.append(sdf.format(new Date()));
            fw.append(msg);
            fw.append('\n');
        }catch (IOException ex){

        }finally{
            if(fw != null) try{ fw.close(); }catch (IOException ex){}
        }
    }

    public static String getPath() {
        String sd_path = Util.getInnerSDCardPath();
        String fpath = sd_path + "/signer.log";
        return fpath;
    }

    private static byte[] buf = new byte[4096];
    private static String readLine(RandomAccessFile rf) throws IOException {
        int i=0, l = buf.length;
        try {
            byte x;
            while(i < l) {
                x = rf.readByte();
                if(x == '\r' ) continue;
                if (x == '\n'){
                    break;
                }
                buf[i++] = x;
            }
        }catch (EOFException ex){
        }
        if(i == 0)
            return null;
        return new String(buf, 0, i-1, "UTF-8");
    }
    private static int readLines(RandomAccessFile rf, int n, LinkedList<String> lines) throws IOException{
        while(true){
            String line = readLine(rf);
            if(line == null) break;
            lines.add(line);
        }
        if(lines.size() > n){
            n = lines.size()-n;
            while(n-- > 0) lines.removeFirst();
        }
        return lines.size();
    }

    // read last n lines of file.
    public static String[] tail(int n) {

        String fpath = getPath();
        RandomAccessFile rf = null;
        LinkedList<String> lines = new LinkedList<>();
        try {
            rf = new RandomAccessFile(fpath, "r");
            long fpos = n * 132; //reverse position of file
            long size = rf.length();
            while(true){
                lines.clear();
                if(size <= fpos){ // read whole file
                    rf.seek(0);
                    readLines(rf, n, lines);
                    return lines.toArray(new String[lines.size()]);
                }
                rf.seek(size - fpos);
                int x = readLines(rf, n, lines);
                if(x == n)
                    return lines.toArray(new String[lines.size()]);
                else // not enough, continue
                    fpos += n * 132;
            }
        }catch (IOException e){

        }finally {
            if(rf != null) try{ rf.close(); }catch (IOException ex){}
        }
        return new String[]{"---read file failed..."};
    }
}
