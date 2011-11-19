/*
 * Copyright (C) the original author or authors.
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
package net.conquiris.api.index;

import com.google.common.util.concurrent.Service;

/**
 * Interface for an indexer service.
 * @author Andres Rodriguez.
 */
public interface IndexerService extends Service {
	/**
	 * Returns the index status.
	 */
	IndexStatus getIndexStatus();
	
	/**
	 * Returns true if and only if the service is RUNNING and the current policy is to keep the index active.
	 */
	boolean isActive();
	
	/** Returns the current delay specification. */
	Delays getDelays();
	
	/**
	 * Set the delay specification.
	 * @param delays New delay specification.
	 */
	void setDelays(Delays delays);
}