package pages.address;

import java.util.Set;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import pages.BasePage;
import pages.components.FrameHandler;

public class StateListPage extends BasePage{
	
	private final FrameHandler frameHandler = new FrameHandler();
	private final By stateRows = By.xpath("//table[@class='list']//tr[td]//td[contains(@id,'nameCol')]");
	private final By noRecords = By.xpath("//span[contains(text(),'No records to display')]");
	private final By newStateButton = By.xpath("//input[@value='New State']");
	private final By cancelButton = By.xpath("//input[contains(@id,'cancelButton')]");
	
	
	public Set<String> getExistingStateName() {
        // Wait for Edit Page to Load (Frame reloads here)
		frameHandler.switchToSetupFrame();
        
        // Check existing states to avoid duplicates        
        wait.getWait().until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(stateRows),
                ExpectedConditions.visibilityOfElementLocated(noRecords)
        ));
        
        // Wait for Edit Page to Load (Frame reloads here)
        return driver.findElements(stateRows).stream()
                .map(e -> e.getText().trim())
                .collect(Collectors.toSet());
	}
	
	public NewStatePage clickAddState(Boolean hasEnteredNewStateMode) {
        // If we previously added a state, the frame reloaded, need to switch back
        if (hasEnteredNewStateMode) {
        	frameHandler.switchToSetupFrame();
        }

        // Click "New State"
        wait.elementToBeClickable(newStateButton);
        log.info("Adding new State:");
        driver.findElement(newStateButton).click();
        return new NewStatePage();
	}
	
	public void backToCountryList(boolean hasEnteredNewStateMode) {
        // --- Exit Edit Mode (Back to Country List) ---
        if (hasEnteredNewStateMode) {
        	frameHandler.switchToSetupFrame();
        }
        
        // Click Cancel/Back to go to list
        wait.elementToBeClickable(cancelButton);
        driver.findElement(cancelButton).click();

        // Wait for the main list to reappear implies the iframe reloads
        frameHandler.waitForFrameReload();
	}

}
