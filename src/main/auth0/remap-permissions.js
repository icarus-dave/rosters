function (user, context, callback) {
  user.scope = user.permissions.join(' ');
  callback(null, user, context);
}