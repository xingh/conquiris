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
package net.conquiris.index;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.GuardedBy;

import net.conquiris.api.index.Writer;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;

/**
 * Default writer implementation.
 * @author Andres Rodriguez.
 */
abstract class DefaultWriter extends AbstractWriter {
	/** Lucene index writer. */
	private final IndexWriter writer;
	/** Last commit user properties. */
	private final ImmutableMap<String, String> lastProperties;
	/** Current user properties. */
	@GuardedBy("this")
	private final Map<String, String> properties;
	/** User properties key set. */
	private final Set<String> keys;
	/** Last commit checkpoint. */
	private final String checkpoint;
	/** Last commit timestamp. */
	private final long timestamp;
	/** Last commit sequence. */
	private final long sequence;
	/** Whether the writer is available. */
	private volatile boolean available = true;
	/** Whether the index has been updated. */
	private volatile boolean updated = false;
	/** Current checkpoint. */
	@GuardedBy("this")
	private String newCheckpoint;

	private static long safe2Long(String s) {
		if (s == null) {
			return 0L;
		}
		try {
			return Long.parseLong(s);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * Default writer.
	 * @param writer Lucene index writer to use.
	 */
	DefaultWriter(Logger logger, IndexWriter writer) throws IOException {
		this.writer = checkNotNull(writer, "The index writer must be provided");
		this.properties = Maps.newHashMap();
		this.keys = Collections.unmodifiableSet(this.properties.keySet());
		// Read properties
		final IndexReader reader = IndexReader.open(writer, false);
		try {
			Map<String, String> data = reader.getCommitUserData();
			if (data == null || data.isEmpty()) {
				this.lastProperties = ImmutableMap.of();
				this.checkpoint = null;
				this.timestamp = 0L;
				this.sequence = 0L;
			} else {
				this.lastProperties = ImmutableMap.copyOf(Maps.filterEntries(data, IS_USER_PROPERTY));
				properties.putAll(this.lastProperties);
				this.checkpoint = data.get(CHECKPOINT);
				this.timestamp = safe2Long(data.get(TIMESTAMP));
				this.sequence = safe2Long(data.get(SEQUENCE));
			}
			this.newCheckpoint = this.checkpoint;
		} finally {
			Closeables.closeQuietly(reader);
		}
	}

	/** Called when the writer can't be used any longer. */
	void done() throws InterruptedException {
		synchronized (this) {
			ensureAvailable();
			available = false;
		}
	}

	@Override
	void ensureAvailable() throws InterruptedException {
		checkState(available, "The writer can't be used any longer");
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}
		// TODO: use interruptions.
	}

	/*
	 * (non-Javadoc)
	 * @see net.conquiris.api.index.Writer#getCheckpoint()
	 */
	@Override
	public String getCheckpoint() throws InterruptedException {
		ensureAvailable();
		return checkpoint;
	}

	/*
	 * (non-Javadoc)
	 * @see net.conquiris.api.index.Writer#getTimestamp()
	 */
	@Override
	public long getTimestamp() throws InterruptedException {
		ensureAvailable();
		return timestamp;
	}

	/*
	 * (non-Javadoc)
	 * @see net.conquiris.api.index.Writer#getSequence()
	 */
	@Override
	public long getSequence() throws InterruptedException {
		ensureAvailable();
		return sequence;
	}

	/*
	 * (non-Javadoc)
	 * @see net.conquiris.api.index.Writer#getProperty(java.lang.String)
	 */
	@Override
	public String getProperty(String key) throws InterruptedException {
		ensureAvailable();
		return properties.get(key);
	}

	/*
	 * (non-Javadoc)
	 * @see net.conquiris.api.index.Writer#getPropertyKeys()
	 */
	@Override
	public Set<String> getPropertyKeys() throws InterruptedException {
		ensureAvailable();
		return keys;
	}

	@Override
	public synchronized Writer setCheckpoint(String checkpoint) throws InterruptedException {
		ensureAvailable();
		return null;
	}

}
