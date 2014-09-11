/*******************************************************************************
 * Copyright (c) 2014 SAP AG or an SAP affiliate company. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 *******************************************************************************/

package com.sap.dirigible.ide.template.ui.sc.wizard;

import com.sap.dirigible.ide.template.ui.common.GenerationModel;
import com.sap.dirigible.ide.template.ui.common.TemplateTargetLocationPage;
import com.sap.dirigible.ide.ui.common.validation.IValidationStatus;
import com.sap.dirigible.repository.api.ICommonConstants;

public class SecurityConstraintTemplateTargetLocationPage extends TemplateTargetLocationPage {

	private static final String SELECT_THE_TARGET_LOCATION_AND_THE_TARGET_FILE_NAME = Messages.SecurityConstraintTemplateTargetLocationPage_SELECT_THE_TARGET_LOCATION_AND_THE_TARGET_FILE_NAME;

	private static final String TARGET_LOCATION = Messages.SecurityConstraintTemplateTargetLocationPage_TARGET_LOCATION;

	private static final long serialVersionUID = 5413819137031452222L;

	private static final String PAGE_NAME = "com.sap.dirigible.ide.template.ui.sc.wizard.SecurityConstraintTemplateTargetLocationPage"; //$NON-NLS-1$

	private SecurityConstraintTemplateModel model;

	protected SecurityConstraintTemplateTargetLocationPage(SecurityConstraintTemplateModel model) {
		super(PAGE_NAME);
		this.model = model;
		setTitle(TARGET_LOCATION);
		setDescription(SELECT_THE_TARGET_LOCATION_AND_THE_TARGET_FILE_NAME);
	}

	@Override
	protected void checkPageStatus() {
		if (getModel().getTargetLocation() == null || "".equals(getModel().getTargetLocation())) { //$NON-NLS-1$
			setPageComplete(false);
			return;
		}
		if (getModel().getFileName() == null || "".equals(getModel().getFileName())) { //$NON-NLS-1$
			setPageComplete(false);
			return;
		}
		IValidationStatus status = model.validateLocation();
		if (status.hasErrors()) {
			setErrorMessage(status.getMessage());
			setPageComplete(false);
		} else if (status.hasWarnings()) {
			setErrorMessage(status.getMessage());
			setPageComplete(true);
		} else {
			setErrorMessage(null);
			setPageComplete(true);
		}
	}

	@Override
	protected String getArtifactContainerName() {
		return ICommonConstants.ARTIFACT_TYPE.SECURITY_CONSTRAINTS;
	}

	@Override
	protected GenerationModel getModel() {
		return model;
	}

	@Override
	protected String getDefaultFileName() {
		return "main.access"; //$NON-NLS-1$
	}

}
