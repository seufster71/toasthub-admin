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

MenuAdmin.prototype = Object.create(ToastHubMenu.prototype);
MenuAdmin.prototype.constructor = MenuAdmin;

toastHub.registerController("menuAdmin",new MenuAdmin("menuAdmin"));

function MenuAdmin(instanceName){
	ToastHubMenu.call(this,instanceName,this);
	this.controllerName = "menuAdmin";
	this.category = "ADMIN";
	var self = this;
	
	this.initCustom = function(params){
		toastHub.logSystem.log("DEBUG","menuAdmin::MenuAdmin::initCustom");
		params.category = this.category;
		params.appForms = ["APP_MENU_FORM","APP_MENU_ITEM_FORM"];
		params.appTexts = ["GLOBAL_PAGE","APP_MENU_PAGE"];
		//params.appLabels = ["APP_MENU_PAGE"];
	}; // initCustom
	

};