/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hawq.ranger.service;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.ranger.plugin.util.TimedEventUtil;


public class HawqConnectionMgr {

	private static final Logger LOG = Logger.getLogger(HawqConnectionMgr.class);
	
	protected ConcurrentMap<String, HawqClient> 	hawqConnectionCache;
	protected ConcurrentMap<String, Boolean> 		repoConnectStatusMap;


	 public	HawqConnectionMgr() {
		 hawqConnectionCache = new ConcurrentHashMap<String, HawqClient>();
		 repoConnectStatusMap = new ConcurrentHashMap<String, Boolean>();
	 }
	

	 public HawqClient getHawqConnection(final String serviceName, final String serviceType, final Map<String,String> configs) {
			HawqClient hawqClient  = null;
			
			if (serviceType != null) {
				// get it from the cache
				hawqClient = hawqConnectionCache.get(serviceName);
				if (hawqClient == null) {
					if (configs != null) {
					
						final Callable<HawqClient> connectHawq = new Callable<HawqClient>() {
							@Override
							public HawqClient call() throws Exception {
								return new HawqClient(serviceName, configs);
							}
						};
						try {
							hawqClient = TimedEventUtil.timedTask(connectHawq, 5, TimeUnit.SECONDS);
						} catch(Exception e){
							LOG.error("Error connecting Hawq repository : "+
									serviceName +" using config : "+ configs, e);
						}

						HawqClient oldClient = null;
						if (hawqClient != null) {
							oldClient = hawqConnectionCache.putIfAbsent(serviceName, hawqClient);
						} else {
							oldClient = hawqConnectionCache.get(serviceName);
						}

						if (oldClient != null) {
							// in the meantime someone else has put a valid client into the cache, let's use that instead.
							if (hawqClient != null) {
								hawqClient.close();
							}
							hawqClient = oldClient;
						}
						repoConnectStatusMap.put(serviceName, true);
					} else {
						LOG.error("Connection Config not defined for asset :"
								+ serviceName, new Throwable());
					}
				} else {
					try {
						hawqClient.getDatabaseList("*");
					} catch(Exception e) {
						hawqConnectionCache.remove(serviceName);
						/*
						 * There is a possibility that some other thread is also using this connection that we are going to close but
						 * presumably the connection is bad which is why we are closing it, so damage should not be much.
						 */
						hawqClient.close();
						hawqClient = getHawqConnection(serviceName,serviceType,configs);
					}
				}
			} else {
				LOG.error("Asset not found with name "+serviceName, new Throwable());
			}
			return hawqClient;
		}
}
