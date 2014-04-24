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
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import models.XQueryUsage.Severity;

import org.brackit.xquery.QueryContext;
import org.brackit.xquery.QueryException;
import org.brackit.xquery.XQuery;
import org.brackit.xquery.xdm.DocumentException;
import org.sirix.xquery.node.DBStore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SirixHandler extends BackendHandlerInterface {
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
		try (final DBStore store = DBStore.newBuilder().build()) {
			final QueryContext ctx = new QueryContext(store);

			File doc1 = generateSampleDoc(tmpDir, "init commit", "sample1");
			doc1.deleteOnExit();

			// Use XQuery to load sample document into store.
			System.out.println("Loading document:");
			URI doc1Uri = doc1.toURI();
			final String xq1 = String.format(
					"bit:load('mydocs.col', io:ls('%s', '\\.xml$'))", doc1Uri.toString());
			/*final String xq1 = String.format("bit:load('" + databaseName
					+ "', '%s')", doc1Uri.toString());*/
			System.out.println(xq1);
			new XQuery(xq1).evaluate(ctx);
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
		return init();
	}

	@Override
	public boolean commit(String url, String content, String message, User user) {
		runQuery("delete node doc('" + databaseName + "')/log/content/"+url);
		append(content,url);
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
		printAllVersions();
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
		
		
		/*DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			String xml = runQuery("doc('" + databaseName + "')/log/last::*");
		
			System.out.println("xpath");
			System.out.println(xml);
			Document doc = builder.parse(new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8"))));
			System.out.println(doc.);
			//XPathFactory xPathfactory = XPathFactory.newInstance();
			//XPath xpath = xPathfactory.newXPath();
			
			//Evaluate XPath against Document itself
			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList nodes = (NodeList)xPath.evaluate("/log",
			        doc.getDocumentElement(), XPathConstants.NODESET);
			for (int i = 0; i < nodes.getLength(); ++i) {
			    Element e = (Element) nodes.item(i);
			    System.out.println(e.getLocalName());
			}
			
			
			//XPathExpression expr = xpath.compile("/content");
			//String s=(String) expr.evaluate(doc, XPathConstants.STRING);
			//System.out.println(s);
			
		} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
			// TODO Auto-generated catch block
			System.out.println("error in xPath");
			e.printStackTrace();
		}
		
		*/
		
		//System.out.println("running getRepositoryHEAD");
		//System.out.println();
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
		out.print(String.format("<msg>%s</msg>", msg));
		out.print(String.format("<content>%s</content>", msg));
		out.print("</log>");
		out.close();
		return file;
	}

	public static void append(String text,String file) {
		text="<"+file+">"+text+"</"+file+">";		
		runQueryWhithCommit("insert nodes " + text + " into doc('" + databaseName
				+ "')/log/content");
	}

	public static void printAllVersions() {
		System.out.println(runQuery("doc('" + databaseName
				+ "')/log/all-time::*"));

	}
	private static String runQuery(String query) { 
		try (final DBStore store = DBStore.newBuilder().build()) {
			System.out.println("running query:" + query);
			// Reuse store and query loaded document.
			final QueryContext ctx2 = new QueryContext(store);
			final XQuery q = new XQuery(query);
			q.prettyPrint();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream writer = new PrintStream(baos);

			q.serialize(ctx2, writer);
			String content = baos.toString("UTF8");

			return content;
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	private static String runQueryWhithCommit(String query) {
		try (final DBStore store = DBStore.newBuilder().build()) {
			System.out.println("running query:" + query);
			// Reuse store and query loaded document.
			final QueryContext ctx2 = new QueryContext(store);
			final XQuery q = new XQuery(query);
			q.prettyPrint();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream writer = new PrintStream(baos);

			q.serialize(ctx2, writer);
			String content = baos.toString("UTF8");

			store.commitAll();
			System.out.println(content);
			return content;
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
	
	
}
