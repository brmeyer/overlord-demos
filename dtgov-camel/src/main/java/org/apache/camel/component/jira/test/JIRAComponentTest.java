/**
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
package org.apache.camel.component.jira.test;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

// NOTE: This is NOT a unit test!  Use it to manually test against a real instance.

public class JIRAComponentTest extends CamelTestSupport {
	
	private static final String URL = "http://issues.jboss.org";
    
    private static final String USERNAME = "username";
    
    private static final String PASSWORD = "password";

    @Test
    public void testNewIssue() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.setResultWaitTime(180000);
        assertMockEndpointsSatisfied();
    }

    @Test
    public void testNewComment() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.setResultWaitTime(180000);
        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("jira://newIssue?serverUrl=" + URL + "&username=" + USERNAME + "&password=" + PASSWORD
                		+ "&jql=project=SRAMP").to("mock:result");
//                from("jira://newComment?serverUrl=" + URL + "&username=" + USERNAME + "&password=" + PASSWORD
//                        + "&jql=RAW(project=DTGOV AND status in (Open, \"Coding In Progress\") AND \"Number of comments\">0)").to("mock:result");
            }
        };
    }
}