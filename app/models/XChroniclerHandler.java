package models;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.inject.Provider;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.admin.SVNLookClient;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

//import com.sun.org.apache.xml.internal.serialize.OutputFormat;





import se.repos.vfile.VFileCalculatorImpl;
import se.repos.vfile.VFileCommitHandler;
import se.repos.vfile.VFileCommitItemHandler;
import se.repos.vfile.VFileDocumentBuilderFactory;
import se.repos.vfile.gen.VFile;
import se.repos.vfile.store.VFileStore;
import se.repos.vfile.store.VFileStoreDisk;
//import se.simonsoft.cms.backend.svnkit.CmsRepositorySvn;
//import se.simonsoft.cms.backend.svnkit.svnlook.CmsChangesetReaderSvnkitLook;
//import se.simonsoft.cms.backend.svnkit.svnlook.CmsContentsReaderSvnkitLook;
//import se.simonsoft.cms.backend.svnkit.svnlook.SvnlookClientProviderStateless;
import se.simonsoft.cms.item.CmsItemId;
import se.simonsoft.cms.item.CmsItemPath;
import se.simonsoft.cms.item.CmsRepository;
import se.simonsoft.cms.item.RepoRevision;
import se.simonsoft.cms.item.impl.CmsItemIdUrl;

import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;
import org.xmldb.api.*;

import javax.xml.transform.OutputKeys;
//import org.exist.xmldb.EXistResource;
//import org.exist.*;
import javax.xml.xquery.*;
import javax.xml.namespace.QName;
import net.xqj.exist.ExistXQDataSource;



public class XChroniclerHandler extends BackendHandlerInterface {
	
	protected static String DRIVER = "org.exist.xmldb.DatabaseImpl"; 
    protected static String DBURI = "xmldb:exist://localhost:8080/exist/xmlrpc"; 
    protected static String collectionPath = "/db/movies"; 
    protected static String resourceName = "movies.xml"; 
	
	private boolean doCleanup = false;
	private File testDir = null;
	private File repoDir = null;
	private SVNURL repoUrl;
	private File wc = null;
	final String BASE_URL = rootBackendFolder + "XChronicler/";
	private SVNClientManager clientManager = null;
	// private Provider<SVNLookClient> svnlookProvider = new
	// SvnlookClientProviderStateless();
	private static XChroniclerHandler instance = null;

	private XChroniclerHandler() {

	}

	/**
	 * SingletonHolder is loaded on the first execution of
	 * Singleton.getInstance() or the first access to SingletonHolder.INSTANCE,
	 * not before.
	 */
	private static class SingletonHolder {
		private static final XChroniclerHandler INSTANCE = new XChroniclerHandler();
	}

