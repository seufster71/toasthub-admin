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

package org.toasthub.admin.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.toasthub.core.general.handler.ServiceProcessor;
import org.toasthub.core.general.model.AppCacheMenuUtil;
import org.toasthub.core.general.model.GlobalConstant;
import org.toasthub.core.general.model.Menu;
import org.toasthub.core.general.model.MenuItem;
import org.toasthub.core.general.model.RestRequest;
import org.toasthub.core.general.model.RestResponse;
import org.toasthub.core.menu.MenuSvcImpl;
import org.toasthub.core.preference.model.PrefCacheUtil;

@Service("MenuAdminSvc")
public class MenuAdminSvcImpl extends MenuSvcImpl implements ServiceProcessor, MenuAdminSvc {

	@Autowired 
	@Qualifier("MenuAdminDao")
	MenuAdminDao menuAdminDao;
	
	@Autowired 
	AppCacheMenuUtil appCacheMenuUtil;
	
	@Autowired 
	PrefCacheUtil prefCacheUtil;
	
	// Processor
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
			break;
		case "LIST":
			request.addParam(PrefCacheUtil.PREFPARAMLOC, PrefCacheUtil.RESPONSE);
			prefCacheUtil.getPrefInfo(request,response);
			this.itemCount(request, response);
			count = (Long) response.getParam(GlobalConstant.ITEMCOUNT);
			if (count != null && count > 0){
				this.items(request, response);
			}
			break;
		case "ITEM":
			request.addParam(PrefCacheUtil.PREFPARAMLOC, PrefCacheUtil.RESPONSE);
			prefCacheUtil.getPrefInfo(request,response);
			this.item(request, response);
			break;
		case "DELETE":
			this.delete(request, response);
			break;
		case "SAVE":
			if (!request.containsParam(PrefCacheUtil.PREFFORMKEYS)) {
				List<String> forms =  new ArrayList<String>(Arrays.asList("ADMIN_MENU_FORM"));
				request.addParam(PrefCacheUtil.PREFFORMKEYS, forms);
			}
			request.addParam(PrefCacheUtil.PREFGLOBAL, global);
			prefCacheUtil.getPrefInfo(request,response);
			this.save(request, response);
			break;
		case "INIT_SUBITEMS": 
			request.addParam(PrefCacheUtil.PREFPARAMLOC, PrefCacheUtil.RESPONSE);
			prefCacheUtil.getPrefInfo(request,response);
			this.itemCount(request, response);
			count = (Long) response.getParam(GlobalConstant.ITEMCOUNT);
			if (count != null && count > 0){
				this.subItems(request, response);
			}
			break;
		case "LIST_SUBITEMS":
			request.addParam(PrefCacheUtil.PREFPARAMLOC, PrefCacheUtil.RESPONSE);
			prefCacheUtil.getPrefInfo(request,response);
			this.subItemCount(request, response);
			count = (Long) response.getParam(GlobalConstant.ITEMCOUNT);
			if (count != null && count > 0){
				this.subItems(request, response);
			}
			break;
		case "SUBITEM":
			request.addParam(PrefCacheUtil.PREFPARAMLOC, PrefCacheUtil.RESPONSE);
			prefCacheUtil.getPrefInfo(request,response);
			this.subItem(request, response);
			break;
		case "DELETE_SUBITEM":
			this.deleteSubItem(request, response);
			break;
		case "SAVE_SUBITEM":
			if (!request.containsParam(PrefCacheUtil.PREFFORMKEYS)) {
				List<String> forms =  new ArrayList<String>(Arrays.asList("ADMIN_MENU_FORM"));
				request.addParam(PrefCacheUtil.PREFFORMKEYS, forms);
			}
			request.addParam(PrefCacheUtil.PREFGLOBAL, global);
			prefCacheUtil.getPrefInfo(request,response);
			this.saveSubItem(request, response);
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
			appCacheMenuUtil.reloadMenuCache();
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
			// validate
			utilSvc.validateParams(request, response);
			
