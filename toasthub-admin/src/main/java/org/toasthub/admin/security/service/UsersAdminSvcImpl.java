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
import org.toasthub.admin.security.repository.UsersAdminDao;
import org.toasthub.core.general.handler.ServiceProcessor;
import org.toasthub.core.general.model.GlobalConstant;
import org.toasthub.core.general.service.UtilSvc;
import org.toasthub.core.preference.model.AppCachePageUtil;
import org.toasthub.security.model.User;
import org.toasthub.security.service.UsersSvcImpl;
import org.toasthub.core.general.model.RestRequest;
import org.toasthub.core.general.model.RestResponse;


@Service("UsersAdminSvc")
public class UsersAdminSvcImpl extends UsersSvcImpl implements ServiceProcessor, UsersAdminSvc {

	@Autowired 
	@Qualifier("UsersAdminDao")
	UsersAdminDao usersAdminDao;
	
	@Autowired 
	UtilSvc utilSvc;
	
	@Autowired 
	AppCachePageUtil appCachePageUtil;

	public void process(RestRequest request, RestResponse response) {
		String action = (String) request.getParams().get(GlobalConstant.ACTION);
		
		Long count = 0l;
		switch (action) {
		case "INIT":
			this.initParams(request);
			request.addParam("appPageParamLoc", "response");
			appCachePageUtil.getPageInfo(request,response);
			this.itemCount(request, response);
			count = (Long) response.getParam(GlobalConstant.ITEMCOUNT);
			this.itemColumns(request, response);
			if (count != null && count > 0){
				this.items(request, response);
			}
			response.addParam(GlobalConstant.ITEMNAME, request.getParam(GlobalConstant.ITEMNAME));
			break;
		case "LIST":
			this.initParams(request);
			request.addParam("appPageParamLoc", "response");
			appCachePageUtil.getPageInfo(request,response);
			this.itemCount(request, response);
			count = (Long) response.getParam(GlobalConstant.ITEMCOUNT);
			this.itemColumns(request, response);
			if (count != null && count > 0){
				this.items(request, response);
			}
			response.addParam(GlobalConstant.ITEMNAME, request.getParam(GlobalConstant.ITEMNAME));
			break;
		case "SHOW":
			this.item(request, response);
			break;
		case "EDIT":
			// get form info
			appCachePageUtil.getPageInfo(request, response);
			// get item info
			this.item(request, response);
			break;
		case "DISABLE":
			this.disable(request, response);
			break;
		case "DELETE":
			this.delete(request, response);
			break;
		case "SAVE":
			appCachePageUtil.getPageInfo(request,response);
			this.save(request, response);
			break;
		case "SAVE_ROLE":
			this.saveRole(request, response);
			break;
		case "DELETE_ROLE":
			this.deleteRole(request, response);
			break;
		default:
			utilSvc.addStatus(RestResponse.INFO, RestResponse.ACTIONNOTEXIST, "Action not available", response);
			break;
		}
		
		
	}
	//@Authorize
	public void disable(RestRequest request, RestResponse response) {
		try {
			usersAdminDao.disable(request, response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//@Authorize
	@Override
	public void delete(RestRequest request, RestResponse response) {
		try{
			// prechecks
			if (request.containsParam(GlobalConstant.ITEMID)){
				this.item(request, response);
				// prevent deleting of admin unless special code is added
				User user = (User)response.getParam(GlobalConstant.ITEM);
				if (user != null && !user.getUsername().equals("cborgadmin")){
					try {
						request.addParam("username", user.getUsername());
						usersAdminDao.delete(request, response);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					response.addParam(GlobalConstant.ITEM, null);
					utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "This account can not me deleted", response);
					return;
				}
				response.addParam(GlobalConstant.ITEM, null);
				utilSvc.addStatus(RestResponse.INFO, RestResponse.SUCCESS, "Delete Successful", response);
			} else {
				utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "You must provide a username!", response);
			}
		} catch (Exception e) {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Delete Failed", response);
			e.printStackTrace();
		}
	}

	//@Authorize
	@Override
	public void save(RestRequest request, RestResponse response) {
		try{
			// validate
			utilSvc.validateParams(request, response);
			
			if ((Boolean) request.getParam(GlobalConstant.VALID) == false) {
				utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Validation Error", response);
				return;
			}
			// get existing item
			if (request.containsParam(GlobalConstant.ITEMID) && request.getParam(GlobalConstant.ITEMID) != null && !request.getParam(GlobalConstant.ITEMID).equals("")) {
				usersAdminDao.item(request, response);
				request.addParam(GlobalConstant.ITEM, response.getParam(GlobalConstant.ITEM));
				response.getParams().remove(GlobalConstant.ITEM);
			} else {
				request.addParam(GlobalConstant.ITEM, new User());
			}
			// marshall
			utilSvc.marshallFields(request, response);
			
			// save
			usersAdminDao.save(request, response);
	
			// reset cache
			//serviceCrawler.clearCache();
			
			utilSvc.addStatus(RestResponse.INFO, RestResponse.SUCCESS, "Save Successful", response);
		} catch (Exception e) {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Save Failed", response);
			e.printStackTrace();
		}
		
	}
	
	//@Authorize
	@Override
	public void saveRole(RestRequest request, RestResponse response) {
		try {
			
			// get existing item
			if (request.containsParam(GlobalConstant.ITEMID) && !request.getParam(GlobalConstant.ITEMID).equals("")) {
				
				usersAdminDao.saveRole(request, response);
			}
			
			utilSvc.addStatus(RestResponse.INFO, RestResponse.SUCCESS, "Save Successful", response);
		} catch (Exception e) {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Save Failed", response);
			e.printStackTrace();
		}
		
	}
	
	//@Authorize
	@Override
	public void deleteRole(RestRequest request, RestResponse response) {
		try {
			
			// get existing item
			if (request.containsParam(GlobalConstant.ITEMID) && !request.getParam(GlobalConstant.ITEMID).equals("")) {
				
				usersAdminDao.deleteRole(request, response);
			}
			
			utilSvc.addStatus(RestResponse.INFO, RestResponse.SUCCESS, "Delete Successful", response);
		} catch (Exception e) {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Delete Failed", response);
			e.printStackTrace();
		}
		
	}
	
}
