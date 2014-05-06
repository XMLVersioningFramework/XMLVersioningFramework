package models;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import models.XQueryUsage.Severity;

import org.apache.commons.io.FileUtils;
import org.brackit.xquery.QueryContext;
import org.sirix.xquery.SirixQueryContext;
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
import org.sirix.xquery.node.DBStore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableSet;

public class SirixHandler extends BackendHandlerInterface implements DiffObserver{
	private static final String USER_HOME = System.getProperty("user.home");

	/** Storage for databases: Sirix data in home directory. */
	private static final File LOCATION = new File(USER_HOME, "sirix-data");

	private static final String databaseName = "mydocs.col";

	
	/** Severity used to build a random sample document. */
	enum Severity {
		low, high, critical
	};

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
			//final String xq1 = String.format(
			//		"bit:load('mydocs.col', io:ls('%s', '\\.xml$'))", doc1Uri.toString());//old versions
			final String xq1 = String.format("sdb:load('mydocs.col', 'resource1', '%s')",
					doc1Uri.toString());

			System.out.println("xq1"+xq1);
			System.out.println("ctx"+ctx);
			new XQuery(compileChain,xq1).evaluate(ctx);
			
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
		//runQueryWhithCommit("replace node doc('" + databaseName + "')/log/content/"+url+" with "+content);
		printAllVersions();
		//content="<"+url+">"+content+"</"+url+">";	
		String selectFile=runQuery("doc('" + databaseName + "')/log/content/"+url);
		System.out.println("before: |"+selectFile+"|");
		if(!selectFile.equals("")){
			String query="replace node doc('" + databaseName + "')/log/content/"+url+" with "+content;
			runQueryWhithCommit(query);
		}else{
			content="<"+url+">"+content+"</"+url+">";	
			String insertQuery="insert nodes " + content + " into doc('" + databaseName+ "')/log/content";
			runQueryWhithCommit(insertQuery);
		}
		
		//printAllVersions();
	//	System.out.println("get version on XPath: "+getVersionOfXpath("/log", 0));
		
		//"replace node doc('" + databaseName + "')/log/content/"+url+" with "+content
		
