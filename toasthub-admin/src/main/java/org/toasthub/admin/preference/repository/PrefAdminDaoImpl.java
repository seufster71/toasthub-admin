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
import org.toasthub.core.preference.model.PrefName;
import org.toasthub.core.preference.model.PrefProduct;
import org.toasthub.core.preference.repository.PrefDaoImpl;

@Repository("PrefAdminDao")
@Transactional("TransactionManagerData")
public class PrefAdminDaoImpl extends PrefDaoImpl implements PrefAdminDao {
	
	@Override
	public void save(RestRequest request, RestResponse response) throws Exception {
		PrefName prefName = (PrefName) request.getParam(GlobalConstant.ITEM);
		if (request.containsParam("prefProductId") && !"".equals(request.getParam("prefProductId"))) {
			PrefProduct prefProduct = (PrefProduct) entityManagerDataSvc.getInstance().getReference(PrefProduct.class, request.getParamLong("prefProductId"));
		} else if (prefName.getPrefProduct() != null && prefName.getPrefProduct().getId().equals(prefName.getPrefProductId())) {
			prefName.setPrefProduct(null);
			PrefProduct prefProduct = (PrefProduct) entityManagerDataSvc.getInstance().getReference(PrefProduct.class, prefName.getPrefProductId());
			prefName.setPrefProduct(prefProduct);
		} else if (prefName.getPrefProduct() == null) {
			PrefProduct prefProduct = (PrefProduct) entityManagerDataSvc.getInstance().createQuery("FROM PrefProduct where productCode = :code").setParameter("code","GLOBAL").getSingleResult();
			prefName.setPrefProduct(prefProduct);
		}
		
		entityManagerDataSvc.getInstance().merge(prefName);
	}

	@Override
	public void delete(RestRequest request, RestResponse response) throws Exception {
		if (request.containsParam(GlobalConstant.ITEMID) && !"".equals(request.getParam(GlobalConstant.ITEMID))) {
			PrefName prefName = (PrefName) entityManagerDataSvc.getInstance().getReference(PrefName.class, request.getParamLong(GlobalConstant.ITEMID));
			entityManagerDataSvc.getInstance().remove(prefName);
		
		} else {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Missing ID", response);
		}
	}
}
