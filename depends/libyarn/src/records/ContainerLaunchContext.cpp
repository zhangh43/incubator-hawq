/*
 * ContainerLaunchContext.cpp
 *
 *  Created on: Jul 14, 2014
 *      Author: bwang
 */

#include "ContainerLaunchContext.h"

namespace libyarn {

ContainerLaunchContext::ContainerLaunchContext() {
	containerLaunchCtxProto = ContainerLaunchContextProto::default_instance();
}

ContainerLaunchContext::ContainerLaunchContext(
		const ContainerLaunchContextProto &proto) :
		containerLaunchCtxProto(proto) {
}

ContainerLaunchContext::~ContainerLaunchContext() {
}

ContainerLaunchContextProto& ContainerLaunchContext::getProto() {
	return containerLaunchCtxProto;
}

list<StringLocalResourceMap> ContainerLaunchContext::getLocalResources() {
	list<StringLocalResourceMap> list;
	for (int i = 0; i < containerLaunchCtxProto.localresources_size(); i++) {
		list.push_back(
				StringLocalResourceMap(
						containerLaunchCtxProto.localresources(i)));
	}
	return list;
}

void ContainerLaunchContext::setLocalResources(
		list<StringLocalResourceMap> &resourcesList) {
	list<StringLocalResourceMap>::iterator it = resourcesList.begin();
	for (; it != resourcesList.end(); it++) {
		StringLocalResourceMapProto* proto =
				containerLaunchCtxProto.add_localresources();
		proto->CopyFrom((*it).getProto());
	}
}

list<StringBytesMap> ContainerLaunchContext::getServiceData() {
	list<StringBytesMap> list;
	for (int i = 0; i < containerLaunchCtxProto.service_data_size(); i++) {
		list.push_back(StringBytesMap(containerLaunchCtxProto.service_data(i)));
	}
	return list;
}

void ContainerLaunchContext::setServiceData(
		list<StringBytesMap> &serviceDataList) {
	list<StringBytesMap>::iterator it = serviceDataList.begin();
	for (; it != serviceDataList.end(); it++) {
		StringBytesMapProto* proto = containerLaunchCtxProto.add_service_data();
		proto->CopyFrom((*it).getProto());
	}
}

list<StringStringMap> ContainerLaunchContext::getEnvironment() {
	list<StringStringMap> list;
	for (int i = 0; i < containerLaunchCtxProto.environment_size(); i++) {
		list.push_back(StringStringMap(containerLaunchCtxProto.environment(i)));
	}
	return list;
}

void ContainerLaunchContext::setEnvironment(list<StringStringMap> &envList) {
	list<StringStringMap>::iterator it = envList.begin();
	for (; it != envList.end(); it++) {
		StringStringMapProto* proto = containerLaunchCtxProto.add_environment();
		proto->CopyFrom((*it).getProto());
	}
}

list<ApplicationACLMap> ContainerLaunchContext::getApplicationACLs() {
	list<ApplicationACLMap> list;
	for (int i = 0; i < containerLaunchCtxProto.application_acls_size(); i++) {
		list.push_back(
				ApplicationACLMap(containerLaunchCtxProto.application_acls(i)));
	}
	return list;
}

void ContainerLaunchContext::setApplicationACLs(
		list<ApplicationACLMap> &aclList) {
	list<ApplicationACLMap>::iterator it = aclList.begin();
	for (; it != aclList.end(); it++) {
		ApplicationACLMapProto* proto =
				containerLaunchCtxProto.add_application_acls();
		proto->CopyFrom((*it).getProto());
	}
}

string ContainerLaunchContext::getTokens() {
	return containerLaunchCtxProto.tokens();
}

void ContainerLaunchContext::setTokens(string &tokens) {
	containerLaunchCtxProto.set_tokens(tokens);
}

void ContainerLaunchContext::setCommand(list<string> &commands) {
	for (list<string>::iterator it = commands.begin(); it != commands.end();
			it++) {
		containerLaunchCtxProto.add_command(*it);
	}
}

list<string> ContainerLaunchContext::getCommand() {
	list<string> commands;
	int size = containerLaunchCtxProto.command_size();
	for (int i = 0; i < size; i++) {
		commands.push_back(containerLaunchCtxProto.command(i));
	}
	return commands;
}

}

/* namespace libyarn */