	public static BackendHandlerInterface getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public void oldTest() {
		CmsRepository repository = new CmsRepository("/anyparent", "anyname");
		CmsItemId testID = new CmsItemIdUrl(repository, new CmsItemPath(
				"/basic.xml"));
		VFileStore store = null;
		try {
			/*
			 * store = this.testVFiling(testID, new File(
			 * "src/test/resources/se/repos/vfile"), "/basic_1.xml",
			 * "basic_2.xml", "basic_3.xml");
			 */
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Document document = store.get(testID);
	}

	/*
	 * Takes a series of file paths, runs unit test that asserts they can be
	 * v-filed.
	 *//*
		 * private VFileStore testVFiling(CmsItemId testID, File folder,
		 * String... filePaths) throws Exception {
		 * 
		 * // Parse the files as Documents for data integrity checking.
		 * DocumentBuilder db = new VFileDocumentBuilderFactory()
		 * .newDocumentBuilder(); ArrayList<Document> documents = new
		 * ArrayList<Document>(); for (String filePath : filePaths) { Document d
		 * = db.parse(new File(folder, filePath)); documents.add(d); }
		 * 
		 * CmsRepositorySvn repository = new CmsRepositorySvn(testID
		 * .getRepository().getParentPath(), testID.getRepository() .getName(),
		 * this.repoDir); CmsContentsReaderSvnkitLook contentsReader = new
		 * CmsContentsReaderSvnkitLook();
		 * contentsReader.setSVNLookClientProvider(this.svnlookProvider);
		 * CmsChangesetReaderSvnkitLook changesetReader = new
		 * CmsChangesetReaderSvnkitLook();
		 * changesetReader.setSVNLookClientProvider(this.svnlookProvider);
		 * 
		 * this.svncheckout();
		 * 
		 * ArrayList<RepoRevision> revisions = new ArrayList<RepoRevision>();
		 * 
		 * File testFile = new File(this.wc, testID.getRelPath().getPath());
		 * boolean addedToSVN = false;
		 * 
		 * // Commits all the files to SVN, saving the RepoRevisions of each //
		 * commit. Transformer trans =
		 * TransformerFactory.newInstance().newTransformer(); for (int i = 0; i
		 * < documents.size(); i++) { Document d = documents.get(i); Source
		 * source = new DOMSource(d); Result result = new
		 * StreamResult(testFile); trans.transform(source, result); if
		 * (!addedToSVN) { this.svnadd(testFile); addedToSVN = true; }
		 * RepoRevision svncommit = this.svncommit(""); if (svncommit == null) {
		 * throw new RuntimeException("No diff for file " + filePaths[i]); }
		 * revisions.add(svncommit); }
		 * 
		 * VFileStore store = new VFileStoreDisk("./vfilestore");
		 * VFileCalculatorImpl calculator = new VFileCalculatorImpl(store);
		 * 
		 * VFileCommitItemHandler itemHandler = new VFileCommitItemHandler(
		 * calculator, contentsReader); VFileCommitHandler commitHandler = new
		 * VFileCommitHandler(repository,
		 * itemHandler).setCmsChangesetReader(changesetReader);
		 * 
		 * 
		 * 
		 * return store; }
		 */

	private void svncheckout() throws SVNException {
		this.clientManager.getUpdateClient().doCheckout(this.repoUrl, this.wc,
				SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, false);
	}

	private void svnadd(File... paths) throws SVNException {
		this.clientManager.getWCClient().doAdd(paths, true, false, false,
				SVNDepth.INFINITY, true, true, true);
	}

	/**
	 * @param comment
	 * @return revision if committed, null if nothing to commit
	 * @throws SVNException
	 */
	private RepoRevision svncommit(String comment) throws SVNException {
		SVNCommitInfo info = this.clientManager.getCommitClient().doCommit(
				new File[] { this.wc }, false, comment, null, null, false,
				false, SVNDepth.INFINITY);
		long revision = info.getNewRevision();
		if (revision < 0L) {
			return null;
			// this.doCleanup = false;
			// throw new
			// RuntimeException("SVN returned negative version number. Working copy: "
			// + this.wc);
		}
		return new RepoRevision(revision, info.getDate());
	}

	@Override
	public Object getRepository() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean init() {
		this.testDir = new File(BASE_URL);

		// this.testDir.delete();

		this.repoDir = new File(this.testDir, "repo");
		try {
			FileUtils.cleanDirectory(this.repoDir);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			this.repoUrl = SVNRepositoryFactory.createLocalRepository(
					this.repoDir, true, false);
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// for low level operations
		// SVNRepository repo = SVNRepositoryFactory.create(repoUrl);
		this.wc = new File(this.testDir, "wc");
		try {
			FileUtils.cleanDirectory(this.wc);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Running local fs repository " + this.repoUrl);
		this.clientManager = SVNClientManager.newInstance();
		return true;
	}

	@Override
	public String commitAFile(TempFile tf) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public TempFile getFile(String url) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public ArrayList<RepositoryFile> getWorkingDirFiles() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean commit(String url, String content, String message, User user) {
		/*
		 * CmsRepository repository = new CmsRepository("/anyparent",
		 * "anyname"); CmsItemId testID = new CmsItemIdUrl(repository, new
		 * CmsItemPath( "/basic.xml")); VFileStore store = null; try { store =
		 * this.testVFiling(testID, new File(
		 * "./backends/XChronicler/testdata"), "/basic_1.xml", "basic_2.xml",
		 * "basic_3.xml"); } catch (Exception e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); } //store ==null Document document =
		 * store.get(testID);
		 */
		/*
		 * System.out.println(document); VFile v = new VFile(store.get(testID));
		 * v.
		 *//*
			 * 
			 * try { printDocument(document, System.out); } catch (IOException
			 * e) { // TODO Auto-generated catch block e.printStackTrace(); }
			 * catch (TransformerException e) { // TODO Auto-generated catch
			 * block e.printStackTrace(); }
			 */
		//tryXSLT();
		try {
			tryExistDB();
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | XMLDBException |XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;

	}

	public static void printDocument(Document doc, OutputStream out)
			throws IOException, TransformerException {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "4");

		transformer.transform(new DOMSource(doc), new StreamResult(
				new OutputStreamWriter(out, "UTF-8")));
	}

	@Override
	public boolean removeExistingRepository() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRepositoryPath() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public RepositoryRevision getRepositoryHEAD() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	/*public void tryXSLT() {
		String xsltResource = "<?xml version='1.0' encoding='UTF-8'?>\n"
				+ "<xsl:stylesheet version='2.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>\n"
				+ "   <xsl:output method='xml' indent='no'/>\n"
				+ "   <xsl:template match='/'>\n"
				+ "      <reRoot><reNode><xsl:value-of select='/root/nodee/@val' /> world</reNode></reRoot>\n"
				+ "   </xsl:template>\n" + "</xsl:stylesheet>";
		String xmlSourceResource = "<?xml version='1.0' encoding='UTF-8'?>\n"
				+ "<root><node val='hello aa '/><nodee id='hje' val='not hello'/></root>";

		StringWriter xmlResultResource = new StringWriter();

		Transformer xmlTransformer;
		try {
			xmlTransformer = TransformerFactory.newInstance().newTransformer(
					new StreamSource(new StringReader(xsltResource)));

			xmlTransformer.transform(new StreamSource(new StringReader(
					xmlSourceResource)), new StreamResult(xmlResultResource));
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(xmlResultResource.getBuffer().toString());
	}
	 */
	public void tryExistDB() throws ClassNotFoundException, InstantiationException, IllegalAccessException, XMLDBException,XQException {

		 XQDataSource xqs = new ExistXQDataSource();
	    xqs.setProperty("serverName", "localhost");
	    xqs.setProperty("port", "8080");

	    XQConnection conn = xqs.getConnection();

	    XQPreparedExpression xqpe =
	      conn.prepareExpression("declare variable $x as xs:string external; $x");

	    xqpe.bindString(new QName("x"), "Hello World!", null);

	    XQResultSequence rs = xqpe.executeQuery();

	    while(rs.next())
	      System.out.println(rs.getItemAsString(null));

	    conn.close();
	}
}
