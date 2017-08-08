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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.toasthub.core.general.api.View;
import org.toasthub.core.general.handler.ServiceProcessor;
import org.toasthub.core.general.model.BaseEntity;
import org.toasthub.core.general.model.ServiceCrawler;
import org.toasthub.core.general.service.EntityManagerMainSvc;
import org.toasthub.core.general.service.UtilSvc;

import com.fasterxml.jackson.annotation.JsonView;

import org.toasthub.core.general.model.RestRequest;
import org.toasthub.core.general.model.RestResponse;

@RestController()
@RequestMapping("/api/admin")
public class AdminWS {

	@Autowired EntityManagerMainSvc entityManagerMainSvc;
	@Autowired UtilSvc utilSvc;
	@Autowired ServiceCrawler serviceLocator;
	
	@JsonView(View.Admin.class)
	@RequestMapping(value = "callService", method = RequestMethod.POST)
	public RestResponse callService(@RequestBody RestRequest request) {
		
		RestResponse response = new RestResponse();
		// set defaults
		utilSvc.setupDefaults(request);
		// validate request
		
		response.addParam(BaseEntity.APPNAME,entityManagerMainSvc.getAppName());

		// call service locator
		ServiceProcessor x = serviceLocator.getService("ADMIN",(String) request.getParams().get(BaseEntity.SERVICE),
				(String) request.getParam(BaseEntity.SVCAPIVERSION), (String) request.getParam(BaseEntity.SVCAPPVERSION),
				entityManagerMainSvc.getAppDomain());
		// process 
		if (x != null) {
			x.process(request, response);
		} else {
		
		}
		// response
		response.addParam(BaseEntity.PAGESTART, request.getParam(BaseEntity.PAGESTART));
		response.addParam(BaseEntity.PAGELIMIT, request.getParam(BaseEntity.PAGELIMIT));
		return response;
	}
	

}
