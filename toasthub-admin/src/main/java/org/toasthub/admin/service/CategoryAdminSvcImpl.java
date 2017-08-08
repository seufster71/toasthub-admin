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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.toasthub.admin.repository.CategoryAdminDao;
import org.toasthub.core.general.handler.ServiceProcessor;
import org.toasthub.core.general.model.BaseEntity;
import org.toasthub.core.general.model.RestRequest;
import org.toasthub.core.general.model.RestResponse;
import org.toasthub.core.general.service.CategorySvcImpl;
import org.toasthub.core.general.service.UtilSvc;

@Service("CategoryAdminSvc")
public class CategoryAdminSvcImpl extends CategorySvcImpl implements ServiceProcessor, CategoryAdminSvc {

	@Autowired 
	@Qualifier("CategoryAdminDao")
	CategoryAdminDao categoryAdminDao;
	
	@Autowired 
	UtilSvc utilSvc;
	
	@Override
	public void process(RestRequest request, RestResponse response) {
		String action = (String) request.getParams().get(BaseEntity.ACTION);
		
		Long count = 0l;
		switch (action) {
		case "INIT":
			this.items(request, response);
			break;
		case "LIST":
			itemCount(request, response);
			count = (Long) response.getParam("count");
			if (count != null && count > 0){
				items(request, response);
			}
			break;
		case "SHOW":
			this.item(request, response);
			break;
		case "DELETE":
			this.delete(request, response);
			break;
		case "SAVE":
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
			categoryAdminDao.delete(request, response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//@Authorize
	public void save(RestRequest request, RestResponse response) {
		try {
			categoryAdminDao.save(request, response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
