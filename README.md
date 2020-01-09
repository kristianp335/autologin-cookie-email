## AutoLogin Cookie Email

This is a Liferay OSGI module configured for 7.2, but which should work for 7.1, 7.0 providing you change the gradle dependencies.

The modules uses the AutoLogin extension point to automatically create a user account and log that user in if they have a cookie value present. 

You can modify the cookies values used to your own requirements. Generally it is just a good example of how to do an AutoLogin and how to use the Liferay API's to create a user programmatically.