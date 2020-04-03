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
import org.toasthub.core.preference.model.PrefFormFieldName;
import org.toasthub.core.preference.model.PrefName;
import org.toasthub.core.preference.repository.PrefFormFieldDaoImpl;

@Repository("PrefFormFieldAdminDao")
@Transactional("TransactionManagerData")
public class PrefFormFieldAdminDaoImpl extends PrefFormFieldDaoImpl implements PrefFormFieldAdminDao {


	@Override
	public void save(RestRequest request, RestResponse response) throws Exception {
		PrefFormFieldName prefFormFieldName = (PrefFormFieldName) request.getParam(GlobalConstant.ITEM);
		if (prefFormFieldName.getPrefName() == null) {
			PrefName prefName = (PrefName) entityManagerDataSvc.getInstance().getReference(PrefName.class, new Long((Integer) request.getParam("parentId")));
			prefFormFieldName.setPrefName(prefName);
		}
		entityManagerDataSvc.getInstance().merge(prefFormFieldName);
	}

	@Override
	public void delete(RestRequest request, RestResponse response) throws Exception {
		if (request.containsParam(GlobalConstant.ITEMID) && !"".equals(request.getParam(GlobalConstant.ITEMID))) {
			PrefFormFieldName prefFormFieldName = (PrefFormFieldName) entityManagerDataSvc.getInstance().getReference(PrefFormFieldName.class, new Long((Integer) request.getParam(GlobalConstant.ITEMID)));
			entityManagerDataSvc.getInstance().remove(prefFormFieldName);
			
		} else {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Missing ID", response);
		}
	}
}
