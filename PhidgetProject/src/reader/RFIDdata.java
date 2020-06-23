package reader;

public class RFIDdata {

	String tagid;
	String readerid;
	boolean valid;
	String doorid;

	// Two constructors are created as only the first two variables are needed to
	// send over to the server initially. When valid check and door id are added
	// later in the program the 4 variable constructor is used.
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
