package net.sf.tail.graphics;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.sf.tail.Indicator;
import net.sf.tail.TimeSeries;
import net.sf.tail.TimeSeriesSlicer;
import net.sf.tail.Trade;
import net.sf.tail.indicator.helper.StandardDeviationIndicator;
import net.sf.tail.indicator.simple.ClosePriceIndicator;
import net.sf.tail.indicator.tracker.SMAIndicator;
import net.sf.tail.indicator.tracker.bollingerbands.BollingerBandsLowerIndicator;
import net.sf.tail.indicator.tracker.bollingerbands.BollingerBandsMiddleIndicator;
import net.sf.tail.indicator.tracker.bollingerbands.BollingerBandsUpperIndicator;
import net.sf.tail.io.reader.CedroTimeSeriesLoader;
import net.sf.tail.runner.HistoryRunner;
import net.sf.tail.series.RegularSlicer;
import net.sf.tail.strategy.IndicatorCrossedIndicatorStrategy;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.joda.time.Period;

public class ChartTest extends ApplicationFrame implements ActionListener {

	private static final long serialVersionUID = -6626066873257820845L;

	private SeriesDataset seriesDataset;

	private final List<Indicator<? extends Number>> indicators;

	private final TimeSeries timeSeries;

	private int seriesEndIndex = 50;

	public ChartTest(String chartName) {
		super(chartName);

		indicators = new ArrayList<Indicator<? extends Number>>();
		timeSeries = readFileData();

		JButton button2 = new JButton("Move Left");
		button2.setActionCommand("Move Left");
		button2.addActionListener(this);

		JButton button = new JButton("Move Right");
		button.setActionCommand("Move Right");
		button.addActionListener(this);

		JPanel chartPanel = createSmaAndStrategyChart();
		chartPanel.setPreferredSize(new Dimension(800, 600));

		JPanel mainPanel = new JPanel(new FlowLayout());
		mainPanel.add(button2, "South");
		mainPanel.add(button, "South");
		mainPanel.add(chartPanel, "North");

		mainPanel.setPreferredSize(new Dimension(1280, 1024));

		setContentPane(mainPanel);
	}

	public static void main(String args[]) {
		ChartTest chart = new ChartTest("Chart test");
		chart.pack();
		RefineryUtilities.centerFrameOnScreen(chart);
		chart.setVisible(true);
	}

	public JPanel createBollingerBandsChart() {

		ClosePriceIndicator close = new ClosePriceIndicator(timeSeries);
		indicators.add(close);

		BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(close);
		StandardDeviationIndicator standard = new StandardDeviationIndicator(close, 8);
		BollingerBandsLowerIndicator bbl = new BollingerBandsLowerIndicator(bbm, standard);
		BollingerBandsUpperIndicator bbu = new BollingerBandsUpperIndicator(bbm, standard);

		indicators.add(bbu);
		indicators.add(bbm);
		indicators.add(bbl);

		seriesDataset = new SeriesDataset(timeSeries, indicators, 0, seriesEndIndex);

		SeriesChart seriesChart = new SeriesChart(seriesDataset);
		JFreeChart jfreechart = seriesChart.createChart("Test Chart", false, true);

		return new ChartPanel(jfreechart);
	}

	public JPanel createSmaAndStrategyChart() {
		ClosePriceIndicator close = new ClosePriceIndicator(timeSeries);
		SMAIndicator sma = new SMAIndicator(close, 8);

		indicators.add(close);
		indicators.add(sma);

		TimeSeriesSlicer slicer = new RegularSlicer(timeSeries,new Period().withYears(2000));
		
		List<Trade> trades = new HistoryRunner(slicer,new IndicatorCrossedIndicatorStrategy(close, sma)).run(0);

		seriesDataset = new SeriesDataset(timeSeries, indicators, 0, seriesEndIndex, trades);

		SeriesChart seriesChart = new SeriesChart(seriesDataset);
		JFreeChart jfreechart = seriesChart.createChart("Test Chart", false, true);

		return new ChartPanel(jfreechart);
	}

	/**
	 * @return
	 */
	private TimeSeries readFileData() {
		CedroTimeSeriesLoader ctsl = new CedroTimeSeriesLoader();
		TimeSeries timeSeries = null;
		try {
			timeSeries = ctsl.load(new FileInputStream("BaseBovespa/15min/ambv4.csv"), "");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return timeSeries;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Move Right")) {

			seriesDataset.moveRight(1);
		} else if (e.getActionCommand().equals("Move Left")) {

			seriesDataset.moveLeft(1);
		}

	}
}
