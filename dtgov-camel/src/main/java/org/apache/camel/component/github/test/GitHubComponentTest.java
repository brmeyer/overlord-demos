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
package org.apache.camel.component.github.test;

import static org.hamcrest.core.Is.is;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.eclipse.egit.github.core.Comment;
import org.junit.Test;

// NOTE: This is NOT a unit test!  Use it to manually test against a real repo.

public class GitHubComponentTest extends CamelTestSupport {
    
    private static final String USERNAME = "username";
    
    private static final String PASSWORD = "password";
    
    private static final String REPO_OWNER = "brmeyer";
    
    private static final String REPO_NAME = "camel-github-test";

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint mock;

    @Test
    public void testPullRequest() throws Exception {
        mock.expectedMessageCount(4);
        mock.setResultWaitTime(60000);
        assertMockEndpointsSatisfied();
    }

    @Test
    public void testPullRequestComment() throws Exception {
        mock.expectedMessageCount(2);
        mock.setResultWaitTime(60000);
        assertMockEndpointsSatisfied();
    }

    @Test
    public void tesCommit() throws Exception {
        mock.expectedMessageCount(1);
        mock.setResultWaitTime(60000);
        assertMockEndpointsSatisfied();
    }

    @Test
    public void testTag() throws Exception {
        mock.expectedMessageCount(1);
        mock.setResultWaitTime(60000);
        assertMockEndpointsSatisfied();
    }

    @Test
    public void testPullRequestCommentProducer() throws Exception {
    	String text = "test comment";
    	ProducerTemplate producerTemplate = context.createProducerTemplate();
    	Map<String, Object> headers = new HashMap<String, Object>();
    	headers.put("GitHubPullRequest", 4);
    	headers.put("GitHubInResponseTo", 17308660); // id manually found by using testPullRequestComment
        producerTemplate.sendBodyAndHeaders("direct:github", text, headers);
        
        mock.expectedMessageCount(1);
        mock.expectedBodyReceived().body(Comment.class);
        assertMockEndpointsSatisfied();
        
        List<Exchange> receivedComments = mock.getExchanges();
        assertNotNull(receivedComments);
        assertThat(receivedComments.size(), is(1));
        Comment receivedComment = receivedComments.get(0).getIn().getBody(Comment.class);
        assertEquals(receivedComment.getBody(), text);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("github://pullRequest?username=" + USERNAME + "&password=" + PASSWORD
                        + "&repoOwner=" + REPO_OWNER + "&repoName=" + REPO_NAME).to("mock:result");
//                from("github://pullRequestComment?username=" + USERNAME + "&password=" + PASSWORD
//                        + "&repoOwner=" + REPO_OWNER + "&repoName=" + REPO_NAME).to("mock:result");
//                from("github://commit/master?username=" + USERNAME + "&password=" + PASSWORD
//                        + "&repoOwner=" + REPO_OWNER + "&repoName=" + REPO_NAME).to("mock:result");
//                from("github://tag?username=" + USERNAME + "&password=" + PASSWORD
//                        + "&repoOwner=" + REPO_OWNER + "&repoName=" + REPO_NAME).to("mock:result");
//            	  from("direct:github")
//		                .inOut("github://pullRequestComment?username=" + USERNAME + "&password=" + PASSWORD
//		                        + "&repoOwner=" + REPO_OWNER + "&repoName=" + REPO_NAME)
//		                .to("mock:result");
            }
        };
    }
}