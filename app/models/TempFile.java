package models;

public class TempFile {
	String url="";
	String content="";
	String revision="";
	String commitMsg="";
	public TempFile(String turl,String tcontent,String trevision,String tcommitMsg){
		url=turl;
		content=tcontent;
		revision=trevision;
		commitMsg=tcommitMsg;
	}
	public String getUrl() {
		return url;
	}
	public String getContent() {
		return content;
	}
	public String getRevision() {
		return revision;
	}
	public String getCommitMsg() {
		return commitMsg;
	}
}
