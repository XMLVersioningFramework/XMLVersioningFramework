package models;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.brackit.xquery.QueryContext;
import org.brackit.xquery.QueryException;
import org.brackit.xquery.XQuery;
import org.brackit.xquery.atomic.QNm;
import org.brackit.xquery.compiler.CompileChain;
import org.brackit.xquery.xdm.DocumentException;
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
import org.sirix.exception.SirixException;
import org.sirix.service.xml.serialize.XMLSerializer;
import org.sirix.xquery.SirixCompileChain;
import org.sirix.xquery.SirixQueryContext;
import org.sirix.xquery.node.DBStore;

import com.google.common.collect.ImmutableSet;

public class SirixHandler extends BackendHandlerInterface implements
		DiffObserver {
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
		// Prepare sample document.
		File tmpDir = new File(System.getProperty("java.io.tmpdir"));
		System.out.println("saving temp in :" + tmpDir.getAbsolutePath());
		// Initialize query context and store.
		try (DBStore store= DBStore.newBuilder().build();){
			CompileChain compileChain = new SirixCompileChain(store);
			
			//runQuery("delete node doc('" + databaseName + "')/log");
			final QueryContext ctx = new SirixQueryContext(store);
			
			File doc1 = generateSampleDoc(tmpDir, "<a>init file</a>", "sample1");

			doc1.deleteOnExit();

			// Use XQuery to load sample document into store.
			System.out.println("Loading document:");
			URI doc1Uri = doc1.toURI();
//			final String xq1 = String.format(
//					"bit:load('mydocs.col', io:ls('%s', '\\.xml$'))",
//					doc1Uri.toString()); //old versions
			final String xq1 = String.format(
					"sdb:load('mydocs.col', 'resource1', '%s')",
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
		return true;
	}

	@Override
	public boolean removeExistingRepository() {
			System.out.println("removeExistingRepository: " + LOCATION + "/" + databaseName);
			return(FileUtils.deleteQuietly(new File(LOCATION, databaseName)));
	}

	@Override
	public boolean commit(String url, String content, String message, User user) {
		System.out.println("commit");
//		runQueryWithCommit("replace node doc('" + databaseName + "')/log/content/"+url+" with "+content);
		printAllVersions();	
		String selectFile=runQuery("doc('" + databaseName + "')/log/content/"+url);
		System.out.println("before: |"+selectFile+"|");
		if(!selectFile.equals("")){
			System.out.println("replace node");
			String query="replace node doc('" + databaseName + "')/log/content/"+url+" with "+content;
			runQueryWithCommit(query);
		}else{
			append(content,url);
			System.out.println("insert node");
			//content="<"+url+">"+content+"</"+url+">";	
			//String insertQuery="insert nodes " + content + " into doc('" + databaseName+ "')/log/content";
			//runQueryWhithCommit(insertQuery);

		}
		System.out.println("end of commit");
		
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
				.getSession(new SessionConfiguration.Builder("resource1")
						.build());

	   generateDiffs(session, 2, 1);

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
		out.print(String.format("<content><a.txt></a.txt></content>", msg));
		out.print("</log>");
		out.close();
		
		return file;
	}

	public static void append(String text,String file) {
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
				+ "')/log/all-time::*"));
	}
	
	private static String runQuery(String query) { 
		try (DBStore store= DBStore.newBuilder().build();){
			CompileChain compileChain = new SirixCompileChain(store);
			
			System.out.println("running query:" + query);

			// Reuse store and query loaded document.
			final QueryContext ctx2 = new SirixQueryContext(store);
			
			final XQuery q = new XQuery(query);
			q.prettyPrint();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream writer = new PrintStream(baos);

			q.serialize(ctx2, writer);
			String content = baos.toString("UTF8");

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
			
			CompileChain compileChain = new SirixCompileChain(store);
			
			System.out.println("runQueryWithCommit"+query);
			
			QueryContext ctx1 = new SirixQueryContext(store);
//			replace node doc('" + databaseName + "')/log/content with '<hej />').evaluate(ctx1)
			System.out.println("runQueryWithCommit mid");
			new XQuery(compileChain, query).evaluate(ctx1);
			System.out.println("intsert node");
		
			System.out.println("runQueryWithCommit end");
			printAllVersions();

		} catch (QueryException e) {
			e.printStackTrace();
		}
		
	}

	private static String GetContentHEAD(String url) {
		return runQuery("doc('" + databaseName + "')/log/content/" + url
				+ "/*/last::*");
	}

	private static String GetMsgHEAD() {
		return runQuery("doc('" + databaseName + "')/log/msg/last::*");
	}

	private static String GetListOfFiles() {
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
					.newBuilder("resource1", dbConf).useDeweyIDs(true)
					.useTextCompression(true).buildPathSummary(false).build());

			final Session session = database
					.getSession(new SessionConfiguration.Builder("resource1")
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
	private static void generateDiffs(final Session session, int newRev, int oldRev)
			throws SirixException {
		DiffFactory.Builder diffc = new DiffFactory.Builder(
				session, newRev, oldRev, DiffOptimized.NO,
				ImmutableSet.of((DiffObserver) getInstance()));
		DiffFactory.invokeFullDiff(diffc);
		displayDiffs(session);
	}

	private static void displayDiffs(Session session) throws SirixException {
		/*
		 * TODO: if i have the oldversion db and the new version then
		 * i can search the old node source and show from where the change happened
		 */

		final NodeReadTrx rtx = session.beginNodeReadTrx();
		for (Map.Entry<Integer, DiffTuple> diff : mDiffs.entrySet()) {
			DiffTuple diffTuple = diff.getValue();
			DiffType diffType = diffTuple.getDiff();
			
			if (diffType != DiffType.SAME && diffType != DiffType.SAMEHASH) {
				rtx.moveTo(diffTuple.getOldNodeKey());
				if(rtx.isNameNode()){
					System.out.println("old node current name: " + rtx.getName().toString());
				}
				
				rtx.moveTo(diffTuple.getNewNodeKey());
				if(rtx.isNameNode()){
					System.out.println("element changed: " + rtx.getName().toString());
				}
				System.out.println("diff type:"+ diffTuple.getDiff());
			}
		}
		
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
	}
	
	@Override
	public void diffListener(final DiffType diffType, final long newNodeKey,
			final long oldNodeKey, final DiffDepth depth) {
		final DiffTuple diffCont = new DiffTuple(diffType, newNodeKey,
				oldNodeKey, depth);
		mEntries++;
		mDiffs.put(mEntries, diffCont);
		System.out.println("mEntries:" + mEntries);

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
				System.out.println("new node key: "
						+ mDiffs.get(newIndex).getNewNodeKey());
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

}
