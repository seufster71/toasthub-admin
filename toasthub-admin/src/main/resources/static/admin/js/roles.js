/**
 * @author Edward H. Seufert
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

adminRoles.prototype = Object.create(toastHubRoles.prototype);
adminRoles.prototype.constructor = adminRoles;

toastHub.registerController("roles",new adminRoles("roles"));
toastHub.registerWidget("roles",new adminRoles("roles"));

function adminRoles(instanceName){
	toastHubRoles.call(this,instanceName,this);
	this.controllerName = "roles";
	var self = this;

	// Override the default setup
	
	
	

}; // adminRoles

adminApplication.prototype = Object.create(toastHubApplication.prototype);
adminApplication.prototype.constructor = adminApplication;

toastHub.registerWidget("application",new adminApplication("application"));

function adminApplication(instanceName){
	toastHubApplication.call(this,instanceName,this);
	this.controllerName = "application";
	var self = this;
	
	// Override the default setup
	
	
	

}; // adminApplication

adminPermissions.prototype = Object.create(toastHubPermissions.prototype);
adminPermissions.prototype.constructor = adminPermissions;

toastHub.registerWidget("permissions",new adminPermissions("permissions"));

function adminPermissions(instanceName){
	toastHubPermissions.call(this,instanceName,this);
	this.controllerName = "permissions";
	var self = this;
	
	// Override the default setup
	
	
	

}; // adminPermissions

