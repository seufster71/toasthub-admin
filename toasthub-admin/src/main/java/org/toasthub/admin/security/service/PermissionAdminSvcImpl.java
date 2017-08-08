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

package org.toasthub.admin.security.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.toasthub.admin.security.repository.PermissionAdminDao;
import org.toasthub.core.general.handler.ServiceProcessor;
import org.toasthub.core.general.model.BaseEntity;
import org.toasthub.core.general.model.RestRequest;
import org.toasthub.core.general.model.RestResponse;
import org.toasthub.core.general.service.UtilSvc;
import org.toasthub.core.preference.model.AppCachePage;
import org.toasthub.security.model.Permission;
import org.toasthub.security.service.PermissionSvcImpl;

@Service("PermissionAdminSvc")
public class PermissionAdminSvcImpl extends PermissionSvcImpl implements ServiceProcessor, PermissionAdminSvc {

	@Autowired 
	@Qualifier("PermissionAdminDao")
	PermissionAdminDao permissionAdminDao;
	
	@Autowired 
	AppCachePage appCachePage;
	
	@Autowired 
	UtilSvc utilSvc;
	
	@Override
	public void process(RestRequest request, RestResponse response) {
		String action = (String) request.getParams().get(BaseEntity.ACTION);
		
		Long count = 0l;
		switch (action) {
		case "INIT":
			request.addParam("appPageParamLoc", "response");
			appCachePage.getPageInfo(request,response);
			this.itemCount(request, response);
			count = (Long) response.getParam(BaseEntity.ITEMCOUNT);
			if (count != null && count > 0){
				items(request, response);
			}
			response.addParam(BaseEntity.ITEMNAME, request.getParam(BaseEntity.ITEMNAME));
			break;
		case "LIST":
			request.addParam("appPageParamLoc", "response");
			appCachePage.getPageInfo(request,response);
			this.itemCount(request, response);
			count = (Long) response.getParam(BaseEntity.ITEMCOUNT);
			if (count != null && count > 0){
				this.items(request, response);
			}
			if (request.containsParam("roleId")){
				this.rolePermissionIds(request,response);
			}
			response.addParam(BaseEntity.ITEMNAME, request.getParam(BaseEntity.ITEMNAME));
			break;
		case "SHOW":
			this.item(request, response);
			break;
		case "DELETE":
			this.delete(request, response);
			break;
		case "SAVE":
			appCachePage.getPageInfo(request,response);
			this.save(request, response);
			break;
		default:
			utilSvc.addStatus(RestResponse.INFO, RestResponse.ACTIONNOTEXIST, "Action not available", response);
			break;
		}
		
	}

	
	//@Authorize
	public void delete(RestRequest request, RestResponse response) {
		try {
			permissionAdminDao.delete(request, response);
			utilSvc.addStatus(RestResponse.INFO, RestResponse.SUCCESS, "Delete Successful", response);
		} catch (Exception e) {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Delete Failed", response);
			e.printStackTrace();
		}
	} // delete
	
	//@Authorize
	public void save(RestRequest request, RestResponse response) {
		try {
			// validate
			utilSvc.validateParams(request, response);
			
			if ((Boolean) request.getParam(BaseEntity.VALID) == false) {
				utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Validation Error", response);
				return;
			}
			// get existing item
			if (request.containsParam(BaseEntity.ITEMID) && request.getParam(BaseEntity.ITEMID) != null && !request.getParam(BaseEntity.ITEMID).equals("")) {
				permissionAdminDao.item(request, response);
				request.addParam(BaseEntity.ITEM, response.getParam(BaseEntity.ITEM));
				response.getParams().remove(BaseEntity.ITEM);
			} else {
				Permission permission = new Permission();
				permission.setArchive(false);
				permission.setLocked(false);
				
				request.addParam(BaseEntity.ITEM, permission);
			}
			
			// marshall
			utilSvc.marshallFields(request, response);
			
			// save
			permissionAdminDao.save(request, response);
			
			utilSvc.addStatus(RestResponse.INFO, RestResponse.SUCCESS, "Save Successful", response);
		} catch (Exception e) {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Save Failed", response);
			e.printStackTrace();
		}
	} // save


	@Override
	public void rolePermissionIds(RestRequest request, RestResponse response) {
		try {
			permissionAdminDao.rolePermissionIds(request, response);
		} catch (Exception e) {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Item failed", response);
			e.printStackTrace();
		}
	}
}
