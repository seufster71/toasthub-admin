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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.toasthub.core.common.UtilSvc;
import org.toasthub.core.general.handler.ServiceProcessor;
import org.toasthub.core.general.model.GlobalConstant;
import org.toasthub.core.general.model.RestRequest;
import org.toasthub.core.general.model.RestResponse;
import org.toasthub.core.preference.model.PrefCacheUtil;
import org.toasthub.security.application.ApplicationSvc;
import org.toasthub.security.model.Role;
import org.toasthub.security.model.UserRole;
import org.toasthub.security.role.RoleSvcImpl;

@Service("RoleAdminSvc")
public class RoleAdminSvcImpl extends RoleSvcImpl implements ServiceProcessor, RoleAdminSvc {

	@Autowired 
	@Qualifier("RoleAdminDao")
	RoleAdminDao roleAdminDao;
	
	@Autowired 
	PrefCacheUtil prefCacheUtil;
	
	@Autowired 
	UtilSvc utilSvc;
	
	@Autowired
	@Qualifier("ApplicationSvc")
	ApplicationSvc applicationSvc;
	
	@Override
	public void process(RestRequest request, RestResponse response) {
		String action = (String) request.getParams().get(GlobalConstant.ACTION);
		List<String> global =  new ArrayList<String>(Arrays.asList("LANGUAGES"));
		Long count = 0l;
		switch (action) {
		case "INIT":
			request.addParam(PrefCacheUtil.PREFPARAMLOC, PrefCacheUtil.RESPONSE);
			prefCacheUtil.getPrefInfo(request,response);
			this.itemCount(request, response);
			count = (Long) response.getParam(GlobalConstant.ITEMCOUNT);
			if (count != null && count > 0){
				this.items(request, response);
			}
			if (request.containsParam(GlobalConstant.PARENTID) && !"".equals(request.getParam(GlobalConstant.PARENTID))) {
				roleAdminDao.userRoleIds(request, response);
				// add user role to items
				List<UserRole> userRoles = (List<UserRole>) response.getParam("userRoles");
				List<Role> roles = (List<Role>) response.getParam(GlobalConstant.ITEMS);
				for (UserRole userRole : userRoles) {
					for (Role role : roles) {
						if (userRole.getRoleId() == role.getId()) {
							role.setUserRole(userRole);
						}
					}
				}
			}
			break;
		case "LIST":
			request.addParam(PrefCacheUtil.PREFPARAMLOC, PrefCacheUtil.RESPONSE);
			prefCacheUtil.getPrefInfo(request,response);
			this.itemCount(request, response);
			count = (Long) response.getParam(GlobalConstant.ITEMCOUNT);
			if (count != null && count > 0){
				this.items(request, response);
			}
			if (request.containsParam(GlobalConstant.PARENTID) && !"".equals(request.getParam(GlobalConstant.PARENTID))) {
				roleAdminDao.userRoleIds(request, response);
				// add user role to items
				List<UserRole> userRoles = (List<UserRole>) response.getParam("userRoles");
				List<Role> roles = (List<Role>) response.getParam(GlobalConstant.ITEMS);
				for (UserRole userRole : userRoles) {
					for (Role role : roles) {
						if (userRole.getRoleId() == role.getId()) {
							role.setUserRole(userRole);
						}
					}
				}
			}
			break;
		case "ITEM":
			request.addParam(PrefCacheUtil.PREFPARAMLOC, PrefCacheUtil.RESPONSE);
			prefCacheUtil.getPrefInfo(request,response);
			this.item(request, response);
			applicationSvc.selectList(request, response);
			// add Select... for first element
			Map<String,Object> f = new HashMap<String,Object>();
			f.put("value", 0);
			//f.put("text", "Select...");
			f.put("defaultText", "Select...");
			((List<Map<String,Object>>) response.getParam("applicationSelectList")).add(0,f);
			break;
		case "DELETE":
			this.delete(request, response);
			break;
		case "SAVE":
			if (!request.containsParam(PrefCacheUtil.PREFFORMKEYS)) {
				List<String> forms =  new ArrayList<String>(Arrays.asList("ADMIN_ROLE_FORM"));
				request.addParam(PrefCacheUtil.PREFFORMKEYS, forms);
			}
			request.addParam(PrefCacheUtil.PREFGLOBAL, global);
			prefCacheUtil.getPrefInfo(request,response);
			this.save(request, response);
			break;
		case "USER_ROLE_ITEM":
			request.addParam(PrefCacheUtil.PREFPARAMLOC, PrefCacheUtil.RESPONSE);
			prefCacheUtil.getPrefInfo(request,response);
			this.userRole(request, response);
			if (request.containsParam("roleId")) {
				response.addParam("roleId", request.getParam("roleId"));
			}
			break;	
		case "USER_ROLE_SAVE":
			if (!request.containsParam(PrefCacheUtil.PREFFORMKEYS)) {
				List<String> forms =  new ArrayList<String>(Arrays.asList("ADMIN_USER_ROLE_FORM"));
				request.addParam(PrefCacheUtil.PREFFORMKEYS, forms);
			}
			request.addParam(PrefCacheUtil.PREFGLOBAL, global);
			prefCacheUtil.getPrefInfo(request,response);
			this.userRoleSave(request, response);
			break;
		default:
			utilSvc.addStatus(RestResponse.INFO, RestResponse.ACTIONNOTEXIST, "Action not available", response);
			break;
		}
		
	}
	
