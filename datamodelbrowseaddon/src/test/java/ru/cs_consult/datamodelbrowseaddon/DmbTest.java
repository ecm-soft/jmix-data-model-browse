package ru.cs_consult.datamodelbrowseaddon;

import io.jmix.core.security.ClientDetails;
import io.jmix.core.security.CurrentAuthentication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import ru.cs_consult.datamodelbrowseaddon.service.DataModelServiceBean;

import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@ExtendWith(AuthAsAdmin.class)
class DmbTest {
	@Autowired
	private DataModelServiceBean dataModelServiceBean;
	@Autowired
	private CurrentAuthentication currentAuthentication;


	@Test
	@DisplayName("DataModelService#getDataModel should return non-blank data model template")
	void getDataModel_checkTemplateIsNotBlank() {
		dataModelServiceBean.getModuleAvailableLocales().forEach(locale -> {
			ClientDetails details = new ClientDetails.Builder().locale(locale).build();
			((AbstractAuthenticationToken) currentAuthentication.getAuthentication()).setDetails(details);
			String dataModel = dataModelServiceBean.getDataModel();
			assertTrue(StringUtils.isNotBlank(dataModel) && dataModel.length() > dataModelServiceBean.getTemplateForLocale(locale).length());
		});
	}
}
