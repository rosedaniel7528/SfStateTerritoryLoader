package flows;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import models.SaveResult;
import models.StateData;
import pages.address.CountryListPage;
import pages.address.NewStatePage;
import pages.address.StateListPage;

public class StateLoadFlow {
	
	CountryListPage countryListPage;
	private static final Logger log = LogManager.getLogger(StateLoadFlow.class);
	
	
	public StateLoadFlow(){
		this.countryListPage = new CountryListPage();
	}
	
	public void run(Map<String, List<StateData>> countryStateMap) throws InterruptedException {
    	        
        countryListPage.waitFroTable();
        
        
        // State control flags (Renamed for clarity)
        boolean needsFrameSwitch = false; 


        // --- Loop through Countries ---
        for (Map.Entry<String, List<StateData>> entry : countryStateMap.entrySet()) {
            String countryCode = entry.getKey();
            log.info("Procession: {}",countryCode);
                                  
            StateListPage stateListPage = countryListPage.openEditFor(countryCode,needsFrameSwitch);
            
            Set<String> existingStates = stateListPage.getExistingStateName();

            boolean hasEnteredNewStateMode = false;

            // --- Loop through States to Add ---
            for (StateData state :  entry.getValue()) {

                if (existingStates.contains(state.getDescription())) {
                	log.warn("State alreaday exists: {}",state.getDescription());
                    continue;
                }

                NewStatePage newStatePage = stateListPage.clickAddState(hasEnteredNewStateMode);
                newStatePage.fillForm(state.getDescription(), state.getRegion());


                SaveResult result = newStatePage.save();

                if (result.success()) {
                    log.info("Added {}", state.getDescription());
                } else {
                    log.error("Could not add {}: {}", state.getDescription(), result.message());
                    newStatePage.cancel();
                }
                
                hasEnteredNewStateMode = true; 
            }
            stateListPage.backToCountryList(hasEnteredNewStateMode);
            needsFrameSwitch = true; 
        }
    
		
	}

}