	//@Authorize
	@Override
	public void delete(RestRequest request, RestResponse response) {
		try {
			roleAdminDao.delete(request, response);
			utilSvc.addStatus(RestResponse.INFO, RestResponse.SUCCESS, "Delete Successful", response);
		} catch (Exception e) {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Delete Failed", response);
			e.printStackTrace();
		}
	}
	
	//@Authorize
	@Override
	public void save(RestRequest request, RestResponse response) {
		try {
			// validate
			utilSvc.validateParams(request, response);
			
			if ((Boolean) request.getParam(GlobalConstant.VALID) == false) {
				utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Validation Error", response);
				return;
			}
			// get existing item
			Map<String,Object> inputList = (Map<String, Object>) request.getParam(GlobalConstant.INPUTFIELDS);
			if (inputList.containsKey(GlobalConstant.ITEMID) && inputList.get(GlobalConstant.ITEMID) != null && !"".equals(inputList.get(GlobalConstant.ITEMID))) {
				request.addParam(GlobalConstant.ITEMID, inputList.get(GlobalConstant.ITEMID));
				roleAdminDao.item(request, response);
				request.addParam(GlobalConstant.ITEM, response.getParam(GlobalConstant.ITEM));
				response.getParams().remove(GlobalConstant.ITEM);
			} else {
				Role role  = new Role();
				role.setArchive(false);
				role.setLocked(false);
				
				request.addParam(GlobalConstant.ITEM, role);
			}
			
			// marshall
			utilSvc.marshallFields(request, response);
			
			roleAdminDao.save(request, response);
			
			utilSvc.addStatus(RestResponse.INFO, RestResponse.SUCCESS, "Save Successful", response);
		} catch (DataIntegrityViolationException e) {
			String message = "Save Failed";
			if (e.getCause() != null && e.getCause().getCause() != null) {
				message += ": "+e.getCause().getCause().getMessage();
			}
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, message, response);
			e.printStackTrace();
		} catch (Exception e) {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Save Failed", response);
			e.printStackTrace();
		}
	}

	@Override
	public void userRole(RestRequest request, RestResponse response) {
		try {
			roleAdminDao.userRole(request, response);
			utilSvc.addStatus(RestResponse.INFO, RestResponse.SUCCESS, "Successful", response);
		} catch (Exception e) {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Failed", response);
			e.printStackTrace();
		}
	} // userRole

	@Override
	public void userRoleSave(RestRequest request, RestResponse response) {
		try {
			// validate
			utilSvc.validateParams(request, response);
			
			if ((Boolean) request.getParam(GlobalConstant.VALID) == false) {
				utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Validation Error", response);
				return;
			}
			// get existing item
			Map<String,Object> inputList = (Map<String, Object>) request.getParam(GlobalConstant.INPUTFIELDS);
			if (inputList.containsKey(GlobalConstant.ITEMID) && inputList.get(GlobalConstant.ITEMID) != null && !"".equals(inputList.get(GlobalConstant.ITEMID))) {
				request.addParam(GlobalConstant.ITEMID, inputList.get(GlobalConstant.ITEMID));
				roleAdminDao.userRole(request, response);
				request.addParam(GlobalConstant.ITEM, response.getParam(GlobalConstant.ITEM));
				response.getParams().remove(GlobalConstant.ITEM);
			} else {
				UserRole userRole = new UserRole();
				userRole.setArchive(false);
				userRole.setLocked(false);
				
				request.addParam(GlobalConstant.ITEM, userRole);
			}
			
			// marshall
			utilSvc.marshallFields(request, response);
			
			// save
			roleAdminDao.userRoleSave(request, response);
			
			utilSvc.addStatus(RestResponse.INFO, RestResponse.SUCCESS, "Save Successful", response);
		} catch (DataIntegrityViolationException e) {
			String message = "Save Failed";
			if (e.getCause() != null && e.getCause().getCause() != null) {
				message += ": "+e.getCause().getCause().getMessage();
			}
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, message, response);
			e.printStackTrace();
		} catch (Exception e) {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Save Failed", response);
			e.printStackTrace();
		}
	} // userRoleSave
}
