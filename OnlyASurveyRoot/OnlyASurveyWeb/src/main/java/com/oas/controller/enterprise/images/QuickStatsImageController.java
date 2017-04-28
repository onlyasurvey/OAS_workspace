package com.oas.controller.enterprise.images;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.VerticalAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.oas.controller.AbstractOASController;
import com.oas.model.enterprise.QuickStats;
import com.oas.model.enterprise.ResponseStatSet;
import com.oas.service.enterprise.EnterpriseDashboardService;

/**
 * Main home page controller for the Enterprise package, which shows summary
 * data and provides navigation to more in-depth views.
 * 
 * @author xhalliday
 * @since October 19, 2008
 */
@Controller
public class QuickStatsImageController extends AbstractOASController {

	/** Widht of the generated image. */
	public static final int QUICK_STATS_IMAGE_WIDTH = 750;

	/** Height of the generated image. */
	public static final int QUICK_STATS_IMAGE_HEIGHT = 125;

	/** Dashboard service. */
	@Autowired
	private EnterpriseDashboardService enterpriseDashboardService;

	/** i18n. */
	@Autowired
	private MessageSource messageSource;

	/** Default constructor. */
	public QuickStatsImageController() {
	}

	@RequestMapping("/ent/db/img/qs.png")
	public void quickStatsChart(HttpServletResponse response) throws IOException {

		String chartTitle = null;
		String categoryAxisLabel = null;
		String valueAxisLabel = "Responses";

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		Random rnd = new Random(new Date().getTime());

		QuickStats stats = enterpriseDashboardService.getQuickStats();
		ResponseStatSet deleted = stats.getDeleted();
		ResponseStatSet partial = stats.getPartial();
		ResponseStatSet complete = stats.getClosed();

		String MESSAGE_TOTAL = messageSource.getMessage("enterprise.quickStats.total", null, getCurrentLocale());
		String MESSAGE_DELETED = messageSource.getMessage("enterprise.quickStats.deleted", null, getCurrentLocale());
		String MESSAGE_TODAY = messageSource.getMessage("enterprise.quickStats.today", null, getCurrentLocale());
		String MESSAGE_LAST_WEEK = messageSource.getMessage("enterprise.quickStats.sevenDays", null, getCurrentLocale());
		String MESSAGE_MONTH = messageSource.getMessage("enterprise.quickStats.thisMonth", null, getCurrentLocale());
		String MESSAGE_QUARTER = messageSource.getMessage("enterprise.quickStats.lastQuarter", null, getCurrentLocale());
		String MESSAGE_PARTIAL = messageSource.getMessage("enterprise.quickStats.partial", null, getCurrentLocale());
		String MESSAGE_COMPLETE = messageSource.getMessage("enterprise.quickStats.closed", null, getCurrentLocale());

		dataset.setValue(deleted.getToday(), MESSAGE_DELETED, MESSAGE_TODAY);
		dataset.setValue(deleted.getLastWeek(), MESSAGE_DELETED, MESSAGE_LAST_WEEK);
		dataset.setValue(deleted.getThisMonth(), MESSAGE_DELETED, MESSAGE_MONTH);
		dataset.setValue(deleted.getLastQuarter(), MESSAGE_DELETED, MESSAGE_QUARTER);
		dataset.setValue(deleted.getTotal(), MESSAGE_DELETED, MESSAGE_TOTAL);

		dataset.setValue(partial.getToday(), MESSAGE_PARTIAL, MESSAGE_TODAY);
		dataset.setValue(partial.getLastWeek(), MESSAGE_PARTIAL, MESSAGE_LAST_WEEK);
		dataset.setValue(partial.getThisMonth(), MESSAGE_PARTIAL, MESSAGE_MONTH);
		dataset.setValue(partial.getLastQuarter(), MESSAGE_PARTIAL, MESSAGE_QUARTER);
		dataset.setValue(partial.getTotal(), MESSAGE_PARTIAL, MESSAGE_TOTAL);

		dataset.setValue(complete.getToday(), MESSAGE_COMPLETE, MESSAGE_TODAY);
		dataset.setValue(complete.getLastWeek(), MESSAGE_COMPLETE, MESSAGE_LAST_WEEK);
		dataset.setValue(complete.getThisMonth(), MESSAGE_COMPLETE, MESSAGE_MONTH);
		dataset.setValue(complete.getLastQuarter(), MESSAGE_COMPLETE, MESSAGE_QUARTER);
		dataset.setValue(complete.getTotal(), MESSAGE_COMPLETE, MESSAGE_TOTAL);

		PlotOrientation orientation = PlotOrientation.VERTICAL;

		boolean showLegend = true;
		boolean tooltips = true;
		boolean urls = false;

		JFreeChart chart = ChartFactory.createStackedAreaChart(//
				//
				chartTitle, //
				categoryAxisLabel, valueAxisLabel,//
				// null, null,//
				dataset, orientation, showLegend//
				, tooltips, urls//
				);

		chart.setBackgroundPaint(ChartColor.WHITE);
		// chart.setPadding(new RectangleInsets(0d, 0d, 0d, 0d));

		CategoryPlot categoryplot = (CategoryPlot) chart.getPlot();
		categoryplot.setForegroundAlpha(0.85F);
		CategoryAxis categoryaxis = categoryplot.getDomainAxis();
		categoryaxis.setLowerMargin(0.0D);
		categoryaxis.setUpperMargin(0.0D);
		categoryaxis.setCategoryMargin(0.0D);

		// Move legend
		LegendTitle legend = chart.getLegend();
		legend.setPosition(RectangleEdge.RIGHT);
		legend.setVerticalAlignment(VerticalAlignment.CENTER);

		//
		response.setContentType("image/png");
		// expire in 60 seconds
		int MS = 60000;
		response.setDateHeader("Expires", new Date().getTime() + (MS));
		response.setHeader("Cache-Control", "public; s-maxage=" + (MS / 1000));
		ChartUtilities.writeChartAsPNG(response.getOutputStream(), chart, QUICK_STATS_IMAGE_WIDTH, QUICK_STATS_IMAGE_HEIGHT);
	}
}
