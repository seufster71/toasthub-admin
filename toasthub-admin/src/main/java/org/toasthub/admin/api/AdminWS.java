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

package org.toasthub.admin.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.toasthub.core.common.UtilSvc;
import org.toasthub.core.general.api.View;
import org.toasthub.core.general.model.GlobalConstant;
import org.toasthub.core.general.model.AppCacheServiceCrawler;

import com.fasterxml.jackson.annotation.JsonView;

import org.toasthub.core.general.model.RestRequest;
import org.toasthub.core.general.model.RestResponse;
import org.toasthub.core.general.model.ServiceClass;
import org.toasthub.core.preference.model.PrefCacheUtil;
import org.toasthub.core.preference.model.PrefOptionValue;
import org.toasthub.core.serviceCrawler.MicroServiceClient;
import org.toasthub.security.common.SecurityUtils;
import org.toasthub.security.model.MyUserPrincipal;

@RestController()
@RequestMapping("/api/admin")
public class AdminWS {

	@Autowired 
	UtilSvc utilSvc;
	
	@Autowired 
	AppCacheServiceCrawler serviceCrawler;

	@Autowired 
	PrefCacheUtil prefCacheUtil;
	
	@Autowired
	MicroServiceClient microServiceClient;
	
	@JsonView(View.Admin.class)
	@RequestMapping(value = "callService", method = RequestMethod.POST)
	public RestResponse callService(@RequestBody RestRequest request) {
		
		RestResponse response = new RestResponse();
		// set defaults
		PrefOptionValue globalListLimit = prefCacheUtil.getPrefOption("GLOBAL_PAGE", "GLOBAL_PAGE_PAGELIMIT",(String)request.getParam(GlobalConstant.LANG));
		PrefOptionValue globalListLimitMax = prefCacheUtil.getPrefOption("GLOBAL_PAGE", "GLOBAL_PAGE_PAGELIMIT_MAX",(String)request.getParam(GlobalConstant.LANG));
		utilSvc.setupDefaults(request, globalListLimit, globalListLimitMax);
		// validate request
		try {
			MyUserPrincipal principal = (MyUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			// call service locator
			ServiceClass serviceClass = serviceCrawler.getServiceClass("ADMIN",(String) request.getParams().get(GlobalConstant.SERVICE),
					(String) request.getParam(GlobalConstant.SVCAPIVERSION), (String) request.getParam(GlobalConstant.SVCAPPVERSION));
			// process 
			if (serviceClass != null) {
				// check permissions
				if (SecurityUtils.containsPermission(principal.getUser(), serviceClass.getPermissionCode(), serviceClass.getPermissionRight())) {
					if ("LOCAL".equals(serviceClass.getLocation()) && serviceClass.getServiceProcessor() != null) {
						// use local service
						serviceClass.getServiceProcessor().process(request, response);
					} else {
						// use remote service
						request.addParam(GlobalConstant.MICROSERVICENAME, "service-admin");
						request.addParam(GlobalConstant.MICROSERVICEPATH, "api/admin");
						microServiceClient.process(request, response);
					}
				} else {
					utilSvc.addStatus(RestResponse.ERROR, RestResponse.ACCESSDENIED, prefCacheUtil.getPrefText("GLOBAL_SERVICE", "GLOBAL_SERVICE_ACCESS_DENIED",principal.getUser().getLang()), response);
				}
			} else {
				utilSvc.addStatus(RestResponse.ERROR, RestResponse.EXECUTIONFAILED, "Service is not available", response);
			}
		} catch (Exception e) {
			utilSvc.addStatus(RestResponse.ERROR, RestResponse.EXECUTIONFAILED, "Service has exceptions", response);
		}
		// response
		response.addParam(GlobalConstant.LISTSTART, request.getParam(GlobalConstant.LISTSTART));
		response.addParam(GlobalConstant.LISTLIMIT, request.getParam(GlobalConstant.LISTLIMIT));
		return response;
	}
	

}
