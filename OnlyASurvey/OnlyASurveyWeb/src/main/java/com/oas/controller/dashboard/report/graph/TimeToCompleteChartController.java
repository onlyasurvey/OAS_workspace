package com.oas.controller.dashboard.report.graph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;

import ca.inforealm.core.security.annotation.ValidUser;

import com.oas.controller.AbstractOASController;
import com.oas.model.Response;
import com.oas.model.Survey;
import com.oas.model.report.time.TimeTakenToComplete;
import com.oas.service.ReportingService;

/**
 * Shows a chart that plots how long it takes people to complete the survey.
 * 
 * @author xhalliday
 * @since October 17, 2009
 */
@Controller
public class TimeToCompleteChartController extends AbstractOASController {

	/** Backing service. */
	@Autowired
	private ReportingService reportingService;

	/** i18n. */
	@Autowired
	private MessageSource messageSource;

	/** Width of the Completion Ratio graph image. */
	public static final int GRAPH_WIDTH = 750;

	/** Height of the Completion Ratio graph image. */
	public static final int GRAPH_HEIGHT = 200;

	/** Responses that take more than this many minutes will be filtered out. */
	public static final int FILTER_OUT_PAST_TIME_TAKEN = 60;

	// ======================================================================

	public TimeToCompleteChartController() {
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
	@RequestMapping("/db/rpt/g/ttc/*.png")
	@ValidUser
	public void timeToCompleteSurveyChart(HttpServletRequest request, HttpServletResponse response) throws IOException {

		// get the survey ID from the URL; handles missing IDs and security
		Survey survey = getSurveyFromRestfulUrl(request);
		Assert.notNull(survey);

		// data comes in sorted
		List<TimeTakenToComplete> originalList = reportingService.getTimeTakenData(survey);
		List<TimeTakenToComplete> filteredOut = new ArrayList<TimeTakenToComplete>(originalList.size());
		filterData(originalList, filteredOut);
		DefaultXYDataset dataset = getDataset(originalList);

		//
		String chartTitle = messageSource
				.getMessage("report.timeTaken.graph.title", null, "Time to Complete", getCurrentLocale());
		String xLabel = messageSource.getMessage("reports.respondents", null, "Complete ", getCurrentLocale());
		String yLabel = "";
		Object[] yAxisArgs = new Object[] { filteredOut.size(), FILTER_OUT_PAST_TIME_TAKEN };
		if (filteredOut.size() > 0) {
			yLabel = messageSource.getMessage("report.timeTaken.graph.yAxis.filtered", yAxisArgs, "Partial ", getCurrentLocale());
		} else {
			yLabel = messageSource.getMessage("report.timeTaken.graph.yAxis", yAxisArgs, "Partial ", getCurrentLocale());
		}

		//
		JFreeChart chart = ChartFactory.createXYLineChart(chartTitle, xLabel, yLabel, dataset, PlotOrientation.HORIZONTAL, false,
				false, false);
		chart.setTextAntiAlias(true);
		chart.setBackgroundPaint(ChartColor.WHITE);
		chart.setBorderPaint(ChartColor.WHITE);

		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setOutlinePaint(ChartColor.WHITE);

		//
		response.setContentType("image/png");
		ChartUtilities.writeChartAsPNG(response.getOutputStream(), chart, //
				GRAPH_WIDTH, GRAPH_HEIGHT //
				);
	}

	/**
	 * Get the dataset for this graph.
	 * 
	 * @param survey
	 *            {@link Survey}
	 * @return {@link DefaultXYDataset}
	 */
	private DefaultXYDataset getDataset(List<TimeTakenToComplete> list) {

		//
		DefaultXYDataset dataset = new DefaultXYDataset();

		int dataSize = list.size();

		double[][] data = new double[2][dataSize];
		int i = 0;
		for (TimeTakenToComplete item : list) {

			data[0][i] = item.getCount();
			data[1][i] = item.getId().getMinutes();

			i++;
		}

		//
		dataset.addSeries("seriesKey", data);
		return dataset;
	}

	/**
	 * Move all items from the first list into the second list that are to be
	 * filtered out based on {@link #FILTER_OUT_PAST_TIME_TAKEN}.
	 * 
	 * @param originalList
	 *            List<TimeTakenToComplete>
	 * @param list
	 *            List<TimeTakenToComplete>
	 */
	private void filterData(List<TimeTakenToComplete> originalList, List<TimeTakenToComplete> list) {

		for (Iterator<TimeTakenToComplete> iterator = originalList.iterator(); iterator.hasNext();) {
			TimeTakenToComplete item = iterator.next();

			if (item.getId().getMinutes() > FILTER_OUT_PAST_TIME_TAKEN) {
				list.add(item);
				iterator.remove();
			}
		}
	}
}
