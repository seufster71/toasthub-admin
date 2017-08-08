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
import org.toasthub.core.general.model.BaseEntity;
import org.toasthub.core.general.model.RestRequest;
import org.toasthub.core.general.model.RestResponse;
import org.toasthub.core.preference.model.AppPageLabelName;
import org.toasthub.core.preference.model.AppPageName;
import org.toasthub.core.preference.repository.AppLabelDaoImpl;

@Repository("AppLabelAdminDao")
@Transactional("TransactionManagerData")
public class AppLabelAdminDaoImpl extends AppLabelDaoImpl implements AppLabelAdminDao {

	@Override
	public void save(RestRequest request, RestResponse response) throws Exception {
		AppPageLabelName appPageLabelName = (AppPageLabelName) request.getParam(BaseEntity.ITEM);
		if (appPageLabelName.getPageName() == null) {
			AppPageName appPageName = (AppPageName) entityManagerDataSvc.getInstance().getReference(AppPageName.class, new Long((Integer) request.getParam("parentId")));
			appPageLabelName.setPageName(appPageName);
		}
		entityManagerDataSvc.getInstance().merge(appPageLabelName);
	}
	
	@Override
	public void delete(RestRequest request, RestResponse response) throws Exception {
		if (request.containsParam(BaseEntity.ITEMID) && !"".equals(request.getParam(BaseEntity.ITEMID))) {
			AppPageLabelName appPageLabelName = (AppPageLabelName) entityManagerDataSvc.getInstance().getReference(AppPageLabelName.class, new Long((Integer) request.getParam(BaseEntity.ITEMID)));
			entityManagerDataSvc.getInstance().remove(appPageLabelName);
			
		} else {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Missing ID", response);
		}
	}
}
