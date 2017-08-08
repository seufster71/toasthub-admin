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

package org.toasthub.admin.service;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.toasthub.admin.repository.MenuAdminDao;
import org.toasthub.core.general.handler.ServiceProcessor;
import org.toasthub.core.general.model.AppCacheMenu;
import org.toasthub.core.general.model.BaseEntity;
import org.toasthub.core.general.model.Menu;
import org.toasthub.core.general.model.MenuItem;
import org.toasthub.core.general.model.RestRequest;
import org.toasthub.core.general.model.RestResponse;
import org.toasthub.core.general.service.MenuSvcImpl;
import org.toasthub.core.general.service.UtilSvc;

@Service("MenuAdminSvc")
public class MenuAdminSvcImpl extends MenuSvcImpl implements ServiceProcessor, MenuAdminSvc {

	@Autowired 
	@Qualifier("MenuAdminDao")
	MenuAdminDao menuAdminDao;
	
	@Autowired 
	AppCacheMenu appCacheMenu;
	
	@Autowired 
	UtilSvc utilSvc;
	
	// Processor
	@Override
	public void process(RestRequest request, RestResponse response) {
		String action = (String) request.getParams().get(BaseEntity.ACTION);
		
		Long count = 0l;
		switch (action) {
		case "INIT": 
			request.addParam("appPageParamLoc", "response");
			appCachePage.getPageInfo(request,response);
			//appCachePage.getGlobalInfo(request,response);
			
			this.initParams(request);
			this.itemColumns(request, response);
			request.addParam(BaseEntity.SHOWALL, true);
			this.getMenuCount(request, response);
			count = (Long) response.getParam(BaseEntity.ITEMCOUNT);
			if (count != null && count > 0){
				this.getMenus(request, response);
			}
			
			break;
		case "LIST":
			request.addParam("appPageParamLoc", "response");
			appCachePage.getPageInfo(request,response);
			
			this.initParams(request);
			this.itemColumns(request, response);
			request.addParam(BaseEntity.SHOWALL, true);
			this.getMenuCount(request, response);
			count = (Long) response.getParam(BaseEntity.ITEMCOUNT);
			if (count != null && count > 0){
				this.getMenus(request, response);
			}
			response.addParam(BaseEntity.PARENTID, request.getParam(BaseEntity.PARENTID));
			break;
		case "LIST_MENUITEMS":
			
			this.initParams(request);
		
			this.getMenuItemCount(request, response);
			count = (Long) response.getParam(BaseEntity.ITEMCOUNT);
			if (count != null && count > 0){
				this.getMenuItems(request, response);
			}
			if (request.containsParam(BaseEntity.PARENTID)){
				response.addParam(BaseEntity.PARENTID, request.getParam(BaseEntity.PARENTID));
			} else {
				response.addParam(BaseEntity.PARENTID, request.getParam(Menu.ID));
			}
			break;
		case "SHOW":
			this.getMenu(request, response);
			if (request.containsParam(BaseEntity.PARENTID)){
				response.addParam(BaseEntity.PARENTID,(Integer) request.getParam(BaseEntity.PARENTID));
			}
			response.addParam(Menu.ID,(Integer) request.getParam(Menu.ID));
			response.addParam(BaseEntity.ID,request.getParam(BaseEntity.ID));
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
	
	@Override
	public void delete(RestRequest request, RestResponse response) {
		try {
			menuAdminDao.delete(request, response);
			// reset cache
			appCacheMenu.reloadMenuCache();
			utilSvc.addStatus(RestResponse.INFO, RestResponse.SUCCESS, "Menu Delete Successful", response);
		//} catch (PrivilegesException pe){
		//	utilSvc.addStatus(RestResponse.ERROR, RestResponse.SERVERERROR, pe.getMessage(), response);
		} catch (Exception e) {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.EXECUTIONFAILED, "Menu Delete Failed", response);
			e.printStackTrace();
		}
	}

	@Override
	public void save(RestRequest request, RestResponse response) {
		try {
			if ( !request.containsParam(BaseEntity.ITEMTYPE) ){
				request.addParam(BaseEntity.ITEMTYPE, "menu");
				if (!request.containsParam("appForms")) {
					request.addParam("appForms", new ArrayList<String>(Arrays.asList("APP_MENU_FORM")));
				}
			}
			
			// validate
			utilSvc.validateParams(request, response);
			
			if ((Boolean) request.getParam(BaseEntity.VALID) == false) {
				utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Validation Error", response);
				return;
			}
			
			// get existing item
			if (request.containsParam(BaseEntity.ITEMID) && !request.getParam(BaseEntity.ITEMID).equals("")) {
				menuAdminDao.item(request, response);
				request.addParam(BaseEntity.ITEM, response.getParam(BaseEntity.ITEM));
				response.getParams().remove(BaseEntity.ITEM);
			} else {
				if ("subItem".equals(request.getParam(BaseEntity.ITEMTYPE)) || "subSub".equals(request.getParam(BaseEntity.ITEMTYPE))){
					MenuItem menuItem = new MenuItem();
					menuItem.setArchive(false);
					menuItem.setLocked(false);
					request.addParam(BaseEntity.ITEM, menuItem);
				} else {
					Menu menu = new Menu();
					menu.setArchive(false);
					menu.setLocked(false);
					request.addParam(BaseEntity.ITEM, menu);
				}
			}
			// marshall
			utilSvc.marshallFields(request, response);
			
			// save
			menuAdminDao.save(request, response);
			
			// reset cache
			appCacheMenu.reloadMenuCache();
			
			utilSvc.addStatus(RestResponse.INFO, RestResponse.SUCCESS, "Menu Save Successful", response);
		//} catch (PrivilegesException pe){
		//	utilSvc.addStatus(RestResponse.ERROR, RestResponse.SERVERERROR, pe.getMessage(), response);
		} catch (Exception e) {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.EXECUTIONFAILED, "Menu Save Failed", response);
			e.printStackTrace();
		}
	}
	
}
