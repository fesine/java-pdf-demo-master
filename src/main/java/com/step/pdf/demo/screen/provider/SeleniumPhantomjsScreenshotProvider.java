package com.step.pdf.demo.screen.provider;

import com.step.pdf.demo.screen.DriverTypeEnum;
import com.step.pdf.demo.screen.param.ScreenshotParam;
import com.step.pdf.demo.screen.param.ScreenshotResult;
import com.step.pdf.demo.screen.param.SeleniumScreenshotParam;
import com.step.pdf.demo.screen.util.OSUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Some of the examples of Headless Drivers include <br />
 * 1,HtmlUnit <br />
 * 2,Ghost <br />
 * 3,PhantomJS <br />
 * 4,ZombieJS <br />
 * 5,Watir-webdriver <br />
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/8/24
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/8/24
 */
@Component
@Slf4j
public class SeleniumPhantomjsScreenshotProvider implements ScreenshotProvider {


    @Value("${screenshotDir:}")
    protected String screenshotDir;
    @Value("${phantomjsPath:}")
    protected String phantomjsPath;

    @PostConstruct
    public void init() {
        if (StringUtils.isBlank(screenshotDir)) {
            try {
                File classPathDir = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX);
                File dir = new File(classPathDir.getAbsolutePath() + "/screenshot/selenium/phantomjs/images");
                if (!dir.exists() || !dir.isDirectory()) {
                    dir.mkdirs();
                }
                screenshotDir = dir.getAbsolutePath();
                log.info("screenshotDir:{}", screenshotDir);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (StringUtils.isBlank(phantomjsPath)) {
            try {
                File exeFile = null;
                if (OSUtil.isUnix() || OSUtil.isMac()) {
                    exeFile = ResourceUtils
                            .getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "drivers/phantomjs/v2.1.1/linux64/phantomjs");
                } else {
                    exeFile = ResourceUtils.getFile(
                            ResourceUtils.CLASSPATH_URL_PREFIX + "drivers/phantomjs/v2.1.1/window/phantomjs.exe");
                }
                phantomjsPath = exeFile.getAbsolutePath();
                log.info("phantomjsPath:{}", phantomjsPath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    // 生成图片的所存放路径
    protected String getRandomImagePath() {
        String outPath = screenshotDir + File.separator + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + "-" + new Random().nextInt() + ".png";
        return outPath;
    }

    @Override
    public boolean support(ScreenshotParam param) {
        if (!(param instanceof SeleniumScreenshotParam)) {
            return false;
        }
        SeleniumScreenshotParam params = (SeleniumScreenshotParam) param;
        return params.getDriverType() == DriverTypeEnum.PhantomJS;
    }

    @Override
    public ScreenshotResult clip(ScreenshotParam param) throws Exception {
        return new ScreenshotResult(getPicByPhantomjs(param));
    }

    private String getPicByPhantomjs(ScreenshotParam param) throws Exception {
        log.warn("使用phantomjs截图链接:{}", param.getUrl());
        // 定义图片存储路径
        DesiredCapabilities capabilities = new DesiredCapabilities(BrowserType.PHANTOMJS, "", Platform.ANY);
        WebDriver driver = null;
        try {
            // ssl证书支持
            capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            // 截屏支持
            capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
            // css搜索支持
            capabilities.setCapability(CapabilityType.SUPPORTS_FINDING_BY_CSS, true);
            // js支持
            capabilities.setJavascriptEnabled(true);
            capabilities.setCapability(CapabilityType.SUPPORTS_LOCATION_CONTEXT, true);
            capabilities.setCapability(CapabilityType.SUPPORTS_NETWORK_CONNECTION, true);

            // defines whether to execute the script in the page or not (defaults to true).
            capabilities.setCapability("phantomjs.page.settings.javascriptEnabled", true);
            // defines whether to load the inlined images or not (defaults to true)
            capabilities.setCapability("phantomjs.page.settings.loadImages", true);
            // defines whether local resource (e.g. from file) can access remote URLs or not
            // (defaults to false).
            capabilities.setCapability("phantomjs.page.settings.localToRemoteUrlAccessEnabled", true);

            // sets the user name used for HTTP authentication.
            // capabilities.setCapability("phantomjs.page.settings.userName", "");
            // sets the password used for HTTP authentication.
            // capabilities.setCapability("phantomjs.page.settings.password", "");
            // defines whether web security should be enabled or not (defaults to true).
            capabilities.setCapability("phantomjs.page.settings.webSecurityEnabled", true);
            // (in milli-secs) defines the timeout after which any resource requested will
            // stop trying and proceed with other parts of the page.
            // capabilities.setCapability("phantomjs.page.settings.resourceTimeout", 5000);

            capabilities.setCapability("phantomjs.page.settings.userAgent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:64.0) Gecko/20100101 Firefox/64.0");
            capabilities.setCapability("phantomjs.page.customHeaders.User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:64.0) Gecko/20100101 Firefox/64.0");

            // 驱动支持（第二参数表明的是你的phantomjs引擎-例如phantomjs.exe所在的路径）
            capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomjsPath);

            // 创建***面浏览器对象
            driver = new PhantomJSDriver(capabilities);
            driver.manage().window().setSize(new Dimension(param.getClipWidth(), param.getClipHeight()));
            // 设置隐性等待（作用于全局）
            driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS).pageLoadTimeout(20, TimeUnit.SECONDS);

            // 发起请求
            try {
                driver.get(param.getUrl());
            } catch (Exception e) {
                log.error("开发地址 {} 发生异常:{}", param.getUrl(), e.getMessage());
                throw e;
            }
            try {
                Thread.sleep(4 * 1000);
            } catch (InterruptedException e) {
            }
            // 指定了OutputType.FILE做为参数传递给getScreenshotAs()方法，其含义是将截取的屏幕以文件形式返回。
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            try {
                Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
            }

            // 利用FileUtils工具类的copyFile()方法保存getScreenshotAs()返回的文件对象
            String outFilePath = this.getRandomImagePath();

            FileCopyUtils.copy(srcFile, new File(outFilePath));

            return outFilePath;
        } catch (Exception e) {
            log.error("使用phantomjs截图时异常：{}", e.getMessage());
            throw e;
        } finally {
            if (driver != null) {
                driver.close();
                driver.quit();
            }
        }
    }

    protected void scrollWindow(JavascriptExecutor js) {
        // 页面下滑10次,每次下滑加载2s
        for (int i = 0; i < 10; i++) {
            js.executeScript("window.scrollBy(0,1000)"); // 睡眠2s等js加载完成
            try {
                Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
            }
        }
    }

    protected void clickAction(PhantomJSDriver driver, String elementClassName) {
        if (elementExist(driver, By.className(elementClassName))) {
            WebElement inputBox = driver.findElement(By.className(elementClassName));
            Actions action = new Actions(driver);
            action.click(inputBox).build().perform(); // 元素点击 后等待加载
            try {
                Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
            }
        }
    }

    private boolean elementExist(PhantomJSDriver driver, By by) {
        return driver.findElement(by) == null ? false : true;
    }

}
