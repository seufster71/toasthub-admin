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


import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.toasthub.core.general.model.GlobalConstant;
import org.toasthub.core.general.model.Menu;
import org.toasthub.core.general.model.MenuItem;
import org.toasthub.core.general.model.RestRequest;
import org.toasthub.core.general.model.RestResponse;
import org.toasthub.core.menu.MenuDaoImpl;

@Repository("MenuAdminDao")
@Transactional("TransactionManagerData")
public class MenuAdminDaoImpl extends MenuDaoImpl implements MenuAdminDao {
	
	//@Authorize
	public void save(RestRequest request, RestResponse response) throws Exception {
		Menu menu = (Menu) request.getParam(GlobalConstant.ITEM);
		entityManagerDataSvc.getInstance().merge(menu);
	}

	//@Authorize
	public void delete(RestRequest request, RestResponse response) throws Exception {
		if (request.containsParam(GlobalConstant.ITEMID) && !"".equals(request.getParam(GlobalConstant.ITEMID))) {
			Menu menu = (Menu) entityManagerDataSvc.getInstance().getReference(Menu.class, new Long((Integer) request.getParam(GlobalConstant.ITEMID)));
			entityManagerDataSvc.getInstance().remove(menu);
		} else {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Missing ID", response);
		}
		
	}
	
	public void saveSubItem(RestRequest request, RestResponse response) throws Exception {
		if ("subItem".equals(request.getParam(GlobalConstant.ITEMTYPE))) {
			// menu reference
			MenuItem menuItem = (MenuItem) request.getParam(GlobalConstant.ITEM);
			if (menuItem.getMenu() == null) {
				Menu menu = (Menu) entityManagerDataSvc.getInstance().getReference(Menu.class, new Long((Integer) request.getParam(GlobalConstant.PARENTID)));
				menuItem.setMenu(menu);
			}
			entityManagerDataSvc.getInstance().merge(menuItem);
		} else if ("subSub".equals(request.getParam(GlobalConstant.ITEMTYPE))) {
			MenuItem menuItem = (MenuItem) request.getParam(GlobalConstant.ITEM);
			if (menuItem.getParent() == null) {
				MenuItem p = (MenuItem) entityManagerDataSvc.getInstance().getReference(MenuItem.class, new Long((Integer) request.getParam(GlobalConstant.PARENTID)));
				menuItem.setParent(p);
				menuItem.setMenu(p.getMenu());
			}
			entityManagerDataSvc.getInstance().merge(menuItem);
		} else {
			Menu menu = (Menu) request.getParam(GlobalConstant.ITEM);
			entityManagerDataSvc.getInstance().merge(menu);
		}
	}
	
	public void deleteSubItem(RestRequest request, RestResponse response) throws Exception {
		if (request.containsParam(GlobalConstant.ITEMID) && !"".equals(request.getParam(GlobalConstant.ITEMID))) {
			MenuItem menuItem = (MenuItem) entityManagerDataSvc.getInstance().getReference(MenuItem.class, new Long((Integer) request.getParam(GlobalConstant.ITEMID)));
			entityManagerDataSvc.getInstance().remove(menuItem);
		} else {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Missing ID", response);
		}
		
	}
}
