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

package org.toasthub.admin.general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.toasthub.admin.menu.MenuAdminSvc;
import org.toasthub.core.common.EntityManagerMainSvc;
import org.toasthub.core.common.UtilSvc;
import org.toasthub.core.general.handler.ServiceProcessor;
import org.toasthub.core.general.model.AppCacheMenuUtil;
import org.toasthub.core.general.model.GlobalConstant;
import org.toasthub.core.general.model.MenuItem;
import org.toasthub.core.general.model.RestRequest;
import org.toasthub.core.general.model.RestResponse;
import org.toasthub.core.preference.model.AppCachePageUtil;
import org.toasthub.security.model.User;
import org.toasthub.security.model.UserContext;

@Service("AdminSvc")
public class AdminSvcImpl implements ServiceProcessor, AdminSvc {
	
	public static final String service = "ADMIN_SVC";
	
	@Autowired 
	UtilSvc utilSvc;
	
	@Autowired 
	EntityManagerMainSvc entityManagerMainSvc;
	
	@Autowired 
	AppCacheMenuUtil appCacheMenuUtil;
	
	@Autowired 
	@Qualifier("MenuAdminSvc")
	MenuAdminSvc menuAdminSvc;
	
	@Autowired
	AppCachePageUtil appCachePageUtil;
	
	@Autowired 
	UserContext userContext;

	// Constructors
	public AdminSvcImpl() {}
	
	// Processor
	public void process(RestRequest request, RestResponse response) {
		String action = (String) request.getParams().get(GlobalConstant.ACTION);
		
		this.setupDefaults(request);
		User user = null;
		String name = "";
		if (userContext != null && userContext.getCurrentUser() != null){
			user = userContext.getCurrentUser();
		} else {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "User is not authenticated", response);
		}
		
		switch (action) {
		case "INIT":
			request.addParam("appPageParamLoc", "response");
			appCachePageUtil.getPageInfo(request,response);
			
			// get menus
			if (request.containsParam(GlobalConstant.MENUNAMES)){
				this.initMenu(request, response);
			}
			response.addParam("USER", user);
			break;
		case "INIT_MENU":
			this.setMenuDefaults(request);
			this.initMenu(request, response);
			break;
		default:
			break;
		}
		
	
	}
	
	protected void initMenu(RestRequest request, RestResponse response){
		List<MenuItem> menu = null;
		Map<String,List<MenuItem>> menuList = new HashMap<String,List<MenuItem>>();
		
		ArrayList<String> mylist = (ArrayList<String>) request.getParam(GlobalConstant.MENUNAMES);
		for (String menuName : mylist) {
			menu = appCacheMenuUtil.getMenu(menuName,(String)request.getParam(GlobalConstant.MENUAPIVERSION),(String)request.getParam(GlobalConstant.MENUAPPVERSION),(String)request.getParam(GlobalConstant.LANG));
			menuList.put(menuName, menu);
		}
		
		String name = "";
		if (userContext != null && userContext.getCurrentUser() != null){
			name = name.concat(userContext.getCurrentUser().getFirstname());
			name = name.concat(" ").concat(userContext.getCurrentUser().getLastname());
		}
		response.addParam("username", name);
		
		if (!menuList.isEmpty()){
			response.addParam(RestResponse.MENUS, menuList);
		} else {
			utilSvc.addStatus(RestResponse.INFO, RestResponse.SUCCESS, "Menu Issue", response);
		}
	}
	
	protected void setupDefaults(RestRequest request){
		
		if (!request.containsParam(GlobalConstant.MENUAPIVERSION)){
			request.addParam(GlobalConstant.MENUAPIVERSION, "1.0");
		}

		if (!request.containsParam(GlobalConstant.MENUAPPVERSION)){
			request.addParam(GlobalConstant.MENUAPPVERSION, "1.0");
		}
		
	}
	
	protected void setMenuDefaults(RestRequest request){
		if (!request.containsParam(GlobalConstant.MENUNAMES)){
			ArrayList<String> myList = new ArrayList<String>();
			myList.add("ADMIN_MENU_LEFT");
			myList.add("ADMIN_MENU_RIGHT");
			request.addParam(GlobalConstant.MENUNAMES, myList);
		}
	}
}