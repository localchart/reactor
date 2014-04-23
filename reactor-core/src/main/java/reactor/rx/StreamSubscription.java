/*
 * Copyright (c) 2011-2013 GoPivotal, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactor.rx;

import org.reactivestreams.spi.Subscriber;
import org.reactivestreams.spi.Subscription;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Relationship between a Stream (Publisher) and a Subscriber.
 * <p/>
 * In Reactor, a subscriber can be an Action which is both a Stream (Publisher) and a Subscriber.
 *
 * @author Stephane Maldini
 * @since 1.1
 */
public class StreamSubscription<O> implements Subscription {
	final Subscriber<O> subscriber;
	final Stream<O>     publisher;
	final AtomicInteger capacity;

	boolean terminated;

	public StreamSubscription(Stream<O> publisher, Subscriber<O> subscriber) {
		this.subscriber = subscriber;
		this.publisher = publisher;
		this.capacity = new AtomicInteger(-1);
		this.terminated = false;
	}

	@Override
	public void requestMore(int elements) {
		if(terminated){
			return;
		}

		if (elements <= 0) {
			throw new IllegalStateException("Cannot request negative number");
		}

		if(capacity.getAndSet(elements) <= 0){
			publisher.drain(capacity.get(), subscriber);
		}

	}

	@Override
	public void cancel() {
		publisher.removeSubscription(this);
		terminated = true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		StreamSubscription that = (StreamSubscription) o;

		if (publisher.hashCode() != that.publisher.hashCode()) return false;
		if (!subscriber.equals(that.subscriber)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = subscriber.hashCode();
		result = 31 * result + publisher.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "{" +
				"capacity=" + capacity +
				'}';
	}

	public Stream<?> getPublisher() {
		return publisher;
	}
}
