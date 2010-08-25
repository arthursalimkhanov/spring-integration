/*
 * Copyright 2002-2010 the original author or authors.
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

package org.springframework.integration.gateway;

import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.context.TrackableComponent;
import org.springframework.integration.core.MessageHistory;
import org.springframework.integration.mapping.InboundMessageMapper;
import org.springframework.integration.mapping.OutboundMessageMapper;
import org.springframework.util.Assert;

/**
 * An implementation of {@link AbstractMessagingGateway} that delegates to
 * an {@link InboundMessageMapper} and {@link OutboundMessageMapper}. The
 * default implementation for both is {@link SimpleMessageMapper}.
 * 
 * @see InboundMessageMapper
 * @see OutboundMessageMapper
 * 
 * @author Mark Fisher
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class SimpleMessagingGateway extends AbstractMessagingGateway implements TrackableComponent {

	private final InboundMessageMapper inboundMapper;

	private final OutboundMessageMapper outboundMapper;

	private volatile boolean shouldTrack = false;


	public SimpleMessagingGateway() {
		SimpleMessageMapper mapper = new SimpleMessageMapper();
		this.inboundMapper = mapper;
		this.outboundMapper = mapper;
	}

	public SimpleMessagingGateway(InboundMessageMapper<?> inboundMapper, OutboundMessageMapper<?> outboundMapper) {
		Assert.notNull(inboundMapper, "InboundMessageMapper must not be null");
		Assert.notNull(outboundMapper, "OutboundMessageMapper must not be null");
		this.inboundMapper = inboundMapper;
		this.outboundMapper = outboundMapper;
	}


	public void setShouldTrack(boolean shouldTrack) {
		this.shouldTrack = shouldTrack;
	}

	public Message<?> sendAndReceiveMessage(Object object) {
		return (Message<?>) super.sendAndReceive(object, false);
	}
	
	public Object sendAndReceive(Object object) {
		return super.sendAndReceive(object, true);
	}


	@Override
	protected Object fromMessage(Message<?> message) {
		try {
			return this.outboundMapper.fromMessage(message);
		}
		catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new MessagingException(message, e);
		}
	}

	@Override
	protected Message<?> toMessage(Object object) {
		Message<?> message = null;
		try {
			message = this.inboundMapper.toMessage(object);
			if (this.shouldTrack) {
				message = MessageHistory.addComponentToHistory(message, this);
			}
		}
		catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new MessagingException("failed to create Message", e);
		}
		return message;
	}

}
