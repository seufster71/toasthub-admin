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

package org.toasthub.admin.users;

import java.util.List;

import jakarta.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.toasthub.core.general.model.GlobalConstant;
import org.toasthub.core.general.model.RestRequest;
import org.toasthub.core.general.model.RestResponse;
import org.toasthub.security.model.Role;
import org.toasthub.security.model.User;
import org.toasthub.security.model.UserRole;
import org.toasthub.security.users.UsersDaoImpl;

@Repository("UsersAdminDao")
@Transactional("TransactionManagerSecurity")
public class UsersAdminDaoImpl extends UsersDaoImpl implements UsersAdminDao {
	
	@Override
	public void save(RestRequest request, RestResponse response) throws Exception {
		User user = (User) request.getParam(GlobalConstant.ITEM);
		entityManagerSecuritySvc.getInstance().merge(user);
	}
	
	@Override
	public void delete(RestRequest request, RestResponse response) throws Exception {
		if (response.containsParam(GlobalConstant.ITEM) && !"".equals(response.getParam(GlobalConstant.ITEM))) {
			
			User userX = (User) response.getParam(GlobalConstant.ITEM);
			// delete userRoles
			String queryStr = "SELECT ur.id FROM UserRole AS ur WHERE ur.user.id =:id";
			Query query = entityManagerSecuritySvc.getInstance().createQuery(queryStr);
			query.setParameter("id", userX.getId());
			List<Long> roleIds = query.getResultList();
			
			for(Long id : roleIds) {
				UserRole userRole = (UserRole) entityManagerSecuritySvc.getInstance().getReference(UserRole.class, id);
				entityManagerSecuritySvc.getInstance().remove(userRole);
			}
			
			// delete user
			User user = (User) entityManagerSecuritySvc.getInstance().getReference(User.class,  userX.getId());
			entityManagerSecuritySvc.getInstance().remove(user);
			
		} else {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Missing ID", response);
		}
	}
	
	@Override
	public void disable(RestRequest request, RestResponse response) throws Exception {
		
	}

	@Override
	public void saveRole(RestRequest request, RestResponse response) throws Exception {
		// get Role
		Role role = (Role) entityManagerSecuritySvc.getInstance().getReference(Role.class, request.getParamLong("roleId"));
		// get User
		User user = (User) entityManagerSecuritySvc.getInstance().getReference(User.class, request.getParamLong(GlobalConstant.ITEMID));
		// save
		entityManagerSecuritySvc.getInstance().merge(new UserRole(user,role));
		
	}

	@Override
	public void deleteRole(RestRequest request, RestResponse response) throws Exception {
		String queryStr = "SELECT ur FROM UserRole AS ur WHERE ur.user.id =:uid AND ur.role.id =:rid";
		Query query = entityManagerSecuritySvc.getInstance().createQuery(queryStr);
	
		query.setParameter("uid", request.getParamLong(GlobalConstant.ITEMID));
		query.setParameter("rid", request.getParamLong("roleId"));
		UserRole userRole = (UserRole) query.getSingleResult();
		
		// remove
		entityManagerSecuritySvc.getInstance().remove(userRole);
	}
	
	
}
