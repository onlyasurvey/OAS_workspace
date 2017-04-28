package com.oas.controller.dashboard.export.excel;

import org.springframework.stereotype.Controller;

import com.oas.controller.AbstractOASController;
import com.oas.model.Question;

/**
 * Extract survey data to Excel format.
 * 
 * TODO move exporting functionality to a service.
 * 
 * @author xhalliday
 * @since November 24, 2008
 */
@Controller
public class AbstractExcelController extends AbstractOASController {

	/**
	 * Generate a default name for a sheet.
	 * 
	 * @return
	 */
	protected final String getDefaultSheetName(Question question) {
		return "Q" + (question.getDisplayOrder());
	}

}
