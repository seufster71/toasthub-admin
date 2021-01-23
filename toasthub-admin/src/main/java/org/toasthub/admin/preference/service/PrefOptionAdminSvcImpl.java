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

package org.toasthub.admin.preference.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.toasthub.admin.preference.repository.PrefOptionAdminDao;
import org.toasthub.core.common.UtilSvc;
import org.toasthub.core.general.handler.ServiceProcessor;
import org.toasthub.core.general.model.GlobalConstant;
import org.toasthub.core.general.model.RestRequest;
import org.toasthub.core.general.model.RestResponse;
import org.toasthub.core.preference.model.PrefCacheUtil;
import org.toasthub.core.preference.model.PrefOptionName;
import org.toasthub.core.preference.service.PrefOptionSvcImpl;

@Service("PrefOptionAdminSvc")
public class PrefOptionAdminSvcImpl extends PrefOptionSvcImpl implements ServiceProcessor, PrefOptionAdminSvc {

	@Autowired 
	@Qualifier("PrefOptionAdminDao")
	PrefOptionAdminDao prefOptionAdminDao;
	
	@Autowired 
	PrefCacheUtil prefCacheUtil;
	
	@Autowired 
	UtilSvc utilSvc;
	
	@Override
	public void process(RestRequest request, RestResponse response) {
		String action = (String) request.getParams().get(GlobalConstant.ACTION);
		List<String> global =  new ArrayList<String>(Arrays.asList("LANGUAGES"));
		
		Long count = 0l;
		switch (action) {
		case "INIT":
			request.addParam(PrefCacheUtil.PREFPARAMLOC, PrefCacheUtil.RESPONSE);
			prefCacheUtil.getPrefInfo(request,response);
			itemCount(request, response);
			count = (Long) response.getParam(GlobalConstant.ITEMCOUNT);
			if (count != null && count > 0){
				items(request, response);
			}
			break;
		case "LIST":
			request.addParam(PrefCacheUtil.PREFPARAMLOC, PrefCacheUtil.RESPONSE);
			prefCacheUtil.getPrefInfo(request,response);
			itemCount(request, response);
			count = (Long) response.getParam(GlobalConstant.ITEMCOUNT);
			if (count != null && count > 0){
				items(request, response);
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
				List<String> forms =  new ArrayList<String>(Arrays.asList("ADMIN_OPTION_FORM"));
				request.addParam(PrefCacheUtil.PREFFORMKEYS, forms);
			}
			request.addParam(PrefCacheUtil.PREFGLOBAL, global);
			prefCacheUtil.getPrefInfo(request,response);
			this.save(request, response);
			break;
		default:
			utilSvc.addStatus(RestResponse.INFO, RestResponse.ACTIONNOTEXIST, prefCacheUtil.getPrefText("GLOBAL_SERVICE", "GLOBAL_SERVICE_ACTION_NOT_AVAIL",prefCacheUtil.getLang(request)), response);
			break;
		}
	}
	
	//@Authorize
	public void delete(RestRequest request, RestResponse response) {
		try {
			prefOptionAdminDao.delete(request, response);
			// reset
			prefCacheUtil.clearPrefOptionCache();
			
			utilSvc.addStatus(RestResponse.INFO, RestResponse.SUCCESS, prefCacheUtil.getPrefText("GLOBAL_SERVICE", "GLOBAL_SERVICE_DELETE_SUCCESS",prefCacheUtil.getLang(request)), response);
		} catch (Exception e) {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, prefCacheUtil.getPrefText("GLOBAL_SERVICE", "GLOBAL_SERVICE_DELETE_FAIL",prefCacheUtil.getLang(request)), response);
			e.printStackTrace();
		}
	}
	
	//@Authorize
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
				prefOptionAdminDao.item(request, response);
				request.addParam(GlobalConstant.ITEM, response.getParam(GlobalConstant.ITEM));
				response.getParams().remove(GlobalConstant.ITEM);
			} else {
				PrefOptionName o = new PrefOptionName();
				o.setArchive(false);
				o.setLocked(false);
				request.addParam(GlobalConstant.ITEM, o);
			}
						
			// marshall
			utilSvc.marshallFields(request, response);
									
			prefOptionAdminDao.save(request, response);
			// reset
			prefCacheUtil.clearPrefOptionCache();
						
			utilSvc.addStatus(RestResponse.INFO, RestResponse.SUCCESS, prefCacheUtil.getPrefText("GLOBAL_SERVICE", "GLOBAL_SERVICE_SAVE_SUCCESS",prefCacheUtil.getLang(request)), response);
		} catch (Exception e) {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACTIONFAILED, prefCacheUtil.getPrefText("GLOBAL_SERVICE", "GLOBAL_SERVICE_SAVE_FAIL",prefCacheUtil.getLang(request)), response);
			e.printStackTrace();
		}
	}
}
