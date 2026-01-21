package cn.dhbin.isme.geometray.utils;

import org.apache.poi.xddf.usermodel.XDDFColor;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xwpf.usermodel.XWPFChart;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.XmlCursor;

import java.util.List;
import java.util.Map;

public class ChartGenerator {

    /**
     * 创建柱形图 - 修复版本
     */
    public static void createBarChart(XWPFDocument document, String chartTitle,
                                      List<String> categories, Map<String, List<Double>> seriesData) {
        try {
            // 创建图表 - 设置合适的尺寸
            XWPFChart chart = document.createChart(15 * 500000, 10 * 500000);

            // 设置图表标题
            if (chartTitle != null) {
                chart.setTitleText(chartTitle);
                chart.setTitleOverlay(false);
            }

            // 必须先创建坐标轴
            XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
            XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
            leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);

            // 获取图表数据源
            XDDFDataSource<String> categoryData = XDDFDataSourcesFactory.fromArray(
                    categories.toArray(new String[0])
            );

            // 创建柱形图数据，传入坐标轴
            XDDFBarChartData data = (XDDFBarChartData) chart.createData(
                    ChartTypes.BAR, bottomAxis, leftAxis
            );
            data.setBarDirection(BarDirection.COL);

            // 添加数据系列
            for (Map.Entry<String, List<Double>> entry : seriesData.entrySet()) {
                String seriesName = entry.getKey();
                List<Double> values = entry.getValue();

                XDDFNumericalDataSource<Double> valueData = XDDFDataSourcesFactory.fromArray(
                        values.toArray(new Double[0])
                );

                XDDFBarChartData.Series series = (XDDFBarChartData.Series) data.addSeries(categoryData, valueData);
                series.setTitle(seriesName, null);

                // 设置系列颜色（可选）
                setSeriesColor(series, data.getSeries().indexOf(series));
            }

            // 绘制图表
            chart.plot(data);

            // 设置坐标轴标题
            bottomAxis.setTitle("类别");
            leftAxis.setTitle("数值");

            // 添加空行
            addEmptyParagraph(document);

        } catch (Exception e) {
            throw new RuntimeException("创建柱形图失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建饼状图 - 修复版本
     */
    public static void createPieChart(XWPFDocument document, String chartTitle,
                                      Map<String, Double> dataMap) {
        try {
            // 创建图表
            XWPFChart chart = document.createChart(12 * 500000, 10 * 500000);

            // 设置图表标题
            if (chartTitle != null) {
                chart.setTitleText(chartTitle);
                chart.setTitleOverlay(false);
            }

            // 准备数据
            String[] categories = dataMap.keySet().toArray(new String[0]);
            Double[] values = dataMap.values().toArray(new Double[0]);

            XDDFDataSource<String> categoryData = XDDFDataSourcesFactory.fromArray(categories);
            XDDFNumericalDataSource<Double> valueData = XDDFDataSourcesFactory.fromArray(values);

            // 创建饼图数据
            XDDFChartData data = chart.createData(ChartTypes.PIE, null, null);
            XDDFChartData.Series series = data.addSeries(categoryData, valueData);
            series.setTitle("数据", null);

            // 绘制图表
            chart.plot(data);

            // 添加空行
            addEmptyParagraph(document);

        } catch (Exception e) {
            throw new RuntimeException("创建饼图失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建简单的条形图
     */
    public static void createSimpleBarChart(XWPFDocument document, String title,
                                            String categoryTitle, String valueTitle,
                                            List<String> categories, List<Double> values) {
        try {
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (int i = 0; i < paragraphs.size(); i++) {
                XWPFParagraph xwpfParagraph = paragraphs.get(i);
                String text = xwpfParagraph.getText();
                if (text.equals("${test-tubiao}")){
                    XmlCursor xmlCursor = xwpfParagraph.getCTP().newCursor();
                    XWPFChart chart = document.createChart(12 * 500000, 8 * 500000);

                    if (title != null) {
                        chart.setTitleText(title);
                    }

                    // 创建坐标轴
                    XDDFCategoryAxis categoryAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
                    XDDFValueAxis valueAxis = chart.createValueAxis(AxisPosition.LEFT);
                    valueAxis.setCrosses(AxisCrosses.AUTO_ZERO);

                    // 准备数据
                    XDDFDataSource<String> catDataSource = XDDFDataSourcesFactory.fromArray(
                            categories.toArray(new String[0])
                    );
                    XDDFNumericalDataSource<Double> valDataSource = XDDFDataSourcesFactory.fromArray(
                            values.toArray(new Double[0])
                    );

                    // 创建图表数据
                    XDDFBarChartData data = (XDDFBarChartData) chart.createData(
                            ChartTypes.BAR, categoryAxis, valueAxis
                    );

                    XDDFBarChartData.Series series = (XDDFBarChartData.Series) data.addSeries(catDataSource, valDataSource);
                    series.setTitle(valueTitle, null);
                    data.setBarDirection(BarDirection.COL);

                    // 绘制
                    chart.plot(data);

                    // 设置坐标轴标题
                    categoryAxis.setTitle(categoryTitle);
                    valueAxis.setTitle(valueTitle);
                    xwpfParagraph.createRun();
                    //addEmptyParagraph(document);

                }
            }
        }catch (Exception e) {
            throw new RuntimeException("创建简单柱形图失败: " + e.getMessage(), e);
        }

    }

    /**
     * 创建检验统计图表 - 简化版本
     */
    public static void createInspectionChart(XWPFDocument document, String jobName) {
        try {
            // 添加图表标题
            addChartTitle(document, jobName + " - 检验统计图表");

            // 1. 创建月度检验统计柱形图
            List<String> months = List.of("1月", "2月", "3月", "4月", "5月", "6月");
            List<Double> inspectionCounts = List.of(120.0, 150.0, 180.0, 160.0, 200.0, 190.0);

            createSimpleBarChart(document, "月度检验数量统计", "月份", "检验数量", months, inspectionCounts);

            // 2. 创建合格率柱形图
            List<Double> passRates = List.of(95.8, 96.7, 97.2, 96.9, 97.5, 97.3);
            createSimpleBarChart(document, "月度合格率统计", "月份", "合格率(%)", months, passRates);

            // 3. 创建检验类型分布饼图（使用模拟数据）
            Map<String, Double> typeDistribution = Map.of(
                    "常规检验", 45.0,
                    "专项检验", 25.0,
                    "定期检验", 20.0,
                    "临时检验", 10.0
            );
            createPieChart(document, "检验类型分布", typeDistribution);

            // 添加图表说明
            addChartDescription(document, "以上图表展示了检验任务的统计数据和分布情况。");

        } catch (Exception e) {
            throw new RuntimeException("创建检验图表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 设置系列颜色
     */
    private static void setSeriesColor(XDDFChartData.Series series, int seriesIndex) {
        try {
            // 预定义颜色（RGB）
            byte[][] colors = {
                    {0, 112, (byte) 192}, // 蓝色
                    {(byte) 237, 125, 49}, // 橙色
                    {112, (byte) 173, 71}, // 绿色
                    {(byte) 255, (byte) 192, 0}, // 黄色
                    {68, 114, (byte) 196}, // 深蓝
                    {(byte) 255, 0, 0}     // 红色
            };

            if (seriesIndex < colors.length) {
                byte[] color = colors[seriesIndex];
                XDDFSolidFillProperties fill = new XDDFSolidFillProperties(
                        XDDFColor.from(new byte[]{color[0], color[1], color[2]})
                );
                series.setFillProperties(fill);
            }
        } catch (Exception e) {
            // 颜色设置失败不影响主要功能
            System.err.println("设置系列颜色失败: " + e.getMessage());
        }
    }

    /**
     * 添加图表标题
     */
    private static void addChartTitle(XWPFDocument document, String title) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(title);
        run.setBold(true);
        run.setFontSize(14);
        run.setFontFamily("宋体");
    }

    /**
     * 添加图表说明
     */
    private static void addChartDescription(XWPFDocument document, String description) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(description);
        run.setFontSize(10);
        run.setFontFamily("宋体");
        run.setItalic(true);
    }

    /**
     * 创建散点图
     * 用于轨距、水平、高低、轨向等重复性检测
     */
    public static void createScatterChart(XWPFDocument document, String chartTitle,
                                         String xAxisTitle, String yAxisTitle,
                                         List<String> xLabels, List<Double> yValues) {
        try {
            // 创建图表
            XWPFChart chart = document.createChart(15 * 500000, 10 * 500000);

            // 设置图表标题
            if (chartTitle != null) {
                chart.setTitleText(chartTitle);
                chart.setTitleOverlay(false);
            }

            // 创建坐标轴
            XDDFCategoryAxis categoryAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
            XDDFValueAxis valueAxis = chart.createValueAxis(AxisPosition.LEFT);
            valueAxis.setCrosses(AxisCrosses.AUTO_ZERO);

            // 准备数据
            XDDFDataSource<String> catDataSource = XDDFDataSourcesFactory.fromArray(
                    xLabels.toArray(new String[0])
            );
            XDDFNumericalDataSource<Double> valDataSource = XDDFDataSourcesFactory.fromArray(
                    yValues.toArray(new Double[0])
            );

            // 创建散点图数据
            XDDFScatterChartData data = (XDDFScatterChartData) chart.createData(
                    ChartTypes.SCATTER, categoryAxis, valueAxis
            );

            // 添加数据系列
            XDDFScatterChartData.Series series = (XDDFScatterChartData.Series) data.addSeries(
                    catDataSource, valDataSource
            );
            series.setTitle("检测值", null);

            // 设置系列颜色为红色（平均值曲线）
            XDDFSolidFillProperties fill = new XDDFSolidFillProperties(
                    XDDFColor.from(new byte[]{(byte) 255, 0, 0})
            );
            series.setFillProperties(fill);

            // 绘制图表
            chart.plot(data);

            // 设置坐标轴标题
            categoryAxis.setTitle(xAxisTitle);
            valueAxis.setTitle(yAxisTitle);

            // 添加空行
            addEmptyParagraph(document);

        } catch (Exception e) {
            throw new RuntimeException("创建散点图失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建多系列散点图（用于重复性检测：曲线1、曲线2、曲线3、平均值）
     */
    public static void createMultiSeriesScatterChart(XWPFDocument document, String chartTitle,
                                                    String xAxisTitle, String yAxisTitle,
                                                    List<String> xLabels,
                                                    List<Double> curve1, List<Double> curve2, List<Double> curve3,
                                                    List<Double> average, List<Double> upperLimit, List<Double> lowerLimit) {
        try {
            // 创建图表
            XWPFChart chart = document.createChart(15 * 500000, 10 * 500000);

            // 设置图表标题
            if (chartTitle != null) {
                chart.setTitleText(chartTitle);
                chart.setTitleOverlay(false);
            }

            // 创建坐标轴
            XDDFCategoryAxis categoryAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
            XDDFValueAxis valueAxis = chart.createValueAxis(AxisPosition.LEFT);
            valueAxis.setCrosses(AxisCrosses.AUTO_ZERO);

            // 准备分类数据
            XDDFDataSource<String> catDataSource = XDDFDataSourcesFactory.fromArray(
                    xLabels.toArray(new String[0])
            );

            // 创建散点图数据
            XDDFScatterChartData data = (XDDFScatterChartData) chart.createData(
                    ChartTypes.SCATTER, categoryAxis, valueAxis
            );

            // 添加曲线1
            if (curve1 != null && !curve1.isEmpty()) {
                XDDFNumericalDataSource<Double> valDataSource1 = XDDFDataSourcesFactory.fromArray(
                        curve1.toArray(new Double[0])
                );
                XDDFScatterChartData.Series series1 = (XDDFScatterChartData.Series) data.addSeries(
                        catDataSource, valDataSource1
                );
                series1.setTitle("曲线1", null);
                setSeriesColor(series1, 0);
            }

            // 添加曲线2
            if (curve2 != null && !curve2.isEmpty()) {
                XDDFNumericalDataSource<Double> valDataSource2 = XDDFDataSourcesFactory.fromArray(
                        curve2.toArray(new Double[0])
                );
                XDDFScatterChartData.Series series2 = (XDDFScatterChartData.Series) data.addSeries(
                        catDataSource, valDataSource2
                );
                series2.setTitle("曲线2", null);
                setSeriesColor(series2, 1);
            }

            // 添加曲线3
            if (curve3 != null && !curve3.isEmpty()) {
                XDDFNumericalDataSource<Double> valDataSource3 = XDDFDataSourcesFactory.fromArray(
                        curve3.toArray(new Double[0])
                );
                XDDFScatterChartData.Series series3 = (XDDFScatterChartData.Series) data.addSeries(
                        catDataSource, valDataSource3
                );
                series3.setTitle("曲线3", null);
                setSeriesColor(series3, 2);
            }

            // 添加平均值曲线（红色）
            if (average != null && !average.isEmpty()) {
                XDDFNumericalDataSource<Double> valDataSourceAvg = XDDFDataSourcesFactory.fromArray(
                        average.toArray(new Double[0])
                );
                XDDFScatterChartData.Series seriesAvg = (XDDFScatterChartData.Series) data.addSeries(
                        catDataSource, valDataSourceAvg
                );
                seriesAvg.setTitle("平均值", null);
                // 设置红色
                XDDFSolidFillProperties fill = new XDDFSolidFillProperties(
                        XDDFColor.from(new byte[]{(byte) 255, 0, 0})
                );
                seriesAvg.setFillProperties(fill);
            }

            // 添加均值上限（均值+0.225）
            if (upperLimit != null && !upperLimit.isEmpty()) {
                XDDFNumericalDataSource<Double> valDataSourceUpper = XDDFDataSourcesFactory.fromArray(
                        upperLimit.toArray(new Double[0])
                );
                XDDFScatterChartData.Series seriesUpper = (XDDFScatterChartData.Series) data.addSeries(
                        catDataSource, valDataSourceUpper
                );
                seriesUpper.setTitle("均值上限（均值+0.225）", null);
                setSeriesColor(seriesUpper, 4);
            }

            // 添加均值下限（均值-0.225）
            if (lowerLimit != null && !lowerLimit.isEmpty()) {
                XDDFNumericalDataSource<Double> valDataSourceLower = XDDFDataSourcesFactory.fromArray(
                        lowerLimit.toArray(new Double[0])
                );
                XDDFScatterChartData.Series seriesLower = (XDDFScatterChartData.Series) data.addSeries(
                        catDataSource, valDataSourceLower
                );
                seriesLower.setTitle("均值下限（均值-0.225）", null);
                setSeriesColor(seriesLower, 5);
            }

            // 绘制图表
            chart.plot(data);

            // 设置坐标轴标题
            categoryAxis.setTitle(xAxisTitle);
            valueAxis.setTitle(yAxisTitle);

            // 添加空行
            addEmptyParagraph(document);

        } catch (Exception e) {
            throw new RuntimeException("创建多系列散点图失败: " + e.getMessage(), e);
        }
    }

    /**
     * 添加空段落
     */
    private static void addEmptyParagraph(XWPFDocument document) {
        document.createParagraph();
    }
}