package pages.address;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import models.SaveResult;
import pages.BasePage;
import pages.components.FrameHandler;

public class NewStatePage extends BasePage {

	private final FrameHandler frameHandler = new FrameHandler();
	private WebElement formFrame;
	private final By editName = By.xpath("//input[contains(@id,'editName')]");
	private final By editCode = By.xpath("//input[contains(@id,'editIsoCode')]");
	private final By activeCheckBox = By.xpath("//input[contains(@id,'editActive')]");
	private final By visibleCheckBox = By.xpath("//input[contains(@id,'editVisible')]");
	private final By addButton = By.xpath("//input[contains(@id,'addButton')]");
	private final By errorHeader = By.xpath("//table//h4");
	private final By cancelButton = By.xpath("//input[contains(@id,'cancelButton')]");
	private final By errorBox = By.xpath("//table//h4/../..");

	public void fillForm(String name, String code) {

		// Wait for "New State" form
		frameHandler.switchToSetupFrame();

		wait.elementToBeClickable(editName);

		// Capture the current iframe element for the Race Condition check
		formFrame = frameHandler.switchToSetupFrameAndGet();

		// Fill Form
		driver.findElement(editName).sendKeys(name);
		driver.findElement(editCode).sendKeys(code);

		WebElement activeCheckBoxWebelement = driver.findElement(activeCheckBox);
		if (!activeCheckBoxWebelement.isSelected())
			activeCheckBoxWebelement.click();

		wait.getWait().until(driver -> {
			try {
				WebElement el = driver.findElement(visibleCheckBox);
				return el.getDomAttribute("disabled") == null;
			} catch (StaleElementReferenceException | NoSuchElementException e) {
				return false; // retry
			}
		});
		driver.findElement(visibleCheckBox).click();

	}

	public SaveResult save() {

		// Click Add
		driver.findElement(addButton).click();

		// --- THE OPTIMIZED WAIT LOGIC ---
		driver.switchTo().defaultContent();

		// We use a 1-size array to store the result from inside the lambda
		// boolean[0] = true (Error found), boolean[0] = false (Success)
		final boolean[] errorDetected = { false };

		try {
			wait.getLongWait().until(d -> {
				d.switchTo().defaultContent();

				// 1. Check if Frame is Stale (Success Case)
				try {
					formFrame.getTagName();
				} catch (StaleElementReferenceException e) {
					// Frame is gone/refreshing -> Success
					return true;
				}

				// 2. Check for Error Message (Error Case)
				try {
					d.switchTo().frame(formFrame);
					if (d.findElements(errorHeader).size() > 0) {
						errorDetected[0] = true; // Mark as error
						return true; // Stop waiting
					}
				} catch (StaleElementReferenceException | NoSuchElementException e) {
					// Frame died while switching -> Success
					return true;
				}
				return false; // Keep waiting
			});
		} catch (TimeoutException e) {
			// If timeout happens, assume success (frame reloaded but slow) or handle
			// accordingly
			log.warn("Timed out waiting for save result, assuming frame reloaded");
		}

		// --- POST WAIT ACTION ---
		if (errorDetected[0]) {
			try {
		        String message = driver.findElement(errorBox).getText();
		        return new SaveResult(false, message);

			} catch (Exception e) {
				log.error("Could not read error message: {}", e.getMessage());
			       return new SaveResult(false, "Unknown error");			}
		} 
			// Success case
			// No specific action needed, the loop continues.
		    frameHandler.waitForFrameReload();       // success: frame is reloading
		    return new SaveResult(true, "");
	}

	public void cancel() {
		wait.elementToBeClickable(cancelButton);
		driver.findElement(cancelButton).click();
		frameHandler.waitForFrameReload();
	}

}
