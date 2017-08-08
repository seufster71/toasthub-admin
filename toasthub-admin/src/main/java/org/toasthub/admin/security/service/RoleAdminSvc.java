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

package org.toasthub.admin.security.service;

import org.toasthub.core.general.model.RestRequest;
import org.toasthub.core.general.model.RestResponse;
import org.toasthub.security.service.RoleSvc;

public interface RoleAdminSvc extends RoleSvc {

	void delete(RestRequest request, RestResponse response);
	void save(RestRequest request, RestResponse response);
	void savePermission(RestRequest request, RestResponse response);
	void deletePermission(RestRequest request, RestResponse response);
	void userRoleIds(RestRequest request, RestResponse response);
}
