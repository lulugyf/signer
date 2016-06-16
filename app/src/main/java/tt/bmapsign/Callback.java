package tt.bmapsign;

public interface Callback {
	public void finishCall(String ret,
			String notesid,
			String signFlag,
			String lat,
			String lng);
}
