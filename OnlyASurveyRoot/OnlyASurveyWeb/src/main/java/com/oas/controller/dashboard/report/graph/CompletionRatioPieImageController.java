package com.oas.controller.dashboard.report.graph;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.VerticalAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;

import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.controller.AbstractOASController;
import com.oas.model.Response;
import com.oas.model.Survey;

/**
 * Shows a ratio of completed vs. partial responses in pie chart form.
 * 
 * @author xhalliday
 * @since October 17, 2009
 */
@Controller
public class CompletionRatioPieImageController extends AbstractOASController {

	@Autowired
	private MessageSource messageSource;

	/** Width of the Completion Ratio graph image. */
	public static final int COMPLETION_RATIO_GRAPH_WIDTH = 300;

	/** Height of the Completion Ratio graph image. */
	public static final int COMPLETION_RATIO_GRAPH_HEIGHT = 150;

	// ======================================================================

	public CompletionRatioPieImageController() {
	}

	// ======================================================================

	/**
	 * Generate a pie chart of completed:partial {@link Response}s for a
	 * {@link Survey}.
	 * 
	 * @param request
	 *            {@link HttpServletRequest}
	 * @param response
	 *            {@link HttpServletResponse}
	 * @throws IOException
	 *             On writing
	 */
	@RequestMapping("/db/rpt/abnd/cp_rto/*.png")
	@ValidUser
	public void completionRatioPieChart(HttpServletRequest request, HttpServletResponse response) throws IOException {

		// get the survey ID from the URL; handles missing IDs and security
		Survey survey = getSurveyFromRestfulUrl(request);
		Assert.notNull(survey);

		// total responses, including partial but not deleted
		int totalResponses = surveyService.countResponses(survey);
		// total responses, only including completed, non-deleted
		int closedResponses = surveyService.countClosedResponses(survey);
		//
		int partialResponses = totalResponses - closedResponses;

		String title = messageSource.getMessage("report.abandonment.rateGraph.title", null, "Completion Ratio",
				getCurrentLocale());

		String completeLabel = messageSource.getMessage("reports.complete", null, "Complete ", getCurrentLocale()) + " "
				+ closedResponses;
		String partialLabel = messageSource.getMessage("reports.partial", null, "Partial ", getCurrentLocale()) + " "
				+ partialResponses;

		DefaultPieDataset dataset = new DefaultPieDataset();
		dataset.setValue(completeLabel, closedResponses);
		dataset.setValue(partialLabel, partialResponses);

		JFreeChart chart = ChartFactory.createPieChart3D(null, dataset, true, true, false);
		chart.setTextAntiAlias(true);
		chart.setBackgroundPaint(ChartColor.WHITE);
		chart.setBorderPaint(ChartColor.WHITE);

		LegendTitle legend = chart.getLegend();
		legend.setFrame(BlockBorder.NONE);
		legend.setPosition(RectangleEdge.RIGHT);
		legend.setVerticalAlignment(VerticalAlignment.CENTER);

		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setToolTipGenerator(null);
		plot.setLabelGenerator(null);
		plot.setOutlinePaint(ChartColor.WHITE);

		//
		response.setContentType("image/png");
		ChartUtilities.writeChartAsPNG(response.getOutputStream(), chart, //
				COMPLETION_RATIO_GRAPH_WIDTH, COMPLETION_RATIO_GRAPH_HEIGHT);
	}
}
