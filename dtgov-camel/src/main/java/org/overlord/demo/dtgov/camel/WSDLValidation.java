package org.overlord.demo.dtgov.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;
import org.eclipse.egit.github.core.PullRequest;

/**
 * This Camel RouteBuilder provides a simple demo of proposed DTGov 2.0 features.  Using camel-github and camel-twitter,
 * the following flow is possible:
 * 
 * 1.) Fork https://github.com/overlordtester/camel-github-test, then create a new pull request with a WSDL update.
 * 2.) DTGov validates the WSDL and discovers an issue.  (For this specific demo, the validation will be fake.)
 * 3.) DTGov comments on the pull request, describing why it was invalidated.
 * 4.) DTGov automatically rejects the pull request.
 * 5.) DTGov posts a Tweet, describing the failure, to a central account.  (Theoretically, this could also be
 *     configured to Tweet *at* a user, send a direct message, etc.)
 * 6.) The developer corrects the WSDL and submits a new pull request.
 * 7.) DTGov successfully validates the WSDL.
 * 8.) DTGov comments on the pull request, mentioning that it's valid.
 * 
 * To run the demo:
 * 
 * 1.) mvn exec:java -Dexec.mainClass=org.overlord.demo.dtgov.camel.WSDLValidation -Dexec.args='-t'
 *     (The -t option will print out Camel Route tracing -- useful for demos.)
 * 2.) Fork https://github.com/overlordtester/camel-github-test, commit *anything*, and create a pull request.
 *     IMPORTANT: Title the pull request "invalid".  This will tell the fake validator to take the error path.
 *     The commit itself is irrelevant.
 * 3.) Verify that DTGov commented on the pull request, closed it, and Tweeted about it.
 * 4.) Repeat #3, but title it anything *other than* "invalid".
 * 5.) Verify that DTGov commented on the pull request.
 * 
 * GitHub: username "overlordtester", password "overlord1!"
 * Twitter: username "overlordtester", password "overlord1!", keys created through https://apps.twitter.com/app/6988265
 * 
 * @author Brett Meyer
 */
public class WSDLValidation extends Main {
	
	private static final String GITHUB_USERNAME = "overlordtester";
    private static final String GITHUB_PASSWORD = "overlord1!";
    private static final String GITHUB_REPO_OWNER = "overlordtester";
    private static final String GITHUB_REPO_NAME = "camel-github-test";
    
    private static final String TWITTER_CONSUMER_KEY = "atzuANIFPtqMUobLuZOcpDrty";
    private static final String TWITTER_CONSUMER_SECRET = "5i92HhI87We7mY516XW2LJTttKLQAeFxgTzciWtpemifhpUgt7";
    private static final String TWITTER_ACCESS_TOKEN = "2795061335-3YlxeRvAptQTnljXcUowxoVXk5n6VXzn70jGKgI";
    private static final String TWITTER_ACCESS_TOKEN_SECRET = "r9kQDbUvxYNYd69B9HpfQmWV6nSPSUaUz6UTr0uLWDduS";

    public static class WSDLValidationRouteBuilder extends RouteBuilder {
		@Override
		public void configure() throws Exception {
			// Watch for pull requests
			from("github://pullRequest?username=" + GITHUB_USERNAME + "&password=" + GITHUB_PASSWORD
	                		+ "&repoOwner=" + GITHUB_REPO_OWNER + "&repoName=" + GITHUB_REPO_NAME)
	                .choice()
	                		.when(new InvalidPullRequestPredicate()).to("direct:invalidPullRequest")
	                		.otherwise().to("direct:validPullRequest");
			
			// Invalid pull request: comment on PR, then Tweet
			// TODO: Also needs to close the PR, so may need to wiretap or in/out in here.  In/out *before* the Tweet
			// might be best, but make sure the new ClosePR endpoint adds the PullRequest object back to the Exchange.
      	  	from("direct:invalidPullRequest")
      	  			.process(new InvalidPullRequestProcessor())
      	  			.multicast().parallelProcessing()
		      	  			.to("github://pullRequestComment?username=" + GITHUB_USERNAME + "&password=" + GITHUB_PASSWORD
		      	  					+ "&repoOwner=" + GITHUB_REPO_OWNER + "&repoName=" + GITHUB_REPO_NAME)
		      	  			.to("twitter://timeline/user?consumerKey=" + TWITTER_CONSUMER_KEY
		      	  					+ "&consumerSecret=" + TWITTER_CONSUMER_SECRET + "&accessToken=" + TWITTER_ACCESS_TOKEN
		      	  					+ "&accessTokenSecret=" + TWITTER_ACCESS_TOKEN_SECRET);
			
			// Valid pull request: comment on PR
      	  	from("direct:validPullRequest")
	  				.process(new ValidPullRequestProcessor())
	  				.to("github://pullRequestComment?username=" + GITHUB_USERNAME + "&password=" + GITHUB_PASSWORD
	  						+ "&repoOwner=" + GITHUB_REPO_OWNER + "&repoName=" + GITHUB_REPO_NAME);
		}
    }
	
	public static class InvalidPullRequestPredicate implements Predicate {
        @Override
		public boolean matches(Exchange exchange) {
			PullRequest pullRequest = exchange.getIn().getBody(PullRequest.class);
			return pullRequest.getTitle().equalsIgnoreCase("invalid");
		}
    }
	
	public static class InvalidPullRequestProcessor implements Processor {
		@Override
		public void process(Exchange exchange) throws Exception {
			PullRequest pullRequest = exchange.getIn().getBody(PullRequest.class);
			String notification = "INVALID PULL REQUEST: Pull request #" + pullRequest.getNumber()
					+ " failed validation!  " + pullRequest.getIssueUrl();
			exchange.getIn().setBody(notification);
			
			// needed for GitHub comment
			exchange.getIn().setHeader("GitHubPullRequest", pullRequest.getNumber());
		}
    }
	
	public static class ValidPullRequestProcessor implements Processor {
		@Override
		public void process(Exchange exchange) throws Exception {
			PullRequest pullRequest = exchange.getIn().getBody(PullRequest.class);
			String notification = "VALID PULL REQUEST: Pull request #" + pullRequest.getNumber()
					+ " passed validation!  " + pullRequest.getIssueUrl();
			exchange.getIn().setBody(notification);
			
			// needed for GitHub comment
			exchange.getIn().setHeader("GitHubPullRequest", pullRequest.getNumber());
		}
    }
	
	public static void main(String[] args) throws Exception {
		WSDLValidation main = new WSDLValidation();
        main.enableHangupSupport();
        main.addRouteBuilder(new WSDLValidationRouteBuilder());
        main.run(args);
    }
}
