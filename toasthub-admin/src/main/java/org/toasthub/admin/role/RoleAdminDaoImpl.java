/*
 * Copyright (C) 2016 The ToastHub Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.toasthub.admin.role;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.toasthub.core.general.model.GlobalConstant;
import org.toasthub.core.general.model.RestRequest;
import org.toasthub.core.general.model.RestResponse;
import org.toasthub.security.model.Application;
import org.toasthub.security.model.Permission;
import org.toasthub.security.model.Role;
import org.toasthub.security.model.RolePermission;
import org.toasthub.security.repository.RoleDaoImpl;

@Repository("RoleAdminDao")
@Transactional("TransactionManagerSecurity")
public class RoleAdminDaoImpl extends RoleDaoImpl implements RoleAdminDao {


	@Override
	public void save(RestRequest request, RestResponse response) throws Exception {
		Role role = (Role) request.getParam(GlobalConstant.ITEM);
		
		// get application
		if (role.getApplication() == null) {
			Application application = (Application) entityManagerSecuritySvc.getInstance().getReference(Application.class, role.getApplicationId());
			role.setApplication(application);
		}
		
		// get permissions
		
		entityManagerSecuritySvc.getInstance().merge(role);
	}
	
	@Override
	public void delete(RestRequest request, RestResponse response) throws Exception {
		if (request.containsParam(GlobalConstant.ITEMID) && !"".equals(request.getParam(GlobalConstant.ITEMID))) {
			
			Role role = (Role) entityManagerSecuritySvc.getInstance().getReference(Role.class,  new Long((Integer) request.getParam(GlobalConstant.ITEMID)));
			entityManagerSecuritySvc.getInstance().remove(role);
			
		} else {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Missing ID", response);
		}
		
	}
	
	@Override
	public void savePermission(RestRequest request, RestResponse response) throws Exception {
		// get Role
		Role role = (Role) entityManagerSecuritySvc.getInstance().getReference(Role.class, new Long((Integer) request.getParam(GlobalConstant.ITEMID)));
		// get Permission
		Permission permission = (Permission) entityManagerSecuritySvc.getInstance().getReference(Permission.class, new Long((Integer) request.getParam("permissionId")));
		// save
		entityManagerSecuritySvc.getInstance().merge(new RolePermission(role,permission));
		
		
	}

	@Override
	public void deletePermission(RestRequest request, RestResponse response) throws Exception {
		
		String queryStr = "SELECT rp FROM RolePermission AS rp WHERE rp.role.id =:rid AND rp.permission.id =:pid";
		Query query = entityManagerSecuritySvc.getInstance().createQuery(queryStr);
	
		query.setParameter("rid", new Long((Integer) request.getParam(GlobalConstant.ITEMID)));
		query.setParameter("pid", new Long((Integer) request.getParam("permissionId")));
		RolePermission rolePermission = (RolePermission) query.getSingleResult();
		
		// remove
		entityManagerSecuritySvc.getInstance().remove(rolePermission);
	}
}
