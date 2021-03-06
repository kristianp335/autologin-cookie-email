package com.liferay.kris.autologin;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auto.login.AutoLogin;
import com.liferay.portal.kernel.security.auto.login.AutoLoginException;
import com.liferay.portal.kernel.security.auto.login.BaseAutoLogin;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.CookieKeys;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringPool;

import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;


@Component(
	immediate = true,
	property = {
		// TODO enter required service properties
	},
	service = AutoLogin.class
)
public class AutoLoginCookieEmail extends BaseAutoLogin {
	
	private String[] credentials = new String[3];
	private String mailDomain = "liferay.com";	

	@Override
	protected String[] doHandleException(
			HttpServletRequest request, HttpServletResponse response,
			Exception e)
		throws AutoLoginException {

		throw new AutoLoginException(e);
	}

	@Override
	protected String[] doLogin(
			HttpServletRequest servletRequest, HttpServletResponse servletResponse)
		throws Exception {
	
		String virtualUser = "";		
		//get all the cookies from the request
		Cookie[] allCookies = servletRequest.getCookies();
		
		
		//loop through cookies looking for a cookie called virtualuser which is an email address
		for (int i = 0; i < allCookies.length; i++ )
		{
			System.out.println(allCookies[i].getName() + " = " + allCookies[i].getValue() );
			if (allCookies[i].getName().contains("virtualuser"))
			{
				virtualUser = allCookies[i].getValue();
			}
		}
		
		//check to see if virtual user in not an empty string
		if (virtualUser != "")
		{	
			if (virtualUser == "donotlogin@liferay.com")
			{	
				System.out.println("No one should be logged in");
				return credentials;
				
			}
			
			else {
			
				Company company = CompanyLocalServiceUtil.getCompanyByMx(mailDomain);
				long companyId = company.getCompanyId();
				//find if a user already exists with the same email address
				User user = _userLocalService.fetchUserByEmailAddress(companyId, virtualUser);
				
				//if no user exists then create the user and get the credentials
				if (user == null)
				{
					user = addUser(virtualUser, servletRequest);
					credentials = getCredentials(user);
					
				}
				
				//else just get the credentials of the user
				else
				{
					credentials = getCredentials(user);
					
				}		
		
				return credentials;
			}
			
		}
		
		return null;
		
	}

	//this method just creates the user, it attempts to split the email address of the user for names
	//You probably want to work on the email splitting
	private User addUser(String virtualUser, HttpServletRequest servletRequest ) {	
		
		//site id is for site membership, this is hardcoded get it from the Liferay instance
		long siteId = 88392;
		Company company = null;
		String[] parts = virtualUser.split("@");
	    String names = parts[0];
	    String[] namesSplit = names.split("\\.");
	    String codedFirstName = "";
	    String codedSurname = "";
	    
	    if (namesSplit.length > 1) 
	    {
	    	codedFirstName = namesSplit[0];
	    	codedSurname = namesSplit[1];
	    }
	    
	    else
	    
	    {
	    	codedFirstName = names;
	    	codedSurname = names;
	    }
	   	   
	    String codedScreenName = codedFirstName + codedSurname;
		   
		try {
			company = CompanyLocalServiceUtil.getCompanyByMx(mailDomain);
		} catch (PortalException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		long companyId = company.getCompanyId();
		Organization organization = null;
		try {
			organization = OrganizationLocalServiceUtil.getOrganization(companyId, "JM");
		} catch (PortalException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		long organizationId = organization.getOrganizationId();
	    long adminUserId = 0;
		Role role = null;
        try {
            role = getRoleById(companyId, RoleConstants.ADMINISTRATOR);
            for (final User admin : _userLocalService.getRoleUsers(role
                    .getRoleId())) {
                adminUserId = admin.getUserId();
            }
        } catch (final Exception e) {
            
        }
        long creatorUserId = adminUserId;
		boolean autoPassword = true;
		String password1 = null;
		String password2 = null;
		boolean autoScreenName = false;
		String emailAddress = virtualUser;
		String screenName = codedScreenName;
		long facebookId = 0;
		String openId = null;
		Locale locale = null;
		try {
			locale = company.getLocale();
		} catch (PortalException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		String firstName = codedFirstName;
		String middleName = null;
		String lastName = codedSurname;
		long prefixId = 0;
		long suffixId = 0;
		boolean male = true;
		int birthdayMonth = 6;
		int birthdayDay = 11;
		int birthdayYear = 1977;
		String jobTitle = "";
		long[] groupIds = new long[1];
		groupIds[0] = siteId;
		long[] organizationIds = new long[1];
		organizationIds[0] = organizationId;
		long[] roleIds = null;
		long[] userGroupIds = null;
		boolean sendEmail = false;
		ServiceContext serviceContext = new ServiceContext();
		serviceContext.setCompanyId(companyId);
		try {
			serviceContext.setScopeGroupId(company.getGroupId());
		} catch (PortalException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		serviceContext.setUserId(adminUserId);	
		
		User createdUser = null;
		
		try {
			createdUser = _userLocalService.addUserWithWorkflow(creatorUserId, companyId, autoPassword, password1, password2, autoScreenName, screenName, emailAddress, facebookId, openId, locale, firstName, middleName, lastName, prefixId, suffixId, male, birthdayMonth, birthdayDay, birthdayYear, jobTitle, groupIds, organizationIds, roleIds, userGroupIds, sendEmail, serviceContext);
		} catch (PortalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		//need to turn off password reset, terms and password reminder question etc
		createdUser.setAgreedToTermsOfUse(true);
		createdUser.setPasswordReset(false);
		createdUser.setReminderQueryQuestion("auto account");
		createdUser.setReminderQueryAnswer("auto account");
		_userLocalService.updateUser(createdUser);
		
		return createdUser;		
	}

	@Reference(unbind = "-")
	protected void setUserLocalService(UserLocalService userLocalService) {
		_userLocalService = userLocalService;
	}

	private static final Log _log = LogFactoryUtil.getLog(
			AutoLoginCookieEmail.class);

	private UserLocalService _userLocalService;
	
	public static Role getRoleById(final long companyId, final String roleStrId) {
        try {
            return RoleLocalServiceUtil.getRole(companyId, roleStrId);
        } catch (final Exception e) {
            
        }
        return null;
    }
	
	//this methods gets the credentials for login
	private String[] getCredentials(User user)
	{	
		
		String password = user.getPassword();
		String userId = Long.toString(user.getUserId());
		String isEncrypted = Boolean.toString(user.getPasswordEncrypted());		
	
		credentials[0] = userId;
		credentials[1] = password;
		credentials[2] = isEncrypted;
		return credentials;
		
	}	

}