function (user, context, callback) {
  user.app_metadata = user.app_metadata || {};
  //we've already signed up (for subsequent logins)
  if (user.app_metadata.signup_complete === true) return callback(null,user,context);

  var request = require('request@2.56.0');

  //First time login: lets check for a shell user
  var token = context.request.query.registration_token || context.request.body.registration_token;
  var user_id = context.request.query.user_id || context.request.body.user_id;
  if (token === undefined) return callback(new UnauthorizedError('Registration token not specified'));
  if (user_id === undefined) return callback(new UnauthorizedError('User ID not specified'));

  //retrieve and check the shell user
  request({url:auth0.baseUrl + "/users/" + user_id,
          headers: { Authorization: 'Bearer ' + auth0.accessToken }},
          function(err, response, body) {
            if (err) return callback(new Error('Error retrieving shell profile: ' + err));
            if (response.statusCode !== 200) return callback(new Error(body));

            var shell_user = JSON.parse(body);
            if (shell_user === null) return callback(new UnauthorizedError('User ID is unknown'));
            if (!("registration_token" in shell_user.app_metadata) || !("token_valid_until" in shell_user.app_metadata))
              return callback(new UnauthorizedError('Referenced user is not setup for registration'));
            if (shell_user.app_metadata.registration_token !== token)
              return callback(new UnauthorizedError('Registration token is invalid'));
            if (shell_user.app_metadata.token_valid_until < Math.round(new Date().getTime() / 1000))
              return callback(new UnauthorizedError('Registration expired; please request a new one'));

            //update the first user with appropriate app metadata and delete the shell user
            user.app_metadata = Object.assign({},shell_user.app_metadata,user.app_metadata);

            //now login for the first time (this will update metadata in Auth0 and delete shell user)
            callback(null, user, context);
          });
}