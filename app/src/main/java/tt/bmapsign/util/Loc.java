package tt.bmapsign.util;

/**
 * Created by guanyf on 2016/6/15.
 */
public class Loc {
    public String name;
    public String latitude;
    public String longitude;
    public Loc(String name, String longitude, String latitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

//    马鞍东路 104.097182 30.680459
//    喜年 104.089847 30.655667
    protected static Loc L1 = new Loc("马鞍东路", "104.097182", "30.680459");
    protected static Loc L2 = new Loc("喜年", "104.089847", "30.655667");
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(name).append(' ');
        sb.append(longitude).append(' ');
        sb.append(latitude).append(' ');
        sb.append('\n');
        return sb.toString();
    }
}
