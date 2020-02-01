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

package org.toasthub.admin.permission;

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
import org.toasthub.security.permission.PermissionDaoImpl;

@Repository("PermissionAdminDao")
@Transactional("TransactionManagerSecurity")
public class PermissionAdminDaoImpl extends PermissionDaoImpl implements PermissionAdminDao {

	@Override
	public void save(RestRequest request, RestResponse response) throws Exception {
		Permission permission = (Permission) request.getParam(GlobalConstant.ITEM);
		
		// get application
		if (permission.getApplication() == null || (permission.getApplication() != null && permission.getApplication().getId().equals(permission.getApplicationId()))){
			permission.setApplication(null);
			Application application = (Application) entityManagerSecuritySvc.getInstance().getReference(Application.class, permission.getApplicationId());
			permission.setApplication(application);
		}
		entityManagerSecuritySvc.getInstance().merge(permission);
	}
	
	@Override
	public void delete(RestRequest request, RestResponse response) throws Exception {
		if (request.containsParam(GlobalConstant.ITEMID) && !"".equals(request.getParam(GlobalConstant.ITEMID))) {
			
			Permission permission = (Permission) entityManagerSecuritySvc.getInstance().getReference(Permission.class,  new Long((Integer) request.getParam(GlobalConstant.ITEMID)));
			entityManagerSecuritySvc.getInstance().remove(permission);
			
		} else {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Missing ID", response);
		}
		
	}

	@Override
	public void rolePermission(RestRequest request, RestResponse response) throws Exception {
		if (request.containsParam(GlobalConstant.ITEMID) && !"".equals(request.getParam(GlobalConstant.ITEMID))) {
			String queryStr = "SELECT p FROM RolePermission AS p WHERE p.id =:id";
			Query query = entityManagerSecuritySvc.getInstance().createQuery(queryStr);
		
			query.setParameter("id", new Long((Integer) request.getParam(GlobalConstant.ITEMID)));
			RolePermission rolePermission = (RolePermission) query.getSingleResult();
			
			response.addParam(GlobalConstant.ITEM, rolePermission);
		} else {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Missing ID", response);
		}
		
	}

	@Override
	public void rolePermissionSave(RestRequest request, RestResponse response) throws Exception {
		RolePermission rolePermission = (RolePermission) request.getParam(GlobalConstant.ITEM);
		
		if (rolePermission.getRole() == null) {
			Role role = (Role) entityManagerSecuritySvc.getInstance().getReference(Role.class,  new Long((Integer) request.getParam("roleId")));
			rolePermission.setRole(role);
		}
		if (rolePermission.getPermission() == null) {
			Permission permission = (Permission) entityManagerSecuritySvc.getInstance().getReference(Permission.class,  new Long((Integer) request.getParam("permissionId")));
			rolePermission.setPermission(permission);
		}
		
		entityManagerSecuritySvc.getInstance().merge(rolePermission);
		
	}
}
