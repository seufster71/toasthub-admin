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

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.toasthub.core.general.model.GlobalConstant;
import org.toasthub.core.general.model.RestRequest;
import org.toasthub.core.general.model.RestResponse;
import org.toasthub.security.model.Application;
import org.toasthub.security.model.Role;
import org.toasthub.security.model.RolePermission;
import org.toasthub.security.model.User;
import org.toasthub.security.model.UserRole;
import org.toasthub.security.role.RoleDaoImpl;

@Repository("RoleAdminDao")
@Transactional("TransactionManagerSecurity")
public class RoleAdminDaoImpl extends RoleDaoImpl implements RoleAdminDao {


	@Override
	public void save(RestRequest request, RestResponse response) throws Exception {
		Role role = (Role) request.getParam(GlobalConstant.ITEM);
		
		// get application
		if (role.getApplication() == null || (role.getApplication() != null && role.getApplication().getId().equals(role.getApplicationId())) ) {
			role.setApplication(null);
			Application application = (Application) entityManagerSecuritySvc.getInstance().getReference(Application.class, role.getApplicationId());
			role.setApplication(application);
		}
		
		// get permissions
		
		entityManagerSecuritySvc.getInstance().merge(role);
	}
	
	@Override
	public void delete(RestRequest request, RestResponse response) throws Exception {
		if (request.containsParam(GlobalConstant.ITEMID) && !"".equals(request.getParam(GlobalConstant.ITEMID))) {
			
			// Delete rolePermissions
			String queryStr = "SELECT rp.id FROM RolePermission AS rp WHERE rp.role.id =:id";
			Query query = entityManagerSecuritySvc.getInstance().createQuery(queryStr);
			query.setParameter("id", new Long((Integer) request.getParam(GlobalConstant.ITEMID)));
			List<Long> permissionIds = query.getResultList();
				
			for(Long id : permissionIds) {
				RolePermission rolePermission = (RolePermission) entityManagerSecuritySvc.getInstance().getReference(RolePermission.class, id);
				entityManagerSecuritySvc.getInstance().remove(rolePermission);
			}
			
			// Delete Role
			Role role = (Role) entityManagerSecuritySvc.getInstance().getReference(Role.class,  new Long((Integer) request.getParam(GlobalConstant.ITEMID)));
			entityManagerSecuritySvc.getInstance().remove(role);
			
		} else {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Missing ID", response);
		}
		
	}
	

	@Override
	public void userRole(RestRequest request, RestResponse response) throws Exception {
		if (request.containsParam(GlobalConstant.ITEMID) && !"".equals(request.getParam(GlobalConstant.ITEMID))) {
			String queryStr = "SELECT r FROM UserRole AS r WHERE r.id =:id";
			Query query = entityManagerSecuritySvc.getInstance().createQuery(queryStr);
		
			query.setParameter("id", new Long((Integer) request.getParam(GlobalConstant.ITEMID)));
			UserRole userRole = (UserRole) query.getSingleResult();
			
			response.addParam(GlobalConstant.ITEM, userRole);
		} else {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Missing ID", response);
		}
		
	}

	@Override
	public void userRoleSave(RestRequest request, RestResponse response) throws Exception {
		UserRole userRole = (UserRole) request.getParam(GlobalConstant.ITEM);
		
		if (userRole.getRole() == null) {
			Role role = (Role) entityManagerSecuritySvc.getInstance().getReference(Role.class,  new Long((Integer) request.getParam("roleId")));
			userRole.setRole(role);
		}
		if (userRole.getUser() == null) {
			User user = (User) entityManagerSecuritySvc.getInstance().getReference(User.class,  new Long((Integer) request.getParam("userId")));
			userRole.setUser(user);
		}
		
		entityManagerSecuritySvc.getInstance().merge(userRole);
	}
}
