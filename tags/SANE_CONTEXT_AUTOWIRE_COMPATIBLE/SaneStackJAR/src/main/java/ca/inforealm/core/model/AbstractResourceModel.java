package ca.inforealm.core.model;

import ca.inforealm.core.util.DisplayTitleUtil;

public abstract class AbstractResourceModel {

	public String getDisplayTitle() {
		return DisplayTitleUtil.getDisplayTitle(this);
	}

}
