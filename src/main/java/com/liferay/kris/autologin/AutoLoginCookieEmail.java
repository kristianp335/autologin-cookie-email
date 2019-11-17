package com.liferay.kris.autologin;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auto.login.AutoLogin;
import com.liferay.portal.kernel.security.auto.login.AutoLoginException;
import com.liferay.portal.kernel.security.auto.login.BaseAutoLogin;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.PortalUtil;

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
		
		Cookie[] allCookies = servletRequest.getCookies();
		for (int i = 0; i < allCookies.length; i++ )
		{
			System.out.println(allCookies[i].getName());
			System.out.println(allCookies[i].getValue());
			
			if (allCookies[i].getName().contains("virtualuser"))
			{
				virtualUser = allCookies[i].getValue();
			}
		}
		if (virtualUser != "")
		{	
			Company company = CompanyLocalServiceUtil.getCompanyByMx(mailDomain);
			long companyId = company.getCompanyId();
			User user = _userLocalService.fetchUserByEmailAddress(companyId, virtualUser);
			
			if (user == null)
			{
				user = addUser(virtualUser, servletRequest);
				credentials = getCredentials(user);
			}
			
			else
			{
				credentials = getCredentials(user);
			}		
	
			return credentials;
		}
		return credentials;
	}

	
	private User addUser(String virtualUser, HttpServletRequest servletRequest ) {	
		
		Company company = null;
		String[] parts = virtualUser.split("@");
	    String names = parts[0];
	    System.out.println("the name part is " + names);
	    String[] namesSplit = names.split("\\.");
	    String codedFirstName = namesSplit[0];
	    System.out.println("the first name part is " + codedFirstName);
	    String codedSurname = namesSplit[1];
	    System.out.println("the surname part is " + codedSurname);
	    String codedScreenName = codedFirstName + codedSurname;
		   
		try {
			company = CompanyLocalServiceUtil.getCompanyByMx(mailDomain);
		} catch (PortalException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		long companyId = company.getCompanyId();
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
		long[] groupIds = null;
		long[] organizationIds = null;
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