/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.gemfire.outbound;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.apache.geode.internal.cache.DistributedRegion;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author David Turanski
 * @author Artem Bilan
 * @since 2.1
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@DirtiesContext
public class GemfireOutboundChannelAdapterTests {

	@Autowired
	MessageChannel cacheChannel1;

	@Autowired
	DistributedRegion region1;

	@Autowired
	MessageChannel cacheChannel2;

	@Autowired
	DistributedRegion region2;

	@Autowired
	MessageChannel cacheChainChannel;


	@Before
	public void setUp() {
		region1.clear();
		region2.clear();
	}

	@Test
	public void testWriteMapPayload() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("foo", "bar");

		Message<?> message = MessageBuilder.withPayload(map).build();
		cacheChannel1.send(message);
		assertThat(region1.size()).isEqualTo(1);
		assertThat(region1.get("foo")).isEqualTo("bar");
	}

	@Test
	public void testWriteExpressions() {
		Message<?> message = MessageBuilder.withPayload("Hello").build();
		cacheChannel2.send(message);
		assertThat(region2.size()).isEqualTo(2);
		assertThat(region2.get("HELLO")).isEqualTo("hello");
		assertThat(region2.get("foo")).isEqualTo("bar");
	}

	@Test //INT-2275
	public void testWriteWithinChain() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("foo", "bar");

		Message<?> message = MessageBuilder.withPayload(map).build();
		cacheChainChannel.send(message);
		assertThat(region1.size()).isEqualTo(1);
		assertThat(region1.get("foo")).isEqualTo("bar");
	}

}
