package controllers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.xml.xquery.XQException;

import org.brackit.xquery.QueryException;
import org.sirix.exception.SirixException;

import models.BackendHandlerInterface;
import models.GitHandler;
import models.SirixHandler;
import models.User;
import models.UserHandler;
import models.XChroniclerHandler;
import models.XQueryUsage;
import play.libs.Json;
//import play.mvc.BodyParser.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.JSONConstants;

import com.fasterxml.jackson.databind.node.ObjectNode;


public class Application extends Controller {

	public static Result index() {
		return ok("this is just the index, you should be looking somewhere else");
	}

	private static BackendHandlerInterface getBackend(String backendName) {
		if (backendName.equals(BackendHandlerInterface.GIT)) {
			return GitHandler.getInstance();
		} else if (backendName.equals(BackendHandlerInterface.XCHRONICLER)) {
			return XChroniclerHandler.getInstance();
		} else if (backendName.equals(BackendHandlerInterface.SIRIX)) {
			return SirixHandler.getInstance();
		} else {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Used for testing, allows to initialize a repository with HTTP/GET
	 * 
	 * @param backendName
	 * @return
	 */
	public static Result initRepositoryWithGET(String backendName) {
		BackendHandlerInterface backend = getBackend(backendName);
		if (backend.init()) {
			return ok("Success initializing repository");
		} else {
			return ok("Failure to initialize repository");
		}
	}

	/**
	 * Initializes the repositories, should clean existing ones if they exist
	 * Uses HTTP/POST
	 * 
	 * @return
	 */
	public static Result initRepository() {
		final Map<String, String[]> postInput = getPOSTData();
		String backendName = postInput.get(utils.JSONConstants.BACKEND)[0];
		BackendHandlerInterface backend = getBackend(backendName);
		ObjectNode returnJson = Json.newObject();

		if (backend.init()) {
			returnJson.put(JSONConstants.ANSWER, JSONConstants.SUCCESS);
		} else {
			returnJson.put(JSONConstants.ANSWER, JSONConstants.FAIL);
		}
		response().setHeader("Access-Control-Allow-Origin", "*");

		return ok(returnJson);
	}

	/**
	 * Collects the data submitted on a HTTP/POST request
	 * 
	 * @return
	 */
	private static Map<String, String[]> getPOSTData() {
		return request().body().asFormUrlEncoded();
	}

	/**
	 * Uses HTTP/POST
	 * 
	 * @return
	 */
	public static Result getHEAD() {
		final Map<String, String[]> postInput = getPOSTData();
		BackendHandlerInterface backend = getBackend(postInput
				.get(JSONConstants.BACKEND)[0]);

		response().setHeader("Access-Control-Allow-Origin", "*");
		return ok(backend.getRepositoryHEAD().toJSON());
	}
	/**
	 * Uses HTTP/POST
	 * 
	 * @return
	 */
	public static Result getRevision() {
		final Map<String, String[]> postInput = getPOSTData();
		BackendHandlerInterface backend = getBackend(postInput
				.get(JSONConstants.BACKEND)[0]);
		String revision=postInput.get(JSONConstants.REVISION_ID)[0];

		response().setHeader("Access-Control-Allow-Origin", "*");
		return ok(backend.checkout(revision).toJSON());
	}
	
	
	
	
	/**
	 * Uses HTTP/POST
	 * 
	 * @return
	 */
	public static Result getLog() {
		final Map<String, String[]> postInput = getPOSTData();
		BackendHandlerInterface backend = getBackend(postInput
				.get(JSONConstants.BACKEND)[0]);

		response().setHeader("Access-Control-Allow-Origin", "*");
		System.out.println(backend.getLog());
		return ok(Json.toJson(backend.getLog()));
	}

	/**
	 * Uses HTTP/GET
	 * 
	 * @return
	 */
	public static Result getHEADWithGET(String backendName) {
		BackendHandlerInterface backend = getBackend(backendName);

		return ok(backend.getRepositoryHEAD().toJSON());
	}

	public static Result removeRepository(String backendName) {
		BackendHandlerInterface backend = getBackend(backendName);
		if (backend.removeExistingRepository())
			return ok(JSONConstants.SUCCESS);
		return ok(JSONConstants.FAIL);
	}

	
	public static Result commit() {
		/**
		 * Fetch content
		 */		
		final Map<String, String[]> postInput = getPOSTData();
		
		String url = postInput.get(JSONConstants.URL)[0];
		String content="";
		if(postInput.get(JSONConstants.CONTENT)!=null){
			content = postInput.get(JSONConstants.CONTENT)[0];
		}
		String message = postInput.get(JSONConstants.MESSAGE)[0];
		String userId = postInput.get(JSONConstants.USER)[0];
		String backendName = postInput.get(JSONConstants.BACKEND)[0];
				
		if(postInput.get(JSONConstants.COMMIT_FILE_URL)!=null){
			String fileurl= postInput.get(JSONConstants.COMMIT_FILE_URL)[0];
			if(fileurl!=null){
				String pathToTest="../TestFramework/public/userStories/";
				System.out.println("try to get "+pathToTest+fileurl);
				File file=new File(pathToTest+fileurl);
				System.out.println(file.getAbsolutePath());
				try {					
					List<String> lines = Files.readAllLines(Paths.get(pathToTest+fileurl), Charset.defaultCharset());
					content="";
					for (String string : lines) {
						content+=string;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		/**
		 * Pre-processes the POST contents
		 */
		BackendHandlerInterface backend = getBackend(backendName);
		User user = UserHandler.getUser(Integer.parseInt(userId));

		/**
		 * Tries to perform the Commit
		 */
		String answer = "";
		long elapsedTime = Long.MAX_VALUE;
		long start = System.nanoTime();
		if (backend.commit(url, content, message, user)) {
			answer = JSONConstants.SUCCESS;
			elapsedTime = System.nanoTime() - start;
		} else {
			answer = JSONConstants.FAIL;
		}

		/**
		 * Creates the response
		 */

		// return new ActionWrapper(super.onRequest(request, actionMethod));
		response().setHeader("Access-Control-Allow-Origin", "*");

		ObjectNode returnJson = Json.newObject();
		returnJson.put(JSONConstants.TIME, elapsedTime);
		returnJson.put(JSONConstants.ANSWER, answer);

		return ok(returnJson);
	}
	
	
	public static Result testVfile(){
		XChroniclerHandler backend = (XChroniclerHandler) XChroniclerHandler.getInstance();

		return ok(backend.generateVFileSimpleTest());
	}
	
	public static Result testXGetHEAD(String fileURL){
		try {
			String result = XChroniclerHandler.getHeadFile(fileURL);
			if(result.isEmpty())
				throw new Exception("Result was empty");
			return ok(result);
		} catch (XQException e) {
			e.printStackTrace();
			return internalServerError("The URL you sent was malformed perhaps\n Exception information: " + e.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return internalServerError("Apparently the file doesn't exist.\n Exception result: " + e.getMessage());
		}
	}
	
	public static Result testXCheckoutRevision(String revision, String fileUrl){
		try {
			String result = XChroniclerHandler.checkout(revision, fileUrl);
			if(result.isEmpty())
				throw new Exception("Result was empty");
			return ok(result);
		} catch (XQException e) {
			e.printStackTrace();
			return internalServerError("The URL you sent was malformed perhaps\n Exception information: " + e.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return internalServerError("Apparently the file doesn't exist.\n Exception result: " + e.getMessage());
		}
	}
	
	public static Result checkPreFlight() {
		// Need to add the correct domain in here!!
		response().setHeader("Access-Control-Allow-Origin", "*");
		// Only allow POST
		response().setHeader("Access-Control-Allow-Methods", "POST");
		// Cache response for 5 minutes
		response().setHeader("Access-Control-Max-Age", "300");
		// Ensure this header is also allowed!
		response().setHeader("Access-Control-Allow-Headers",
				"Origin, X-Requested-With, Content-Type, Accept");
		return ok();
	}
	public static Result testSirix(){
		SirixHandler.printAllVersions();
		/*try {
			
			//XQueryUsage.loadDocumentAndQuery();
			 //System.out.println();
			// XQueryUsage.loadDocumentAndUpdate();
			 //System.out.println();
			// XQueryUsage.loadCollectionAndQuery();
			// System.out.println();
			// XQueryUsage.loadDocumentAndQueryTemporal();
		} catch (QueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} */
		return ok();
	}
}
