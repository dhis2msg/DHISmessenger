package org.dhis2.messaging.Models;

public class InboxModel implements Comparable<InboxModel> {
	private final String subject;
	private final String date;
	private final String id;
    private final String lastSender;
	private final boolean read;

	public InboxModel(String subject, String date, String id,String lastSender, boolean read){
		this.subject = subject;
		this.date = convertDate(date);
		this.id = id;
		this.read = read;
        this.lastSender = lastSender;
	}

    public String getSubject() {
        return this.subject;
    }
    public String getDate() {
        return this.date;
    }
    public String getId() {
        return this.id;
    }
    public String getLastSender() {
        return this.lastSender;
    }
    public boolean getRead() {
        return this.read;
    }

    public String convertDate(String d)
    {
        d = d.substring(0, 10);
        d = d.replaceAll("\\D+",".");
        return d;
    }
    public int compareTo(InboxModel model) {
        if(!read && model.read  ) {
            return -1;
        }
        if(read && !model.read  ) {
            return 1;
        }
        return 0;
    }
}
