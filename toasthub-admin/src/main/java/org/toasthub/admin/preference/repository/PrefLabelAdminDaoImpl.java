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


import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.toasthub.core.general.model.GlobalConstant;
import org.toasthub.core.general.model.RestRequest;
import org.toasthub.core.general.model.RestResponse;
import org.toasthub.core.preference.model.PrefLabelName;
import org.toasthub.core.preference.model.PrefName;
import org.toasthub.core.preference.repository.PrefLabelDaoImpl;

@Repository("PrefLabelAdminDao")
@Transactional("TransactionManagerMember")
public class PrefLabelAdminDaoImpl extends PrefLabelDaoImpl implements PrefLabelAdminDao {

	@Override
	public void save(RestRequest request, RestResponse response) throws Exception {
		PrefLabelName prefLabelName = (PrefLabelName) request.getParam(GlobalConstant.ITEM);
		if (prefLabelName.getPrefName() == null) {
			// get highest order
			Object max = entityManagerSvc.getInstance().createQuery("SELECT max(x.sortOrder) FROM PrefLabelName AS x WHERE x.prefName.id =:parentId ")
					.setParameter("parentId", request.getParamLong(GlobalConstant.PARENTID)).getSingleResult();
			if (max != null) {
				int order = (int) max + 1;
				prefLabelName.setSortOrder(order);
			} else {
				prefLabelName.setSortOrder(1);
			}
			PrefName prefName = (PrefName) entityManagerSvc.getInstance().getReference(PrefName.class, request.getParamLong(GlobalConstant.PARENTID));
			prefLabelName.setPrefName(prefName);
		}
		entityManagerSvc.getInstance().merge(prefLabelName);
	}
	
	@Override
	public void delete(RestRequest request, RestResponse response) throws Exception {
		if (request.containsParam(GlobalConstant.ITEMID) && !"".equals(request.getParam(GlobalConstant.ITEMID))) {
			PrefLabelName prefLabelName = (PrefLabelName) entityManagerSvc.getInstance().getReference(PrefLabelName.class, request.getParamLong(GlobalConstant.ITEMID));
			entityManagerSvc.getInstance().remove(prefLabelName);
			
		} else {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, "Missing ID", response);
		}
	}

	@Override
	public void moveSave(RestRequest request, RestResponse response) {
		// get list of id and current order
		List<Long> list = entityManagerSvc.getInstance().createQuery("SELECT x.id FROM PrefLabelName AS x WHERE x.prefName.id =:parentId ORDER BY x.sortOrder")
				.setParameter("parentId", request.getParamLong(GlobalConstant.PARENTID)).getResultList();
		
		// update order
		Long moveSelectedItemId = request.getParamLong(GlobalConstant.MOVESELECTEDITEMID);
		Long itemId = request.getParamLong(GlobalConstant.ITEMID);
		List<Long> updatedList = new ArrayList<Long>();
		for(Long item : list) {
			if ( item.equals(itemId) ){
				if ("MOVEABOVE".equals(request.getParam(GlobalConstant.CODE))) {
					updatedList.add(moveSelectedItemId);
					updatedList.add(item);
				} else if ("MOVEBELOW".equals(request.getParam(GlobalConstant.CODE))) {
					updatedList.add(item);
					updatedList.add(moveSelectedItemId);
				}
			} else if (item.equals(moveSelectedItemId) ) {
				// do nothing
			} else {
				updatedList.add(item);
			}
		}
		
		// save orderr
		int count = 1;
		for (Long item : updatedList) {
			entityManagerSvc.getInstance().createQuery("UPDATE PrefLabelName set sortOrder =:orderNum WHERE id =:itemId").setParameter("itemId",item).setParameter("orderNum", count).executeUpdate();
			count++;
		}
	}
}
