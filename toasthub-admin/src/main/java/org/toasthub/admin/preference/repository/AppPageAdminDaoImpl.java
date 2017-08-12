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

package org.toasthub.admin.preference.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.toasthub.core.general.model.GlobalConstant;
import org.toasthub.core.general.model.RestRequest;
import org.toasthub.core.general.model.RestResponse;
import org.toasthub.core.preference.model.AppPageName;
import org.toasthub.core.preference.repository.AppPageDaoImpl;

@Repository("AppPageAdminDao")
@Transactional("TransactionManagerData")
public class AppPageAdminDaoImpl extends AppPageDaoImpl implements AppPageAdminDao {
	
	@Override
	public void save(RestRequest request, RestResponse response) throws Exception {
		AppPageName appPageName = (AppPageName) request.getParam(GlobalConstant.ITEM);
		entityManagerDataSvc.getInstance().merge(appPageName);
	}

	@Override
	public void delete(RestRequest request, RestResponse response) throws Exception {
		if (request.containsParam(GlobalConstant.ITEMID) && !"".equals(request.getParam(GlobalConstant.ITEMID))) {
			AppPageName appPageName = (AppPageName) entityManagerDataSvc.getInstance().getReference(AppPageName.class, new Long((Integer) request.getParam(GlobalConstant.ITEMID)));
			entityManagerDataSvc.getInstance().remove(appPageName);
		
		} else {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Missing ID", response);
		}
	}
}
