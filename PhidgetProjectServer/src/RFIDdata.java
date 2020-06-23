
public class RFIDdata {

	String tagid;
	String readerid;
	boolean valid;
	String doorid;

	public RFIDdata(String tagid, String readerid, boolean valid, String doorid) {
		super();
		this.tagid = tagid;
		this.readerid = readerid;
		this.valid = valid;
		this.doorid = doorid;
	}
	
	public RFIDdata(String tagid, String readerid) {
		super();
		this.tagid = tagid;
		this.readerid = readerid;
	}
	

	public String getTagid() {
		return tagid;
	}

	public void setTagid(String tagid) {
		this.tagid = tagid;
	}

	public String getReaderid() {
		return readerid;
	}

	public void setReaderid(String readerid) {
		this.readerid = readerid;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public String getDoorid() {
		return doorid;
	}

	public void setDoorid(String doorid) {
		this.doorid = doorid;
	}

	@Override
	public String toString() {
		return "RFIDdata [tagid=" + tagid + ", readerid=" + readerid + ", valid=" + valid + ", doorid=" + doorid + "]";
	}

}
