package org.toasthub.admin.users;

import org.toasthub.core.general.model.RestRequest;
import org.toasthub.core.general.model.RestResponse;
import org.toasthub.security.users.UsersDao;

public interface UsersAdminDao extends UsersDao {

	public void save(RestRequest request, RestResponse response) throws Exception;
	public void delete(RestRequest request, RestResponse response) throws Exception;
	public void disable(RestRequest request, RestResponse response) throws Exception;
	public void saveRole(RestRequest request, RestResponse response) throws Exception;
	public void deleteRole(RestRequest request, RestResponse response) throws Exception;
}