			if ((Boolean) request.getParam(GlobalConstant.VALID) == false) {
				utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, prefCacheUtil.getPrefText("GLOBAL_SERVICE", "GLOBAL_SERVICE_VALIDATION_ERR",prefCacheUtil.getLang(request)), response);
				return;
			}
			
			// get existing item
			Map<String,Object> inputList = (Map<String, Object>) request.getParam(GlobalConstant.INPUTFIELDS);
			if (inputList.containsKey(GlobalConstant.ITEMID) && inputList.get(GlobalConstant.ITEMID) != null && !"".equals(inputList.get(GlobalConstant.ITEMID))) {
				request.addParam(GlobalConstant.ITEMID, inputList.get(GlobalConstant.ITEMID));
				menuAdminDao.item(request, response);
				request.addParam(GlobalConstant.ITEM, response.getParam(GlobalConstant.ITEM));
				response.getParams().remove(GlobalConstant.ITEM);
			} else {
				Menu menu = new Menu();
				menu.setArchive(false);
				menu.setLocked(false);
				request.addParam(GlobalConstant.ITEM, menu);
			}
			// marshall
			utilSvc.marshallFields(request, response);
			
			// save
			menuAdminDao.save(request, response);
			
			// reset cache
			appCacheMenuUtil.reloadMenuCache();
			
			utilSvc.addStatus(RestResponse.INFO, RestResponse.SUCCESS, prefCacheUtil.getPrefText("GLOBAL_SERVICE", "GLOBAL_SERVICE_SAVE_SUCCESS",prefCacheUtil.getLang(request)), response);
		} catch (Exception e) {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.EXECUTIONFAILED, prefCacheUtil.getPrefText("GLOBAL_SERVICE", "GLOBAL_SERVICE_SAVE_FAIL",prefCacheUtil.getLang(request)), response);
			e.printStackTrace();
		}
	}

	@Override
	public void deleteSubItem(RestRequest request, RestResponse response) {
		try {
			menuAdminDao.deleteSubItem(request, response);
			// reset cache
			appCacheMenuUtil.reloadMenuCache();
			utilSvc.addStatus(RestResponse.INFO, RestResponse.SUCCESS, "Menu Delete Successful", response);
		} catch (Exception e) {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.EXECUTIONFAILED, "Menu Delete Failed", response);
			e.printStackTrace();
		}
	}

	@Override
	public void saveSubItem(RestRequest request, RestResponse response) {
		try {
			// validate
			utilSvc.validateParams(request, response);
			
			if ((Boolean) request.getParam(GlobalConstant.VALID) == false) {
				utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, prefCacheUtil.getPrefText("GLOBAL_SERVICE", "GLOBAL_SERVICE_VALIDATION_ERR",prefCacheUtil.getLang(request)), response);
				return;
			}
			
			// get existing item
			Map<String,Object> inputList = (Map<String, Object>) request.getParam(GlobalConstant.INPUTFIELDS);
			if (inputList.containsKey(GlobalConstant.ITEMID) && inputList.get(GlobalConstant.ITEMID) != null && !"".equals(inputList.get(GlobalConstant.ITEMID))) {
				request.addParam(GlobalConstant.ITEMID, inputList.get(GlobalConstant.ITEMID));
				menuAdminDao.subItem(request, response);
				request.addParam(GlobalConstant.ITEM, response.getParam(GlobalConstant.ITEM));
				response.getParams().remove(GlobalConstant.ITEM);
			} else {
				MenuItem menu = new MenuItem();
				menu.setArchive(false);
				menu.setLocked(false);
				request.addParam(GlobalConstant.ITEM, menu);
			}
			// marshall
			utilSvc.marshallFields(request, response);
			
			// save
			menuAdminDao.saveSubItem(request, response);
			
			// reset cache
			appCacheMenuUtil.reloadMenuCache();
			
			utilSvc.addStatus(RestResponse.INFO, RestResponse.SUCCESS, prefCacheUtil.getPrefText("GLOBAL_SERVICE", "GLOBAL_SERVICE_SAVE_SUCCESS",prefCacheUtil.getLang(request)), response);
		} catch (Exception e) {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.EXECUTIONFAILED, prefCacheUtil.getPrefText("GLOBAL_SERVICE", "GLOBAL_SERVICE_SAVE_FAIL",prefCacheUtil.getLang(request)), response);
			e.printStackTrace();
		}
	}
	
}
