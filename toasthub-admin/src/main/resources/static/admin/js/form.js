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

adminForm.prototype = Object.create(toastHubForm.prototype);
adminForm.prototype.constructor = adminForm;

toastHub.registerController("form",new adminForm("form"));

function adminForm(instanceName){
	toastHubForm.call(this,instanceName,this);
	this.TAG = "default::adminForm::";
	this.controllerName = "form";
	var self = this;


};