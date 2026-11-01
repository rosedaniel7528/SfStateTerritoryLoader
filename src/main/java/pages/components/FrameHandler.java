package pages.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import pages.BasePage;

public class FrameHandler extends BasePage {

	private final By iframe = By.xpath("//iframe[contains(@title,'Configure States, Countries')]");

	public void switchToSetupFrame() {

		driver.switchTo().defaultContent();
		wait.invisibilityOfElementLocated(iframe);
		wait.frameToBeAvailableAndSwitchToIt(iframe);

	}

	public void waitForFrameReload() {

		driver.switchTo().defaultContent();
		wait.invisibilityOfElementLocated(iframe);

	}
	
	public void swithToiframe() {
		wait.frameToBeAvailableAndSwitchToIt(iframe);

	}
	
	public WebElement switchToSetupFrameAndGet() {
	    driver.switchTo().defaultContent();
	    WebElement frame = driver.findElement(iframe);
	    wait.frameToBeAvailableAndSwitchToIt(frame);
	    return frame;
	}

}
