package nz.net.cdonald.rosters.services

import ch.qos.logback.classic.Logger
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class UserService {

	static final Logger logger = LoggerFactory.getLogger(UserService.class)

	@Value('${auth0.domain}')
	String auth0Domain

	@Value('${auth0.management.client_id}')
	String clientId

	@Value('${auth0.management.client_secret}')
	String clientSecret

	@Value('${auth0.management.audience}')
	String audience

	@Value('${auth0.connection:ShellUsers}')
	String connection

	@Value('${mail.sink.suffix}')
	String emailSinkSuffix

	public Optional<Map> getProfile(String userId) {
		logger.debug("Getting profile for {}", userId)

		def accessToken = getAccessToken();

		def auth0Client = new RESTClient("https://$auth0Domain")
		try {
			def resp = auth0Client.get(
					path: '/api/v2/users/' + userId,
					headers: ["Authorization": "Bearer " + accessToken],
					requestContentType: ContentType.JSON
			)
			return Optional.ofNullable(resp.data as Map)
		} catch (ex) {
			if (ex.response.status == 404) return Optional.empty()
			else throw new Exception("Auth0 error when attempting to get user profile: ${ex.statusCode}:${ex.response.data.message}",ex)
		}
	}

	public Optional<Map> getOperatorProfile(long operatorId) {
		def accessToken = getAccessToken();

		def auth0Client = new RESTClient("https://$auth0Domain")

		def q = "app_metadata.operator_id:" + (operatorId < 0 ? "\\$operatorId" : operatorId )

		try {
			def resp = auth0Client.get(
					path: '/api/v2/users',
					query: [q: q],
					headers: ["Authorization": "Bearer " + accessToken],
					requestContentType: ContentType.JSON
			)

			if (resp.data.size() > 1) throw new Exception("More than one user with the same operator ID")
			if (resp.data.size() == 0) return Optional.empty()
			return Optional.ofNullable(resp.data[0] as Map)
		} catch (HttpResponseException ex) {
			throw new Exception("Auth0 error when attempting to get operator profile: ${ex.statusCode}:${ex.response.data.message}",ex)
		}
	}

	public void updateAppMetadata(String userId, Map<String, Object> metadata) {
		def accessToken = getAccessToken();

		def auth0Client = new RESTClient("https://$auth0Domain")

		try {
			def resp = auth0Client.patch(
					path: '/api/v2/users/' + userId,
					headers: ["Authorization": "Bearer " + accessToken],
					requestContentType: ContentType.JSON,
					body: [app_metadata: metadata]
			)
		} catch (HttpResponseException ex) {
			throw new Exception("Auth0 error when attempting to update user app metadata: ${ex.statusCode}:${ex.response.data.message}",ex)
		}

		logger.info("Updated profile for user {} with metadata {}", userId, metadata)
	}

	private def getAccessToken() {
		def auth0Client = new RESTClient("https://$auth0Domain")

		try {
			def resp = auth0Client.post(
					path: '/oauth/token',
					body: ["client_id"    : clientId,
						   "client_secret": clientSecret,
						   "audience"     : audience,
						   "grant_type"   : "client_credentials"],
					requestContentType: ContentType.JSON
			)
			return resp.data.access_token
		} catch (HttpResponseException ex) {
			throw new Exception("Auth0 error when attempting to get access token: ${ex.statusCode}:${ex.response.data.message}",ex)
		}

	}

	def createShellUser(String email, def app_metadata = [:], String password = UUID.randomUUID().toString(), boolean emailVerified = true) {
		def accessToken = getAccessToken();

		def auth0Client = new RESTClient("https://$auth0Domain")

		try {
			def resp = auth0Client.post(
					path: '/api/v2/users',
					headers: ["Authorization": "Bearer " + accessToken],
					requestContentType: ContentType.JSON,
					body: [connection    : connection,
							//paranoid but want to be sure we don't spam users
						   email         : email + emailSinkSuffix,
						   password      : password,
						   email_verified: emailVerified,
						   app_metadata  : app_metadata ]
			)

			logger.info("Created user for email {} ", email)
			return resp.data
		} catch (HttpResponseException ex) {
			throw new Exception("Auth0 error when attempting to create user: ${ex.statusCode}:${ex.response.data.message}",ex)
		}

	}

	def deleteUser(String id) {
		def accessToken = getAccessToken();

		def auth0Client = new RESTClient("https://$auth0Domain")

		try {
			def resp = auth0Client.delete(
					path: '/api/v2/users/' + id,
					headers: ["Authorization": "Bearer " + accessToken],
					requestContentType: ContentType.JSON,
			)
		} catch (HttpResponseException ex) {
			throw new Exception("Auth0 error when attempting to delete user: ${ex.statusCode}:${ex.response.data.message}",ex)
		}
		logger.info("Deleted user with ID {} ", id)
	}
}
