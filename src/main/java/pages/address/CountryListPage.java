package pages.address;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import pages.BasePage;
import pages.components.FrameHandler;

public class CountryListPage extends BasePage{
	
	private final FrameHandler frameHandler = new FrameHandler();
	private final By listTable = By.xpath("//table[@class='list']");
	private final String countryRow = "//span[contains(text(),'%s')]/ancestor::tr";
	private final By editButton = By.xpath(".//td//a[text()='Edit']");
	
	
	public void waitFroTable(){
		frameHandler.swithToiframe();
		log.info("Waiting for contries to load in the country list page");
		wait.visibilityOf(listTable);
		
	}
	
	public StateListPage openEditFor(String code,Boolean needsFrameSwitch) throws InterruptedException{
		
        // If we came back from a Save/Cancel, the frame might have reloaded. Switch back.
		 if (needsFrameSwitch) {
			 frameHandler.switchToSetupFrame();
         }
		
        // Re-find the country row (DOM is refreshed)
        // Optimization: We construct the XPath only once per iteration
        WebElement countryRowWebelement = driver.findElement(By.xpath(String.format(countryRow, code)));

        // Scroll only if needed (Optimization: Check visibility before scrolling)
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", countryRowWebelement);
        Thread.sleep(500);

        // --- Enter Edit Mode ---
        log.info("--- Enter Edit Mode ---");
        countryRowWebelement.findElement((By) editButton).click();;
        return new StateListPage();
	}
}
