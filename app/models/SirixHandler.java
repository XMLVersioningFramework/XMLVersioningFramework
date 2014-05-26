package models;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.brackit.xquery.QueryContext;
import org.brackit.xquery.QueryException;
import org.brackit.xquery.XQuery;
import org.brackit.xquery.atomic.QNm;
import org.brackit.xquery.compiler.CompileChain;
import org.brackit.xquery.xdm.DocumentException;
import org.brackit.xquery.xdm.Item;
import org.brackit.xquery.xdm.Iter;
import org.brackit.xquery.xdm.Sequence;
import org.h2.constant.SysProperties;
import org.sirix.access.Databases;
import org.sirix.access.conf.DatabaseConfiguration;
import org.sirix.access.conf.ResourceConfiguration;
import org.sirix.access.conf.SessionConfiguration;
import org.sirix.api.Database;
import org.sirix.api.NodeReadTrx;
import org.sirix.api.NodeWriteTrx;
import org.sirix.api.Session;
import org.sirix.diff.DiffDepth;
import org.sirix.diff.DiffFactory;
import org.sirix.diff.DiffFactory.DiffOptimized;
import org.sirix.diff.DiffFactory.DiffType;
import org.sirix.diff.DiffObserver;
import org.sirix.diff.DiffTuple;
import org.sirix.diff.service.FMSEImport;
import org.sirix.exception.SirixException;
import org.sirix.io.StorageType;
import org.sirix.service.xml.serialize.XMLSerializer;
import org.sirix.xquery.SirixCompileChain;
import org.sirix.xquery.SirixQueryContext;
import org.sirix.xquery.node.DBNode;
import org.sirix.xquery.node.DBStore;

import com.google.common.collect.ImmutableSet;

