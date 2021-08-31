package com.step.pdf.demo.controller;

import com.github.abel533.echarts.Option;
import com.github.abel533.echarts.axis.CategoryAxis;
import com.github.abel533.echarts.axis.ValueAxis;
import com.github.abel533.echarts.code.Magic;
import com.github.abel533.echarts.code.Tool;
import com.github.abel533.echarts.code.Trigger;
import com.github.abel533.echarts.feature.MagicType;
import com.github.abel533.echarts.series.Line;
import com.step.pdf.demo.screen.DriverTypeEnum;
import com.step.pdf.demo.screen.ScreenshotManager;
import com.step.pdf.demo.screen.param.EchartsScreenshotParam;
import com.step.pdf.demo.screen.param.FreemarkerSeleniumScreenshotParam;
import com.step.pdf.demo.screen.param.ScreenshotResult;
import com.step.pdf.demo.screen.param.SeleniumScreenshotParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/8/24
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/8/24
 */
@Tag(name = "屏幕截图", description = "截屏测试控制器")
@RestController
@RequestMapping(path = "/screenshot")
public class ScreenshotController {

    @Autowired
    private ScreenshotManager manager;

    @Operation(summary = "使用Freemarker生成html并截图", description = "", tags = {"屏幕截图"})
    @GetMapping(path = "/freemarker")
    public ScreenshotResult freemarkerScreenshot(String templateName) throws Exception {
        FreemarkerSeleniumScreenshotParam param =
                new FreemarkerSeleniumScreenshotParam(DriverTypeEnum.PhantomJS);
        param.setTemplatePath(templateName);
        param.setClipWidth(600);
        param.setClipHeight(500);
        return manager.clip(param);
    }

    @Operation(summary = "使用Selenium截图", description = "", tags = {"屏幕截图"})
    @GetMapping(path = "/selenium/phantomjs")
    public ScreenshotResult seleniumScreenshot(String url) throws Exception {
        SeleniumScreenshotParam param = new SeleniumScreenshotParam(DriverTypeEnum.PhantomJS);
        param.setUrl(url);
        return manager.clip(param);
    }

    @Operation(summary = "使用phantomjs进行echarts截图", description = "", tags = {"屏幕截图"})
    @GetMapping(path = "/phantomjs/echarts")
    public ScreenshotResult phantomjsEchartsScreenshot() throws Exception {
        EchartsScreenshotParam param = new EchartsScreenshotParam();
        param.setType("base64");
        param.setOption(getEchartOption());

        return manager.clip(param);
    }

    public Option getEchartOption() {
        Option option = new Option();
        option.legend("this is the legend");
        option.toolbox().show(true).feature(Tool.mark, Tool.dataView, new MagicType(Magic.line,
                        Magic.bar),
                Tool.restore, Tool.saveAsImage);
        option.calculable(true);
        option.tooltip().trigger(Trigger.axis).formatter("Temperature : <br/>{b}km : {c}°C");

        ValueAxis valueAxis = new ValueAxis();
        valueAxis.axisLabel().formatter("{value} °C");
        option.xAxis(valueAxis);

        CategoryAxis categoryAxis = new CategoryAxis();
        categoryAxis.axisLine().onZero(false);
        categoryAxis.axisLabel().formatter("{value} km");
        categoryAxis.boundaryGap(false);
        categoryAxis.data(0, 10, 20, 30, 40, 50, 60, 70, 80);
        option.yAxis(categoryAxis);

        Line line = new Line();
        line.smooth(true).name("line ssss").data(15, -50, -56.5, -46.5, -22.1, -2.5, -27.7, -55.7
                , -76.5).itemStyle()
                .normal().lineStyle().shadowColor("rgba(0,0,0,0.4)");
        option.series(line);
        return option;
    }

    public ScreenshotManager getManager() {
        return manager;
    }

    public void setManager(ScreenshotManager manager) {
        this.manager = manager;
    }
}
