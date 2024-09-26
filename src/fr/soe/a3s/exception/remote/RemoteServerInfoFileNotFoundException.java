package fr.soe.a3s.exception.remote;

import fr.soe.a3s.dao.DataAccessConstants;

public class RemoteServerInfoFileNotFoundException extends RemoteRepositoryException
		implements DataAccessConstants {

	private static String message = "Remote file not found: " + A3S_FOlDER_NAME + "/" + SERVERINFO_FILE_NAME;

	public RemoteServerInfoFileNotFoundException() {
		super(message);
	}
}