public class SirixHandler extends BackendHandlerInterface implements
		DiffObserver {
	private static final String RESOURCE = "shredded";

	private static final String USER_HOME = System.getProperty("user.home");

	/** Storage for databases: Sirix data in home directory. */
	private static final File LOCATION = new File(USER_HOME, "sirix-data");

	private static final String databaseName = "mydocs.col";

	/** Severity used to build a random sample document. */
	enum Severity {
		low, high, critical
	}

	private static Map<Integer, DiffTuple> mDiffs = new HashMap<Integer, DiffTuple>();

	private Integer mEntries = 0;

	private Map<Long, Integer> mNewKeys = new HashMap<Long, Integer>();

	private Map<Long, Integer> mOldKeys = new HashMap<Long, Integer>();

	static boolean  diffDone=false;
	private SirixHandler() {
		
	}

	/**
	 * SingletonHolder is loaded on the first execution of
	 * Singleton.getInstance() or the first access to SingletonHolder.INSTANCE,
	 * not before.
	 */
	private static class SingletonHolder {
		private static final SirixHandler INSTANCE = new SirixHandler();
	}

	public static BackendHandlerInterface getInstance() {
		return SingletonHolder.INSTANCE;
	}

	@Override
	public Object getRepository() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean init() {
		resetDb();
		// Prepare sample document.
		File tmpDir = new File(System.getProperty("java.io.tmpdir"));
		System.out.println("saving temp in :" + tmpDir.getAbsolutePath());
		// Initialize query context and store.
		try (DBStore store= DBStore.newBuilder().build();){
			CompileChain compileChain = new SirixCompileChain(store);
			
			//runQuery("delete node doc('" + databaseName + "')/log");
			final QueryContext ctx = new SirixQueryContext(store);
			
			File doc1 = generateSampleDoc(tmpDir, "", "sample1");

			doc1.deleteOnExit();

			// Use XQuery to load sample document into store.
			System.out.println("Loading document:");
			URI doc1Uri = doc1.toURI();
//			final String xq1 = String.format(
//					"bit:load('mydocs.col', io:ls('%s', '\\.xml$'))",
//					doc1Uri.toString()); //old versions
			final String xq1 = String.format(
					"sdb:load('mydocs.col', '%s', '%s')", SirixHandler.RESOURCE,
					doc1Uri.toString());

			System.out.println("xq1" + xq1);
			System.out.println("ctx" + ctx);
			new XQuery(compileChain, xq1).evaluate(ctx);

		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("**************INIT PRINT ALL**********");
		printAllVersions();
		return true;
	}

	@Override
	public boolean removeExistingRepository() {
			System.out.println("removeExistingRepository: " + LOCATION + "/" + databaseName);
			return(FileUtils.deleteQuietly(new File(LOCATION, databaseName)));
	}

	@Override
	public boolean commit(String url, String content, String message, User user) {
		
		
		
		System.out.println("shredding");
		// Old Sirix resource to update.
		// File resOldRev = File.createTempFile("temp-file-name", ".tmp");
		// BufferedWriter bw = new BufferedWriter(new FileWriter(resOldRev));
		// bw.write("<a>asd<b></b></a>");
		// bw.close();

		// XML document which should be imported as the new revision.

		//	 Initialize query context and store.
		//printAllVersions();
		File resNewRev=null;
		try (DBStore store = DBStore.newBuilder().build();) {
			CompileChain compileChain = new SirixCompileChain(store);

			// runQuery("delete node doc('" + databaseName + "')/log");
			final QueryContext ctx = new SirixQueryContext(store);

			resNewRev = File.createTempFile("temp-file-name", ".tmp");
			BufferedWriter bw = new BufferedWriter(new FileWriter(resNewRev));
			bw.write("<log tstamp=\"Mon May 26 01:25:32 CEST 2014\" severity=\"high\" foo=\"bar\">"
					+ " <src>192.168.0.1</src>"
					+ " <version2/>"
					+ "</log>");
			bw.close();


		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Determine and import differences between the sirix resource and the
		// provided XML document.
		final FMSEImport fmse = new FMSEImport();
		String[] args=new String[] {
				LOCATION.getAbsolutePath() + "/" + databaseName
						,
						resNewRev.getAbsolutePath()};
		fmse.main(args);
		printAllVersions();
		System.out.println("after.....");

		// fmse.dataImport(resOldRev, resNewRev);
		
		
		
		
/*		System.out.println("commit");
		String selectFile=runQuery("doc('" + databaseName + "')/log/content/"+url);
		if(!selectFile.equals("")){
			content="<"+url+">"+content+"</"+url+">";	
			String query="replace node doc('" + databaseName + "')/log/content/"+url+" with "+content;
			runQueryWithCommit(query);
		}else{
			append(content,url);
		//	System.out.println("insert node");
			//content="<"+url+">"+content+"</"+url+">";	
			//String insertQuery="insert nodes " + content + " into doc('" + databaseName+ "')/log/content";
			//runQueryWhithCommit(insertQuery);

		}
		System.out.println("end of commit");
		*/
//		"replace node doc('" + databaseName + "')/log/content/"+url+" with "+content
//		,"doc('" + databaseName + "')/log/all-time::*");
//		runQueryWithCommit("delete node doc('" + databaseName + "')/log/content/"+url,
//				"insert nodes " + content + " into doc('" + databaseName+ "')/log/content");
//		append(content,url);
		
		return true;
	}

	@Override
	public String commitAFile(TempFile tf) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TempFile getFile(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Logs getLog() {
//		System.out.println("running get Log");
//		Logs logs =new Logs();
////		System.out.println("doc('" +databaseName+ "')/log/all-time::*");
//		String msgs=runQuery("doc('" +databaseName+ "')/log/src/all-time::*");
//		String[] msgArr=msgs.split("\n");
//		for (int i = 0; i < msgArr.length; i++) {
//			Log log=new Log(""+i, msgArr[1]);
//			logs.addLog(log);
//		}
//		System.out.println(msgs);
//		
//		return logs;
		
		try {
			testDiff();
		} catch (SirixException e) {
			e.printStackTrace();
			return null;
		}
		return new Logs();
	}

	public void testDiff() throws SirixException {
		final DatabaseConfiguration dbConf = new DatabaseConfiguration(
				new File(LOCATION, databaseName));

		final Database database = Databases.openDatabase(dbConf.getFile());

		final Session session = database
				.getSession(new SessionConfiguration.Builder(SirixHandler.RESOURCE)
						.build());

	   System.out.println( generateDiffs(session, 1, 0));

	}
	
	@Override
	public RepositoryRevision checkout(String revision) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<RepositoryFile> getWorkingDirFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRepositoryPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RepositoryRevision getRepositoryHEAD() {
		System.out.println("getRepositoryHEAD");
		String fileString=GetListOfFiles();
		fileString=fileString.replace("<file>", "");
		fileString=fileString.trim();

		String[] files = fileString.split("</file>");

		RepositoryRevision rr = new RepositoryRevision();
		rr.setLastCommitMessage(GetMsgHEAD());
		
		for (String file : files) {
			if(file!="\n"){
				String content=GetContentHEAD(file);
				rr.addRepositoryFile(new RepositoryFile(file,content));
			}
		}

		return rr;
	}

	/**
	 * Generate a small sample document. Needed?
	 * 
	 * @param dir
	 *            the directory
	 * @param prefix
	 *            prefix of name to use
	 * @return the generated file
	 * @throws IOException
	 *             if any I/O exception occurred
	 */
	static File generateSampleDoc(final File dir, final String msg,
			final String prefix) throws IOException {
		final File file = File.createTempFile(prefix, ".xml", dir);
		file.deleteOnExit();
		final PrintStream out = new PrintStream(new FileOutputStream(file));
		final Random rnd = new Random();
		final long now = System.currentTimeMillis();
		final int diff = rnd.nextInt(6000 * 60 * 24 * 7);
		final Date tst = new Date(now - diff);
		final Severity sev = Severity.values()[1];
		final String src = "192.168.0.1";

		out.print("<?xml version='1.0'?>");
		out.print(String.format("<log tstamp='%s' severity='%s' foo='bar'>",
				tst, sev));
		out.print(String.format("<src>%s</src>", src));
//		out.print(String.format("<msg><bbb>%s</bbb></msg>", msg));
		out.print(String.format("<content></content>", msg));
		out.print("</log>");
		out.close();
		
		return file;
	}

	public static void append(String text,String file) {
		System.out.println("append");
		text="<"+file+">"+text+"</"+file+">";		
		runQueryWithCommit("insert nodes " + text + " into doc('" + databaseName
				+ "')/log/content");
	}

	public static void printAllVersions() {
		printAllVersions(databaseName);

	}

	public static void printAllVersions(String s){
		System.out.println("printAllVersions");
		System.out.println(runQuery("doc('" + s
				+ "')/log"));
	}
	
	private static String runQuery(String query) { 
		try (DBStore store= DBStore.newBuilder().build();){
			CompileChain compileChain = new SirixCompileChain(store);
			
			System.out.println("running query:" + query);

			// Reuse store and query loaded document.
			final QueryContext ctx2 = new SirixQueryContext(store);

			String printAllFunctions="for $a in sdb return $a";
			
		/*	String pathQuery="let $doc := sdb:doc('mydocs.col', 'resource1')/log/content "
					//	+ "for $a in doc('" + databaseName + "')/log/content "
						+ "return  sdb:moveTo($doc,5)";*/
			
		//	String pathQuery="let $doc := sdb:doc('mydocs.col', 'resource1')/log/content "
			
			
			//sdb:getNodeKey($a)
				//+ "return $doc";
					//+"return fn:base-uri($a)";
			final XQuery q = new XQuery(query);
			q.prettyPrint();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream writer = new PrintStream(baos);
			
			q.serialize(ctx2, writer);
			String content = baos.toString("UTF8");
			//System.out.println("*********************\n "+content);
			//final Sequence seq = new XQuery(new SirixCompileChain(store), query);
			
			return content;
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (QueryException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;

	}
	
	
	private static void runQueryWithCommit(String query) {
		try (DBStore store= DBStore.newBuilder().build();){
			System.out.println("-------------------------------------------------");
			printHead();
			System.out.println("-------------------------------------------------");
			CompileChain compileChain = new SirixCompileChain(store);
			
			System.out.println("runQueryWithCommit:  "+query);
			
			QueryContext ctx1 = new SirixQueryContext(store);
//			replace node doc('" + databaseName + "')/log/content with '<hej />').evaluate(ctx1)
			System.out.println("runQueryWithCommit mid");
			//String selectQuery="doc('" + databaseName+ "')/log/all-time::*";
		//	String insertQuery="insert nodes <aa></aa> into doc('" + databaseName+ "')/log/content";
			final Sequence seq =new XQuery(compileChain, query).evaluate(ctx1);
			
			/*System.out.println(seq);
						Iter t=seq.iterate();
			Item item = t.next();
			
			while (item !=null) {
				final DBNode node = (DBNode) item;
                final NodeReadTrx rtx = node.getTrx();
                
                System.out.println("iterator getName: "+rtx.getName());
                item = t.next();
            	prettyPrint(rtx.getSession(), System.out);
			}
			
			 */
			
			
		} catch (QueryException e) {
			e.printStackTrace();
		} 
		
	}
	

	private String GetContentHEAD(String url) {
		printAllVersions();
	     
		return runQuery("doc('" + databaseName + "')/log/content/" + url
				+ "/*/last::*");
	}
	private static void printHead(){
		System.out.println(	runQuery("doc('" + databaseName + "')"));
	}
	

	private String GetMsgHEAD() {
		//getDiff(1);
		return runQuery("doc('" + databaseName + "')/log/msg/last::*");
	}

	private String GetListOfFiles() {
		System.out.println("filelist");
		return runQuery("for $a in doc('" + databaseName
				+ "')/log/content/*/last::* "
				+ "return <file>{local-name($a)}</file>");
	}

	public static void databaseSirix() {
		SirixHandler.getInstance().removeExistingRepository();

		/** Storage for databases: Sirix data in home directory. */
		try {
			final DatabaseConfiguration dbConf = new DatabaseConfiguration(
					new File(LOCATION, databaseName));
			Databases.truncateDatabase(dbConf);
			Databases.createDatabase(dbConf);
			final Database database = Databases.openDatabase(dbConf.getFile());

			database.createResource(ResourceConfiguration
					.newBuilder(SirixHandler.RESOURCE, dbConf).useDeweyIDs(true)
					.useTextCompression(true).buildPathSummary(false).build());

			final Session session = database
					.getSession(new SessionConfiguration.Builder(SirixHandler.RESOURCE)
							.build());

			try (final NodeWriteTrx wtx = session.beginNodeWriteTrx();) {
				
				wtx.insertElementAsFirstChild(new QNm("bar"));
				wtx.insertElementAsFirstChild(new QNm("foo"));
				wtx.insertElementAsFirstChild(new QNm("bao")).commit();
				wtx.moveToParent();
				wtx.moveToParent(); //moves wtx to <bar>
				wtx.insertElementAsFirstChild(new QNm("foz"));
				System.out.println("revision: " + wtx.getRevisionNumber() + " @" + wtx.getRevisionTimestamp());
				wtx.insertElementAsFirstChild(new QNm("baz")).commit();
				System.out.println("revision: " + wtx.getRevisionNumber() + " @" + wtx.getRevisionTimestamp());

				try (final NodeReadTrx rtx = session.beginNodeReadTrx();) {
					rtx.moveToDocumentRoot();
					rtx.moveToFirstChild();
					rtx.moveToFirstChild();
					rtx.moveToRightSibling();

					wtx.moveToDocumentRoot();
					wtx.moveToFirstChild();
					wtx.moveToFirstChild();
					
					System.out.println("getDeweyID: "+wtx.getDeweyID());
					
					prettyPrint(session, System.out);
					
					System.out.println("rtx is at:" + rtx.getName().toString()); //foo
					System.out.println("wtx is at:" + wtx.getName().toString()); //foz
					
					//Not working atm, issue #27 on github
					long fromKey = rtx.getNodeKey();
					System.out.println("revision: " + wtx.getRevisionNumber() + " @" + wtx.getRevisionTimestamp());
					wtx.moveSubtreeToFirstChild(fromKey).commit(); 
					System.out.println("revision: " + wtx.getRevisionNumber() + " @" + wtx.getRevisionTimestamp());
					
					prettyPrint(session, System.out);

					generateDiffs(session, 4, 3);


				}
				System.out.println(mDiffs.entrySet().toString());
			}
			System.out.println("db:" + session.getDatabase());
			System.out.println(session.getResourceConfig());

			database.close();
		} catch (SirixException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param session
	 * @throws SirixException
	 */
	private static ArrayList<String> generateDiffs(final Session session, int newRev, int oldRev)
			throws SirixException {
		System.out.println("generateDiffs");
		DiffFactory.Builder diffc = new DiffFactory.Builder(
				session, newRev, oldRev, DiffOptimized.NO,
				ImmutableSet.of((DiffObserver) getInstance()));
		DiffFactory.invokeFullDiff(diffc);
		while(!diffDone){
			System.out.println("wating for diff");
		}
		return getDiffs(session);
	}

	private static ArrayList<String> getDiffs(Session session) throws SirixException {
		System.out.println("getDiffs");
		/*
		 * TODO: if i have the oldversion db and the new version then
		 * i can search the old node source and show from where the change happened
		 */
		
		
		final NodeReadTrx rtx = session.beginNodeReadTrx();
		
		ArrayList<String> xQueryList = new ArrayList<String>();
		for (Map.Entry<Integer, DiffTuple> diff : mDiffs.entrySet()) {
			DiffTuple diffTuple = diff.getValue();
			DiffType diffType = diffTuple.getDiff();
			
			if (diffType != DiffType.SAME && diffType != DiffType.SAMEHASH) {
				rtx.moveTo(diffTuple.getOldNodeKey());
				//if(rtx.isNameNode()){
				//	System.out.println("old node current name: " + rtx.getName().toString());
				//}
				
				rtx.moveTo(diffTuple.getNewNodeKey());
				if(rtx.isNameNode()){
				//	System.out.println("element changed: " + rtx.getName().toString());
				//	System.out.println("getDeweyID: "+rtx.getDeweyID().get());
				//	System.out.println("getDeweyID list: "+rtx.getDeweyID().get().list());
					
					String xQuery="";
					if(diffType==DiffType.INSERTED){
						xQuery+="insert node "; 
						
						
					
						xQuery+="<"+rtx.getName()+"></"+rtx.getName()+">";
						
						
						xQuery+=" into sdb:select-node(doc('mydocs.col')/log ,"+rtx.getNodeKey()+")";
						
					}else if(diffType==DiffType.DELETED){
						xQuery+="delete node ";
						xQuery+="sdb:select-node(doc('mydocs.col')/log ,"+rtx.getNodeKey()+")";
						
					}else if(diffType==DiffType.REPLACEDNEW){
						System.out.println("replaceNew ");
					}
					
					
					
					//System.out.println(xQuery);	
					//rtx.getSession();
					//session.getDatabase().getDatabaseConfig();
					
					//for (int i = 0; i < rtx.getDeweyID().get().list().length(); i++) {
				//		rtx.getDeweyID().get().list().;
				//	}
					if(xQuery!=""){
						xQueryList.add(xQuery);
					}
				}
				
				//System.out.println("diff type:"+ diffTuple.getDiff());
			}
			
			
		}
		System.out.println("print Arr");
		for (Iterator iterator = xQueryList.iterator(); iterator
				.hasNext();) {
			System.out.println((String) iterator.next());
			
		}
		return xQueryList;
	}

	/**
	 * @param session
	 * @throws SirixException
	 */
	private static void prettyPrint(final Session session, PrintStream out)
			throws SirixException {
	
		final XMLSerializer serializer = XMLSerializer
				.newBuilder(session, out).prettyPrint()
				.build();
		serializer.call();
	}

	@Override
	public void diffDone() {
		System.out.println("detecting moves...");
		detectMoves();
		System.out.println("diff done");
		diffDone=true;
	}
	
	@Override
	public void diffListener(final DiffType diffType, final long newNodeKey,
			final long oldNodeKey, final DiffDepth depth) {
		final DiffTuple diffCont = new DiffTuple(diffType, newNodeKey,
				oldNodeKey, depth);
		mEntries++;
		mDiffs.put(mEntries, diffCont);
		//System.out.println("mEntries:" + mEntries);

		switch (diffType) {
		case INSERTED:
			mNewKeys.put(newNodeKey, mEntries);
			break;
		case DELETED:
			mOldKeys.put(oldNodeKey, mEntries);
			break;
		default:
		}
	}

	private void detectMoves() {
		for (final DiffTuple diffCont : mDiffs.values()) {
			final Integer newIndex = mNewKeys.get(diffCont.getOldNodeKey());
			if (newIndex != null
					&& (diffCont.getDiff() == DiffType.DELETED || diffCont
							.getDiff() == DiffType.MOVEDFROM)) {
			//	System.out.println("new node key: "
			//			+ mDiffs.get(newIndex).getNewNodeKey());
				mDiffs.get(newIndex).setDiff(DiffType.MOVEDTO);
			}
			final Integer oldIndex = mOldKeys.get(diffCont.getNewNodeKey());
			if (oldIndex != null
					&& (diffCont.getDiff() == DiffType.INSERTED || diffCont
							.getDiff() == DiffType.MOVEDTO)) {
				mDiffs.get(oldIndex).setDiff(DiffType.MOVEDFROM)
						.setIndex(mNewKeys.get(diffCont.getNewNodeKey()));
			}
		}
	}
	private void printAVersion(int i){
		System.out.println( runQuery("doc('" + databaseName+ "')/log/all-time::*[1]"));
	}
	
	
	@Override
	public ArrayList<String> getDiff(int relativeRevisionId) {
		System.out.println("getDiff betwine version "+getVersionId()+" : "+(getVersionId()-relativeRevisionId));
		printAVersion(getVersionId());
		printAVersion((getVersionId()-relativeRevisionId));
		
		
		try {
			return generateDiffs(getSession(), getVersionId()-relativeRevisionId,getVersionId());
		} catch (SirixException e) {
			e.printStackTrace();
		}
	
		return null;
	}
	private void resetDb(){
		final DatabaseConfiguration dbConf = new DatabaseConfiguration(
				new File(LOCATION, databaseName));
		Databases.truncateDatabase(dbConf);
		Databases.createDatabase(dbConf);
	}
	

	private Session getSession(){
		final DatabaseConfiguration dbConf = new DatabaseConfiguration(
				new File(LOCATION, databaseName));
		Session session=null;
		Database database;
		try {
			database = Databases.openDatabase(dbConf.getFile());
			session = database
				.getSession(new SessionConfiguration.Builder(SirixHandler.RESOURCE)
						.build());
		} catch (SirixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return session;
	}
	@Override
	public int getVersionId() {
		final DatabaseConfiguration dbConf = new DatabaseConfiguration(
				new File(LOCATION, databaseName));

		Database database;
		try {
			database = Databases.openDatabase(dbConf.getFile());
	
			final Session session = database
					.getSession(new SessionConfiguration.Builder(SirixHandler.RESOURCE)
							.build());
			return session.getMostRecentRevisionNumber();
			
		} catch (SirixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
	}
	public boolean commitXquery(ArrayList<String> xQuerys){
		for (String query : xQuerys) {
			runQueryWithCommit(query);
		}
		return true;
	}
	
	
	
	@Override
	public boolean revert(int relativeRevision) {
		
		getSession().beginNodeWriteTrx().revertTo(getVersionId()-relativeRevision);
		return false;
	}

	@Override
	public boolean commit(String url, String content, String message,
			User user, int relativeVersion) {
		ArrayList<String> pul=getDiff(relativeVersion);
		
		revert(relativeVersion);
		commit(url, content, message, user);
		commitXquery(pul);
	//	System.out.println("Nead to code this");
		
		return true;
	}

}
