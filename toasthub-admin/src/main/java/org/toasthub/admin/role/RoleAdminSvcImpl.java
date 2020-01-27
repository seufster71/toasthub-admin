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
import org.springframework.stereotype.Service;
import org.toasthub.core.common.UtilSvc;
import org.toasthub.core.general.handler.ServiceProcessor;
import org.toasthub.core.general.model.GlobalConstant;
import org.toasthub.core.general.model.RestRequest;
import org.toasthub.core.general.model.RestResponse;
import org.toasthub.core.preference.model.AppCachePageUtil;
import org.toasthub.security.application.ApplicationSvc;
import org.toasthub.security.model.Role;
import org.toasthub.security.role.RoleSvcImpl;

@Service("RoleAdminSvc")
public class RoleAdminSvcImpl extends RoleSvcImpl implements ServiceProcessor, RoleAdminSvc {

	@Autowired 
	@Qualifier("RoleAdminDao")
	RoleAdminDao roleAdminDao;
	
	@Autowired 
	AppCachePageUtil appCachePageUtil;
	
	@Autowired 
	UtilSvc utilSvc;
	
	@Autowired
	ApplicationSvc applicationSvc;
	
	@Override
	public void process(RestRequest request, RestResponse response) {
		String action = (String) request.getParams().get(GlobalConstant.ACTION);
		
		Long count = 0l;
		switch (action) {
		case "INIT":
			request.addParam(AppCachePageUtil.APPPAGEPARAMLOC, AppCachePageUtil.RESPONSE);
			appCachePageUtil.getPageInfo(request,response);
			this.itemCount(request, response);
			count = (Long) response.getParam(GlobalConstant.ITEMCOUNT);
			if (count != null && count > 0){
				this.items(request, response);
			}
			break;
		case "LIST":
			request.addParam(AppCachePageUtil.APPPAGEPARAMLOC, AppCachePageUtil.RESPONSE);
			appCachePageUtil.getPageInfo(request,response);
			this.itemCount(request, response);
			count = (Long) response.getParam(GlobalConstant.ITEMCOUNT);
			if (count != null && count > 0){
				this.items(request, response);
			}
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
				List<String> forms =  new ArrayList<String>(Arrays.asList("ADMIN_ROLE_FORM"));
				request.addParam("appForms", forms);
			}
			List<String> global =  new ArrayList<String>(Arrays.asList("LANGUAGES"));
			request.addParam("appGlobal", global);
			appCachePageUtil.getPageInfo(request,response);
			this.save(request, response);
			break;
		case "SAVE_PERMISSION":
			this.savePermission(request,response);
			break;
		case "DELETE_PERMISSION":
			this.deletePermission(request,response);
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
			Map<String,Object> inputList = (Map<String, Object>) request.getParam("inputFields");
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
		} catch (Exception e) {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Save Failed", response);
			e.printStackTrace();
		}
	}
	
	//@Authorize
	@Override
	public void savePermission(RestRequest request, RestResponse response) {
		try {
			
			// get existing item
			if (request.containsParam(GlobalConstant.ITEMID) && !request.getParam(GlobalConstant.ITEMID).equals("")) {
				
				roleAdminDao.savePermission(request, response);
			}
			
			
			utilSvc.addStatus(RestResponse.INFO, RestResponse.SUCCESS, "Save Successful", response);
		} catch (Exception e) {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Save Failed", response);
			e.printStackTrace();
		}
	}
	
	//@Authorize
	@Override
	public void deletePermission(RestRequest request, RestResponse response) {
		try {
			
			// get existing item
			if (request.containsParam(GlobalConstant.ITEMID) && !request.getParam(GlobalConstant.ITEMID).equals("")) {
				
				roleAdminDao.deletePermission(request, response);
			}
			
			
			utilSvc.addStatus(RestResponse.INFO, RestResponse.SUCCESS, "Delete Successful", response);
		} catch (Exception e) {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Delete Failed", response);
			e.printStackTrace();
		}
	}

	@Override
	public void userRoleIds(RestRequest request, RestResponse response) {
		try {
			roleAdminDao.userRoleIds(request, response);
		} catch (Exception e) {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Item failed", response);
			e.printStackTrace();
		}
		
	}
}
