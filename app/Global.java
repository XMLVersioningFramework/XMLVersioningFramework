import models.UserHandler;
import play.*;

public class Global extends GlobalSettings {

	public void onStart(Application app) {
		Logger.info("on start running");
		UserHandler.addUsers();
	}

	public void onStop(Application app) {
		Logger.info("Application shutdown...");
	}
}