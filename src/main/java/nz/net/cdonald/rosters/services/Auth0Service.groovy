package nz.net.cdonald.rosters.services

import ch.qos.logback.classic.Logger
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class Auth0Service {

	static final Logger logger = LoggerFactory.getLogger(Auth0Service.class)

	@Value('${auth0.domain}')
	String auth0Domain

	@Value('${auth0.management.client_id}')
	String clientId

	@Value('${auth0.management.client_secret}')
	String clientSecret

	@Value('${auth0.management.audience}')
	String audience

	public Map getProfile(String userId) {
		logger.debug("Getting profile for {}", userId)

		def accessToken = getAccessToken();

		def auth0Client = new RESTClient("https://$auth0Domain")

		def resp = auth0Client.get(
				path: '/api/v2/users/' + userId,
				headers: ["Authorization": "Bearer " + accessToken],
				requestContentType: ContentType.JSON
		)

		return resp.data
	}

	public void updateAppMetadata(String userId, Map<String, Object> metadata) {
		logger.info("Updating profile for user {} with metadata {}", userId, metadata)
		def accessToken = getAccessToken();

		def auth0Client = new RESTClient("https://$auth0Domain")

		def resp = auth0Client.patch(
				path: '/api/v2/users/' + userId,
				headers: ["Authorization": "Bearer " + accessToken],
				requestContentType: ContentType.JSON,
				body: [app_metadata: metadata]
		)
	}

	//in the future we should cache this...
	def getAccessToken() {
		def auth0Client = new RESTClient("https://$auth0Domain")

		def resp = auth0Client.post(
				path: '/oauth/token',
				body: ["client_id"    : clientId,
					   "client_secret": clientSecret,
					   "audience"     : audience,
					   "grant_type"   : "client_credentials"],
				requestContentType: ContentType.JSON
		)

		return resp.data.access_token
	}

	//update the metadata in the JWT (naughty edge case for when first logging in)
	//NOTE the signing secret will be the JWT one, not the management API one
	public String updateAppMetadataJwt(String jwt, String secret, Map appMetadata) {
		def claims = Jwts.parser().setSigningKey(secret.bytes).parseClaimsJws(jwt)

		//add to the app metadata
		def newMetadata = claims.getBody().get("app_metadata", [:]) + appMetadata

		String updatedJwt = Jwts.builder().signWith(SignatureAlgorithm.HS256, secret.bytes).setClaims(claims.getBody())
				.claim("app_metadata", newMetadata).setHeaderParams(claims.getHeader()).compact()

		return updatedJwt;
	}

	//only currently used for testing
	def createUser(String email, String password = UUID.randomUUID().toString(), boolean emailVerified = true) {
		logger.info("Creating user for email {} ", email)
		def accessToken = getAccessToken();

		def auth0Client = new RESTClient("https://$auth0Domain")

		def resp = auth0Client.post(
				path: '/api/v2/users',
				headers: ["Authorization": "Bearer " + accessToken],
				requestContentType: ContentType.JSON,
				body: [connection    : 'Username-Password-Authentication',
					   email         : email,
					   password      : password,
					   email_verified: emailVerified]
		)

		return resp.data
	}

	//currently only used for testing
	def deleteUser(String id) {
		logger.info("Deleting user with ID {} ", id)
		def accessToken = getAccessToken();

		def auth0Client = new RESTClient("https://$auth0Domain")

		def resp = auth0Client.delete(
				path: '/api/v2/users/' + id,
				headers: ["Authorization": "Bearer " + accessToken],
				requestContentType: ContentType.JSON,
		)
	}
}
