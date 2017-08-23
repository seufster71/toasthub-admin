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

package org.toasthub.admin.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.toasthub.core.common.UtilSvc;
import org.toasthub.core.general.handler.ServiceProcessor;
import org.toasthub.core.general.model.GlobalConstant;
import org.toasthub.core.general.model.RestRequest;
import org.toasthub.core.general.model.RestResponse;
import org.toasthub.core.preference.model.AppCachePageUtil;
import org.toasthub.security.model.Application;
import org.toasthub.security.service.ApplicationSvcImpl;

@Service("ApplicationAdminSvc")
public class ApplicationAdminSvcImpl extends ApplicationSvcImpl implements ServiceProcessor, ApplicationAdminSvc {

	@Autowired 
	@Qualifier("ApplicationAdminDao")
	ApplicationAdminDao applicationAdminDao;
	
	@Autowired 
	AppCachePageUtil appCachePageUtil;
	
	@Autowired 
	UtilSvc utilSvc;
	
	@Override
	public void process(RestRequest request, RestResponse response) {
		String action = (String) request.getParams().get(GlobalConstant.ACTION);
		
		Long count = 0l;
		switch (action) {
		case "INIT":
			request.addParam("appPageParamLoc", "response");
			appCachePageUtil.getPageInfo(request,response);
			this.itemCount(request, response);
			count = (Long) response.getParam(GlobalConstant.ITEMCOUNT);
			if (count != null && count > 0){
				items(request, response);
			}
			response.addParam(GlobalConstant.ITEMNAME, request.getParam(GlobalConstant.ITEMNAME));
			break;
		case "LIST":
			request.addParam("appPageParamLoc", "response");
			appCachePageUtil.getPageInfo(request,response);
			this.itemCount(request, response);
			count = (Long) response.getParam(GlobalConstant.ITEMCOUNT);
			if (count != null && count > 0){
				this.items(request, response);
			}
			response.addParam(GlobalConstant.ITEMNAME, request.getParam(GlobalConstant.ITEMNAME));
			break;
		case "SHOW":
			this.item(request, response);
			break;
		case "DELETE":
			this.delete(request, response);
			break;
		case "SAVE":
			appCachePageUtil.getPageInfo(request,response);
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
			applicationAdminDao.delete(request, response);
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
			
			if ((Boolean) request.getParam(GlobalConstant.VALID) == false) {
				utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Validation Error", response);
				return;
			}
			// get existing item
			if (request.containsParam(GlobalConstant.ITEMID) && !request.getParam(GlobalConstant.ITEMID).equals("")) {
				applicationDao.item(request, response);
				request.addParam(GlobalConstant.ITEM, response.getParam(GlobalConstant.ITEM));
				response.getParams().remove(GlobalConstant.ITEM);
			} else {
				request.addParam(GlobalConstant.ITEM, new Application());
			}
			
			// marshall
			utilSvc.marshallFields(request, response);
			
			// save
			applicationAdminDao.save(request, response);
			
			utilSvc.addStatus(RestResponse.INFO, RestResponse.SUCCESS, "Save Successful", response);
		} catch (Exception e) {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Save Failed", response);
			e.printStackTrace();
		}
	} // save
}