			//	,"doc('" + databaseName + "')/log/all-time::*");
		//runQueryWhithCommit("delete node doc('" + databaseName + "')/log/content/"+url,
		//		"insert nodes " + content + " into doc('" + databaseName+ "')/log/content");
		//append(content,url);
		
		
		return true;
	}

	@Override
	public String commitAFile(TempFile tf) {
		return null;
	}

	@Override
	public TempFile getFile(String url) {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Logs getLog() {
		// TODO Auto-generated method stub
		//printAllVersions();
		return null;
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
		String fileString=GetListOfFiles();
		System.out.println(fileString);
		fileString=fileString.replace("<file>", "");
		fileString=fileString.trim();
		String[] files = fileString.split("</file>");
		
		RepositoryRevision rr=new RepositoryRevision();
		rr.setLastCommitMessage(GetMsgHEAD());
		
		for (String file : files) {
			System.out.println("|"+file+"|");
			if(file!="\n"){
				String content=GetContentHEAD(file);
				rr.addRepositoryFile(new RepositoryFile(file,content));
			}
		}
		return rr;
	}

	/**
	 * Generate a small sample document. Neaded?
	 * 
	 * @param dir
	 *            the directory
	 * @param prefix
	 *            prefix of name to use
	 * @return the generated file
	 * @throws IOException
	 *             if any I/O exception occured
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
		//out.print(String.format("<msg><bbb>%s</bbb></msg>", msg));
		out.print(String.format("<content></content>", msg));
		out.print("</log>");
		out.close();
		
		return file;
	}

	public static void append(String text,String file) {
		text="<"+file+">"+text+"</"+file+">";		
	//	runQueryWhithCommit("insert nodes " + text + " into doc('" + databaseName
	//				+ "')/log/content");
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
	private static void runQueryWhithCommit(String query) {
		try (DBStore store= DBStore.newBuilder().build();){
			CompileChain compileChain = new SirixCompileChain(store);
			
			System.out.println("runQueryWhithCommit"+query);
			
			final QueryContext ctx1 = new SirixQueryContext(store);
			//replace node doc('" + databaseName + "')/log/content with '<hej />').evaluate(ctx1)
			
			new XQuery(compileChain, query).evaluate(ctx1);
		//	try (final DBStore store2 = DBStore.newBuilder().build()) {
		//		final QueryContext ctx2 = new QueryContext(store2);
		//		new XQuery(compileChain, "doc('" +databaseName+ "')/log/all-time::*").evaluate(ctx2);
		//	}
			
		} catch (QueryException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private static String GetContentHEAD(String url){
		return runQuery("doc('" + databaseName + "')/log/content/"+url+"/*/last::*");
	}
	private static String GetMsgHEAD(){
		return runQuery("doc('" + databaseName + "')/log/msg/last::*");
	}
	
	
	private static String GetListOfFiles(){
		System.out.println("filelist");
		return runQuery("for $a in doc('" + databaseName + "')/log/content/*/last::* "
						+"return <file>{local-name($a)}</file>");
	}
	public static void databaseSirix() {
		
		/** Storage for databases: Sirix data in home directory. */
		try {
			
			//	final File doc = generateSampleDoc(tmpDir,  "<a.txt>init file</a.txt>", "sample1");
			//	final DatabaseConfiguration dbConf = new DatabaseConfiguration(doc);
			final DatabaseConfiguration dbConf = new DatabaseConfiguration(new File(
					LOCATION, databaseName));
			Databases.truncateDatabase(dbConf);
			Databases.createDatabase(dbConf);
			final Database database = Databases.openDatabase(dbConf.getFile());
			
			database.createResource(ResourceConfiguration
					.newBuilder("resource1", dbConf).useDeweyIDs(true)
					.useTextCompression(true).buildPathSummary(true)
					.build());
			
			final Session session = database.getSession(new SessionConfiguration.Builder("resource1")
						.build());
			
				try(final NodeWriteTrx wtx = session.beginNodeWriteTrx();){
				
				wtx.insertElementAsFirstChild(new QNm("hej"));
				wtx.commit();
				wtx.insertElementAsFirstChild(new QNm("nr2"));
				wtx.commit();
				
				//wtx.moveToFirstChild();
				wtx.moveToDocumentRoot();
	//			wtx.insertCommentAsFirstChild("asdf");
	//			wtx.commit();
				wtx.insertElementAsFirstChild(new QNm("san"));
				wtx.commit();
				
				wtx.insertElementAsFirstChild(new QNm("asdsaad"));
				wtx.commit();
				wtx.moveToParent();
				wtx.replaceNode("tset");
				wtx.commit();
				System.out.println(wtx.getKind());
				wtx.moveToNext();
				System.out.println(wtx.getKind());
				//wtx.insertTextAsLeftSibling("bbb");
				wtx.insertAttribute(new QNm("aaaa"), "bar");
				System.out.println(wtx.getKind());
			
				wtx.commit();
				wtx.moveToParent();
				System.out.println(wtx.getKind());
				
				
				//	wtx.insertCommentAsFirstChild("comment").commit();
				//wtx.moveTo(4);
	//			wtx.revertTo(3);
				
				try(final NodeReadTrx rtx = session.beginNodeReadTrx();){
					rtx.moveToDocumentRoot();
					rtx.moveToFirstChild();
					//wtx.copySubtreeAsFirstChild(rtx);
					//wtx.commit();
					prettyPrint(session);
//					String content = baos.toString("UTF8");
//					System.out.println(content);
				//	System.out.println(rtx.get.getLocalName());
					
				
					
					
					DiffFactory.Builder diffb=new DiffFactory.Builder(session, 6,
				            0, DiffOptimized.NO, ImmutableSet.of((DiffObserver)getInstance()));
					DiffFactory.invokeFullDiff(diffb);
					System.out.println("getName: "+rtx.getName());
					System.out.println(rtx.getNamespaceURI());
					
					
				//	System.out.println(rtx.getAttributeCount());
				//	System.out.println(rtx.getNamespaceURI());
				//	System.out.println(rtx.getNamespaceURI());
				//	System.out.println(rtx.getItemList());
					//final NodeWriteTrx wtx = session.beginNodeWriteTrx();
					//wtx.insertTextAsFirstChild("foo");
					//wtx.commit();
					
					System.out.println("valure: "+rtx.getValue());
				}
			}
			System.out.println("db:"+session.getDatabase());
			System.out.println(session.getResourceConfig());
				database.close();
		} catch (SirixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}

	
	/**
	 * @param session
	 * @throws SirixException
	 */
	private static void prettyPrint(final Session session)
			throws SirixException {
		final XMLSerializer serializer = XMLSerializer
				.newBuilder(session, System.out).prettyPrint()
				.build();
		serializer.call();
	}

	@Override
	public void diffDone() {
		// TODO Auto-generated method stub
		System.out.println("diffDone");
	}

	Map<Integer, DiffTuple> mDiffs= new HashMap<Integer, DiffTuple>();
	
	@Override
	public void diffListener(final DiffType diffType,
		final long newNodeKey, final long oldNodeKey,
			final DiffDepth depth) {
		/*		
		final DiffTuple diffCont = new DiffTuple(diffType, newNodeKey, oldNodeKey, depth);
        
		mDiffs.put(mEntries, diffCont);

	    switch (diffType) {
		case INSERTED:
		    mNewKeys.put(newNodeKey, mEntries);
		    break;
		case DELETED:
		    mOldKeys.put(oldNodeKey, mEntries);
		    break;
	     default:
        }*/
	}
	
	
	
}
