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
import org.toasthub.core.preference.model.AppCachePageUtil;
import org.toasthub.security.application.ApplicationSvc;
import org.toasthub.security.model.Permission;
import org.toasthub.security.model.RolePermission;
import org.toasthub.security.permission.PermissionSvcImpl;

@Service("PermissionAdminSvc")
public class PermissionAdminSvcImpl extends PermissionSvcImpl implements ServiceProcessor, PermissionAdminSvc {

	@Autowired 
	@Qualifier("PermissionAdminDao")
	PermissionAdminDao permissionAdminDao;
	
	@Autowired 
	AppCachePageUtil appCachePageUtil;
	
	@Autowired 
	UtilSvc utilSvc;
	
	@Autowired
	ApplicationSvc applicationSvc;
	
	@Override
	public void process(RestRequest request, RestResponse response) {
		String action = (String) request.getParams().get(GlobalConstant.ACTION);
		List<String> global =  new ArrayList<String>(Arrays.asList("LANGUAGES"));
		Long count = 0l;
		switch (action) {
		case "INIT":
			request.addParam(AppCachePageUtil.APPPAGEPARAMLOC, AppCachePageUtil.RESPONSE);
			appCachePageUtil.getPageInfo(request,response);
			this.itemCount(request, response);
			count = (Long) response.getParam(GlobalConstant.ITEMCOUNT);
			if (count != null && count > 0){
				items(request, response);
			}
			if (request.containsParam("roleId") && !"".equals(request.getParam("roleId"))) {
				permissionAdminDao.rolePermissionIds(request,response);
				// add role permissions to items
				List<RolePermission> rolePermissions = (List<RolePermission>) response.getParam("rolePermissions");
				List<Permission> permissions = (List<Permission>) response.getParam(GlobalConstant.ITEMS);
				for (RolePermission rolePermission : rolePermissions) {
					for (Permission permission : permissions) {
						if (rolePermission.getPermissionId() == permission.getId()) {
							permission.setRolePermission(rolePermission);
						}
					}
				}
				response.addParam("rolePermissions", null);
			}
			response.addParam(GlobalConstant.ITEMNAME, request.getParam(GlobalConstant.ITEMNAME));
			break;
		case "LIST":
			request.addParam(AppCachePageUtil.APPPAGEPARAMLOC, AppCachePageUtil.RESPONSE);
			appCachePageUtil.getPageInfo(request,response);
			this.itemCount(request, response);
			count = (Long) response.getParam(GlobalConstant.ITEMCOUNT);
			if (count != null && count > 0){
				this.items(request, response);
			}
			if (request.containsParam("roleId") && !"".equals(request.getParam("roleId"))) {
				permissionAdminDao.rolePermissionIds(request,response);
				// add role permissions to items
				List<RolePermission> rolePermissions = (List<RolePermission>) response.getParam("rolePermissions");
				List<Permission> permissions = (List<Permission>) response.getParam("items");
				for (RolePermission rolePermission : rolePermissions) {
					for (Permission permission : permissions) {
						if (rolePermission.getPermissionId() == permission.getId()) {
							permission.setRolePermission(rolePermission);
						}
					}
				}
				response.addParam("rolePermissions", null);
			}
			response.addParam(GlobalConstant.ITEMNAME, request.getParam(GlobalConstant.ITEMNAME));
			break;
		case "ITEM":
			request.addParam(AppCachePageUtil.APPPAGEPARAMLOC, AppCachePageUtil.RESPONSE);
			appCachePageUtil.getPageInfo(request,response);
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
			if (!request.containsParam("appForms")) {
				List<String> forms =  new ArrayList<String>(Arrays.asList("ADMIN_PERMISSION_FORM"));
				request.addParam("appForms", forms);
			}
			request.addParam("appGlobal", global);
			appCachePageUtil.getPageInfo(request,response);
			this.save(request, response);
			break;
		case "ROLE_PERMISSION_ITEM":
			request.addParam(AppCachePageUtil.APPPAGEPARAMLOC, AppCachePageUtil.RESPONSE);
			appCachePageUtil.getPageInfo(request,response);
			this.rolePermission(request, response);
			if (request.containsParam("permissionId")) {
				response.addParam("permissionId", request.getParam("permissionId"));
			}
			break;	
		case "ROLE_PERMISSION_SAVE":
			if (!request.containsParam("appForms")) {
				List<String> forms =  new ArrayList<String>(Arrays.asList("ADMIN_ROLE_PERMISSION_FORM"));
				request.addParam("appForms", forms);
			}
			request.addParam("appGlobal", global);
			appCachePageUtil.getPageInfo(request,response);
			this.rolePermissionSave(request, response);
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
			permissionAdminDao.delete(request, response);
			
		} catch (Exception e) {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Delete Failed", response);
			e.printStackTrace();
		}
	} // delete
	
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
			Map<String,Object> inputList = (Map<String, Object>) request.getParam("inputFields");
			if (inputList.containsKey(GlobalConstant.ITEMID) && inputList.get(GlobalConstant.ITEMID) != null && !"".equals(inputList.get(GlobalConstant.ITEMID))) {
				request.addParam(GlobalConstant.ITEMID, inputList.get(GlobalConstant.ITEMID));
				permissionAdminDao.item(request, response);
				request.addParam(GlobalConstant.ITEM, response.getParam(GlobalConstant.ITEM));
				response.getParams().remove(GlobalConstant.ITEM);
			} else {
				Permission permission = new Permission();
				permission.setArchive(false);
				permission.setLocked(false);
				
				request.addParam(GlobalConstant.ITEM, permission);
			}
			
			// marshall
			utilSvc.marshallFields(request, response);
			
			// save
			permissionAdminDao.save(request, response);
			
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
	} // save


	@Override
	public void rolePermission(RestRequest request, RestResponse response) {
		try {
			permissionAdminDao.rolePermission(request, response);
			utilSvc.addStatus(RestResponse.INFO, RestResponse.SUCCESS, "Successful", response);
		} catch (Exception e) {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Failed", response);
			e.printStackTrace();
		}
	} // rolePermission


	@Override
	public void rolePermissionSave(RestRequest request, RestResponse response) {
		try {
			// validate
			utilSvc.validateParams(request, response);
			
			if ((Boolean) request.getParam(GlobalConstant.VALID) == false) {
				utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Validation Error", response);
				return;
			}
			// get existing item
			Map<String,Object> inputList = (Map<String, Object>) request.getParam("inputFields");
			if (inputList.containsKey(GlobalConstant.ITEMID) && inputList.get(GlobalConstant.ITEMID) != null && !"".equals(inputList.get(GlobalConstant.ITEMID))) {
				request.addParam(GlobalConstant.ITEMID, inputList.get(GlobalConstant.ITEMID));
				permissionAdminDao.rolePermission(request, response);
				request.addParam(GlobalConstant.ITEM, response.getParam(GlobalConstant.ITEM));
				response.getParams().remove(GlobalConstant.ITEM);
			} else {
				RolePermission rolePermission = new RolePermission();
				rolePermission.setArchive(false);
				rolePermission.setLocked(false);
				
				request.addParam(GlobalConstant.ITEM, rolePermission);
			}
			
			// marshall
			utilSvc.marshallFields(request, response);
			
			// save
			permissionAdminDao.rolePermissionSave(request, response);
			
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
	} // rolePermissionSave

}
